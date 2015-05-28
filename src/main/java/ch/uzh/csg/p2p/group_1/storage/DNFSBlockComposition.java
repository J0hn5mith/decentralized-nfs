package ch.uzh.csg.p2p.group_1.storage;

/**
 * Created by janmeier on 14.05.15.
 */

import ch.uzh.csg.p2p.group_1.Main;
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
    final private static Logger LOGGER = Logger.getLogger(DNFSBlockComposition.class.getName());

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
        LOGGER.setLevel(Main.LOGGER_LEVEL);
    }

    @Override
    public long getSize() throws DNFSException.DNFSBlockStorageException {
        long totalSize = 0;
        for (Number160 blockID : iNode.getBlockIDs()) {
            DNFSBlock block = this.peer.getBlock(blockID);
            totalSize += block.getSize();
        }
        return totalSize;
//        List<Number160> blockIds = this.getINode().getBlockIDs();
//        if(blockIds.size() == 0){
//            return 0;
//        }
//        DNFSBlock lastBlock = this.getPeer().getBlock(blockIds.get(blockIds.size()-1));
//        return this.getCapacity() - lastBlock.getSize();
    }

    public long getCapacity() {
        return DNFSBlock.getCapacity() * iNode.getBlockIDs().size();
    }

    @Override
    public long write(ByteBuffer byteBuffer, long bufferSize, long offsetInBytes) throws
            DNFSException.DNFSBlockStorageException {
        LOGGER.debug(String.format("Write %d bytes with with offset %d", bufferSize, offsetInBytes));
//        // Create blocks which are missing until offset.
//        // start writing at the last block
//        // Write until no data left
//        DNFSBlockCompositionOffset offset = null;
//        if(this.getSize() < offsetInBytes){
//            LOGGER.warn("Writing with an offset larger then the total size.");
//            offset = fillUntil(offsetInBytes);
//        }
//
//        if(offset == null){
//            offset = this.seek(offsetInBytes);
//        }
//
//        long bytesWritten = 0;
//        byteBuffer.position(0);
//
//        DNFSBlock currentBlock = null;
//        if(offset != null){
//            bytesWritten += offset.getBlock().write(byteBuffer, bufferSize, offset.getOffset());
//            currentBlock = offset.getBlock();
//        }
//
//        while (bytesWritten != bufferSize){
//            if(currentBlock != null){
//                currentBlock = this.getNextBlock(currentBlock.getId());
//            }
//            else{
//                currentBlock = this.getNextBlock(null);
//            }
//            long numBytesLeft = bufferSize - bytesWritten;
//            bytesWritten += currentBlock.write(byteBuffer, numBytesLeft, 0);
//        }
//
//        return bytesWritten;

        long additionalCapacity = this.getSize() + bufferSize - this.getCapacity();

        if (additionalCapacity > 0) {
            int numAdditionalBlocks = (int) Math.ceil((double) additionalCapacity / (DNFSBlock.getCapacity()));
            for (int i = 0; i < numAdditionalBlocks; i++) {
                this.addNewBlockToINode();
            }
        }

        DNFSBlockCompositionOffset offset = seek(offsetInBytes);
        DNFSBlock block = offset.getBlock();
        long numBytesRemaining = bufferSize;

        numBytesRemaining -= block.write(byteBuffer, bufferSize, offset.getOffset());

        while (numBytesRemaining != 0) {
            block = this.getNextBlock(block.getId());
            if (block == null) {
                throw new DNFSException.DNFSBlockStorageException("Not enough blocks to write");
            }
            numBytesRemaining -= block.write(byteBuffer, numBytesRemaining, 0);
        }
        return bufferSize;
    }

    private DNFSBlockCompositionOffset fillUntil(long position) throws DNFSException.DNFSBlockStorageException {
        List<Number160> blockIds = this.getINode().getBlockIDs();
        DNFSBlock lastBlock = this.getPeer().getBlock(blockIds.get(blockIds.size() - 1));

        long numMissingBytes = position - this.getSize();

        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[(int) DNFSBlock.getCapacity()]);
        byteBuffer.position(0);
        numMissingBytes -= lastBlock.append(byteBuffer, DNFSBlock.getCapacity());
        while (numMissingBytes != 0) {
            lastBlock = this.getNextBlock(lastBlock.getId());
            byteBuffer.position(0);
            numMissingBytes -= lastBlock.append(byteBuffer, DNFSBlock.getCapacity());
        }

        return new DNFSBlockCompositionOffset(lastBlock, lastBlock.getSize());
    }


    private DNFSBlock getNextBlock(Number160 blockId) throws DNFSException.DNFSBlockStorageException {

        List<Number160> blockIDs = this.getINode().getBlockIDs();

        int nextIndex = blockIDs.indexOf(blockId) + 1; // If there is no block this works as well

        if (nextIndex >= blockIDs.size()) {
            return null;
        }
        if (nextIndex == blockIDs.size()) {
            return this.addNewBlockToINode();
        }

        Number160 nextId = blockIDs.get(nextIndex);
        return this.getPeer().getBlock(nextId);
    }


    @Override
    public long read(ByteBuffer byteBuffer, long bytesToRead, long offset) throws DNFSException.DNFSBlockStorageException {

        DNFSBlockCompositionOffset blockOffset = this.seek(offset);
        if (blockOffset == null) {
            return 0;
        }

        DNFSBlock currentBlock = blockOffset.getBlock();
        long bytesReadTotal = currentBlock.read(byteBuffer, bytesToRead, blockOffset.getOffset());

        while (bytesReadTotal != bytesToRead) {
            currentBlock = this.getNextBlock(currentBlock.getId());
            if (currentBlock == null) {
                return bytesReadTotal;
            }
            bytesReadTotal += currentBlock.read(byteBuffer, bytesToRead - bytesReadTotal, 0);
        }
        return bytesReadTotal;

    }


    @Override
    public long truncate(long offsetInBytes) throws DNFSException.DNFSBlockStorageException {

        DNFSBlockCompositionOffset offset = this.seek((int) offsetInBytes);
        if (offset == null) {
            return 0;
        }

        long bytesTruncated = offset.getBlock().truncate(offset.getOffset());

        int indexOfBlock = this.getINode().getBlockIDs().indexOf(offset.getBlock().getId()) + 1;
        if (this.getINode().getBlockIDs().size() == indexOfBlock) {
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


    private DNFSBlockCompositionOffset seek(final long offset) throws DNFSException.DNFSBlockStorageException {

        final long blockCapacity = DNFSBlock.getCapacity();
        int blockIndex = (int) Math.floor((float) offset / blockCapacity);
        int blockOffset = (int) (offset % blockCapacity);

        List<Number160> blockIDs = this.getINode().getBlockIDs();

        if (blockIndex >= blockIDs.size()) {
            LOGGER.warn(String.format("Offset is too largs for this block composition. blockIndex: %d; numBlocks: %d", blockIndex, blockIDs.size()));
            return null;
        }

        Number160 blockID = blockIDs.get(blockIndex);
        DNFSBlock block = this.getPeer().getBlock(blockID);

        return new DNFSBlockCompositionOffset(block, blockOffset);
    }


    private DNFSBlock addNewBlockToINode() throws DNFSException.DNFSBlockStorageException {
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
