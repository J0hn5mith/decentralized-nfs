package ch.uzh.csg.p2p.group_1.network;

/**
 * Created by janmeier on 14.05.15.
 */

import ch.uzh.csg.p2p.group_1.*;
import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
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
    DNFSIPeer peer;

    public DNFSIPeer getPeer() {
        return peer;
    }

    public DNFSIiNode getINode() {
        return iNode;
    }

    public DNFSBlockComposition(DNFSIiNode iNode, DNFSIPeer peer) {
        this.iNode = iNode;
        this.peer = peer;
        LOGGER.setLevel(Level.DEBUG);
    }

    @Override
    public long getSize() throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        long totalSize = 0;
        for (Number160 blockID : iNode.getBlockIDs()) {
            DNFSBlock block = this.peer.getBlock(blockID);
            totalSize += block.getSize();
        }
        return totalSize;
    }

    public long getCapacity(){
        return DNFSBlock.BLOCK_SIZE * iNode.getBlockIDs().size();
    }

    @Override
    public long write(ByteBuffer byteBuffer, long bufferSize, long offsetInBytes) throws
            DNFSException.DNFSBlockStorageException,
            DNFSException.DNFSNetworkNotInit
    {
        LOGGER.debug(String.format("Write %d bytes with with offset %d", bufferSize, offsetInBytes));

        long additionalCapacity = this.getSize() + bufferSize - this.getCapacity();
        if (additionalCapacity > 0){
            int numAdditionalBlocks = (int)Math.ceil((double)additionalCapacity/(DNFSBlock.getMaxCapacity()));
            for (int i = 0; i < numAdditionalBlocks; i++) {
                DNFSBlock block = this.addNewBlockToINode();
            }
        }

        byteBuffer.position(0);
        DNFSBlockCompositionOffset offset = seek(offsetInBytes);
        DNFSBlock block = offset.getBlock();
        long numBytesRemaining = bufferSize;

        numBytesRemaining -= block.write(byteBuffer, bufferSize, offset.getOffset());
        while (numBytesRemaining != 0){
            block = this.getNextBlock(block);
            numBytesRemaining -= block.write(byteBuffer, numBytesRemaining, 0 );
        }
        return bufferSize;
    }

    private DNFSBlock getNextBlock(DNFSBlock block) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        int nextIndex = this.getINode().getBlockIDs().indexOf(block.getId()) + 1;
        return this.getPeer().getBlock(this.getINode().getBlockIDs().get(nextIndex));
    }

    @Override
    public long read(ByteBuffer byteBuffer, long bytesToRead, long offset) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {

        // First block
//        TODO: Double check this code!!!
        long summedUpSize = 0;
        long bytesLeft = bytesToRead;
        long bytesReadTotal = 0;
        for (Number160 blockId : this.getINode().getBlockIDs()) {
            DNFSBlock block = this.getPeer().getBlock(blockId);

            long blockSize = block.getSize();
            if (summedUpSize + blockSize > offset){
                long bytesRead = block.read(byteBuffer, bytesLeft, Math.max(0, offset - summedUpSize));
                summedUpSize += bytesRead;
                bytesReadTotal += bytesRead;
                bytesLeft -= bytesRead;
            }
            else{
                summedUpSize += blockSize;
            }
            if (summedUpSize > offset + bytesToRead){
                break;
            }
        }
        return bytesReadTotal;

    }


    @Override
    public long truncate(long offsetInBytes) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        DNFSBlockCompositionOffset offset = this.seek((int)offsetInBytes);
        long bytesTruncated = offset.getBlock().truncate(offset.getOffset());

        int indexOfBlock = this.getINode().getBlockIDs().indexOf(offset.getBlock().getId()) + 1;
        if(this.getINode().getBlockIDs().size() == indexOfBlock){
            return 0;
        }

        List<Number160> blocksToDelete = this.getINode().getBlockIDs().subList(indexOfBlock, this.getINode().getNumBlocks());
        this.getINode().removeBlocks(blocksToDelete);

        for (Number160 number160 : blocksToDelete) {
            this.getPeer().deleteBlock(number160);
        }


        return 0;
    }

    @Override
    public long append(ByteBuffer byteBuffer, long bufferSize) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        return this.write(byteBuffer, bufferSize, this.getSize());
    }

    @Override
    public void delete() throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        for (Number160 blockID : this.getINode().getBlockIDs()) {
           this.getINode().removeBlockID(blockID);
           this.getPeer().deleteBlock(blockID);
        }
    }


    private DNFSBlockCompositionOffset seek(final long offset) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {

        long bytesLeft = offset;
        for (Number160 blockID : iNode.getBlockIDs()) {
            DNFSBlock block = this.peer.getBlock(blockID);
            long blockSize = block.getSize();
            if (bytesLeft - blockSize <= 0){
                return new DNFSBlockCompositionOffset(block, bytesLeft);
            }
            bytesLeft -= blockSize;
        }
        return null;
    }

    private DNFSBlock splitBlock(DNFSBlock block) throws
            DNFSException.DNFSBlockStorageException,
            DNFSException.DNFSNetworkNotInit
    {
        DNFSBlock newBlock = this.getPeer().createBlock();

        long halfSize = block.getSize()/2;
        ByteBuffer contentNewBlock = ByteBuffer.wrap(new byte[(int) halfSize]);
        block.read(contentNewBlock, halfSize, halfSize);
        block.truncate(halfSize);
        newBlock.write(contentNewBlock, halfSize, 0);
        this.getINode().addBlock(newBlock, block);
        return block;

    }

    private DNFSBlock addNewBlockToINode() throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        DNFSBlock newBlock = this.getPeer().createBlock();
        this.getINode().addBlock(newBlock);
//        TODO: Why do I have to call this, should be done automatically
        try {
            this.getPeer().updateINode(this.getINode());
        } catch (DNFSException e) {
            e.printStackTrace();
        }
        return newBlock;

    }


    static class DNFSBlockCompositionOffset{
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
