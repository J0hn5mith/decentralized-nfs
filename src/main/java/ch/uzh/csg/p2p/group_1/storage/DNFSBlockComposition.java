package ch.uzh.csg.p2p.group_1.storage;

/**
 * Created by janmeier on 14.05.15.
 */

import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIBlock;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.storage.interfaces.IStorage;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import net.tomp2p.peers.Number160;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Wrapper class for iNodes so DNFSFile and DNFSFolder don't have to bother with
 * getting/ and writing blocks
 */
public class DNFSBlockComposition implements DNFSIBlock {
    final private static Logger LOGGER = Logger.getLogger(DNFSBlockComposition .class.getName());

    DNFSIiNode iNode;
    IStorage peer;

    public IStorage getPeer() {
        return peer;
    }

    public DNFSIiNode getINode() {
        return iNode;
    }

    public DNFSBlockComposition(DNFSIiNode iNode, IStorage peer) {
        this.iNode = iNode;
        this.peer = peer;
        LOGGER.setLevel(Level.DEBUG);
    }

    @Override
    public long getSize() throws DNFSException.DNFSBlockStorageException {
        long totalSize = 0;
        for (Number160 blockID : iNode.getBlockIDs()) {
            DNFSBlock block = this.peer.getBlock(blockID);
            totalSize += block.getSize();
        }
        return totalSize;
    }

    public long getCapacity(){
        return DNFSBlock.getCapacity() * iNode.getBlockIDs().size();
    }

    @Override
    public long write(ByteBuffer byteBuffer, long bufferSize, long offsetInBytes) throws
            DNFSException.DNFSBlockStorageException
    {
        LOGGER.debug(String.format("Write %d bytes with with offset %d", bufferSize, offsetInBytes));

        long additionalCapacity = this.getSize() + bufferSize - this.getCapacity();
        if (additionalCapacity > 0){
            int numAdditionalBlocks = (int)Math.ceil((double) additionalCapacity / (DNFSBlock.getCapacity()));
            for (int i = 0; i < numAdditionalBlocks; i++) {
                this.addNewBlockToINode();
            }
        }

        byteBuffer.position(0);
        DNFSBlockCompositionOffset offset = seek(offsetInBytes);
        DNFSBlock block = offset.getBlock();
        long numBytesRemaining = bufferSize;

        numBytesRemaining -= block.write(byteBuffer, bufferSize, offset.getOffset());

        while (numBytesRemaining != 0){
            block = this.getNextBlock(block);
            if(block == null) {
                throw new DNFSException.DNFSBlockStorageException("Not enough blocks to write");
            }
            numBytesRemaining -= block.write(byteBuffer, numBytesRemaining, 0 );
        }
        return bufferSize;
    }

    
    private DNFSBlock getNextBlock(DNFSBlock block) throws DNFSException.DNFSBlockStorageException {
        
        List<Number160> blockIDs = this.getINode().getBlockIDs();
        int nextIndex = blockIDs.indexOf(block.getId()) + 1;

        if(nextIndex >= blockIDs.size()) {
            return null;
        }
        Number160 nextId = blockIDs.get(nextIndex);
        return this.getPeer().getBlock(nextId);
    }

    
    @Override
    public long read(ByteBuffer byteBuffer, long bytesToRead, long offset) throws DNFSException.DNFSBlockStorageException {

        DNFSBlockCompositionOffset blockOffset = this.seek(offset);
        if(blockOffset == null) {
            return 0;
        }
        
        DNFSBlock currentBlock = blockOffset.getBlock();
        long bytesReadTotal = currentBlock.read(byteBuffer, bytesToRead, blockOffset.getOffset());
        
        while(bytesReadTotal != bytesToRead){
            currentBlock = this.getNextBlock(currentBlock);
            if(currentBlock == null) {
               return bytesReadTotal;
            }
            bytesReadTotal += currentBlock.read(byteBuffer, bytesToRead-bytesReadTotal, 0);
        }
        return bytesReadTotal;

    }


    @Override
    public long truncate(long offsetInBytes) throws DNFSException.DNFSBlockStorageException {
        
        DNFSBlockCompositionOffset offset = this.seek((int) offsetInBytes);
        if(offset == null) {
            return 0;
        }
        
        long bytesTruncated = offset.getBlock().truncate(offset.getOffset());

        int indexOfBlock = this.getINode().getBlockIDs().indexOf(offset.getBlock().getId()) + 1;
        if(this.getINode().getBlockIDs().size() == indexOfBlock) {
            return bytesTruncated;
        }

        List<Number160> blocksToDelete = this.getINode().getBlockIDs().subList(indexOfBlock, this.getINode().getNumBlocks());
        this.getINode().removeBlocks(blocksToDelete);

        for (Number160 number160 : blocksToDelete) {
            this.getPeer().deleteBlock(number160);
        }

        return bytesTruncated;
    }

    
    @Override
    public long append(ByteBuffer byteBuffer, long bufferSize) throws DNFSException.DNFSBlockStorageException {
        return this.write(byteBuffer, bufferSize, this.getSize());
    }

    
    @Override
    public void delete() throws DNFSException.DNFSBlockStorageException {
        for (Number160 blockID : this.getINode().getBlockIDs()) {
           this.getINode().removeBlockID(blockID);
           this.getPeer().deleteBlock(blockID);
        }
    }


    private DNFSBlockCompositionOffset seek(final long offset) throws DNFSException.DNFSBlockStorageException{

        final long blockCapacity = DNFSBlock.getCapacity();
        int blockIndex = (int) Math.floor((float) offset / blockCapacity);
        int blockOffset = (int) (offset % blockCapacity);
        
        List<Number160> blockIDs = this.getINode().getBlockIDs();

        if(blockIndex >= blockIDs.size()) {
            return null;
        }

        Number160 blockID = blockIDs.get(blockIndex);
        DNFSBlock block = this.getPeer().getBlock(blockID);

        return new DNFSBlockCompositionOffset(block, blockOffset);
    }


    private DNFSBlock addNewBlockToINode() throws DNFSException.DNFSBlockStorageException{
        DNFSBlock newBlock = this.getPeer().createBlock();
        this.getINode().addBlock(newBlock);
//        TODO: Why do I have to call this, should be done automatically
        try {
            this.getPeer().updateINode(this.getINode());
        } catch (DNFSException e) {
            throw new DNFSException.DNFSBlockStorageException("", e);
        }
        return newBlock;

    }


    static class DNFSBlockCompositionOffset {
        private DNFSBlock block;
        private long offset;

        public DNFSBlockCompositionOffset(DNFSBlock block, long offset) {
            this.block = block;
            this.offset = offset;
        }

        public DNFSBlock getBlock() {
            return block;
        }

        public long getOffset() {
            return offset;
        }
    }

}
