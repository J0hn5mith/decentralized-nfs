package ch.uzh.csg.p2p.group_1.network;

/**
 * Created by janmeier on 14.05.15.
 */

import ch.uzh.csg.p2p.group_1.*;
import net.tomp2p.peers.Number160;
import net.tomp2p.utils.Pair;

import java.nio.ByteBuffer;

/**
 * Wrapper class for iNodes so DNFSFile and DNFSFolder don't have to bother with
 * getting/ and writing blocks
 */
public class DNFSBlockComposition implements DNFSIBlockComposition {

    DNFSiNode iNode;
    DNFSIBlockStorage blockStorage;

    public DNFSIBlockStorage getBlockStorage() {
        return blockStorage;
    }

    public DNFSiNode getINode() {
        return iNode;
    }

    public DNFSBlockComposition(DNFSiNode iNode, DNFSIBlockStorage blockStorage) {
        this.iNode = iNode;
        this.blockStorage = blockStorage;
    }

    @Override
    public long size() throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {
        long totalSize = 0;
        for (Number160 blockID : iNode.getBlockIDs()) {
            DNFSBlock block = this.blockStorage.getBlock(blockID);
            totalSize += block.getSize();
        }
        return totalSize;
    }

    public long getCapacity(){

        long maxCapacity = 0;
        for (Number160 blockID : iNode.getBlockIDs()) {
            maxCapacity += DNFSBlock.BLOCK_SIZE;
        }

        return maxCapacity;
    }

    @Override
    public long write(ByteBuffer byteBuffer, long bufferSize, long offsetInBytes) throws
            DNFSException.DNFSBlockStorageException,
            DNFSException.DNFSNetworkNoConnection
    {

        long additionalCapacity = this.size() + bufferSize - this.getCapacity();
        if (additionalCapacity > 0){
            int numAdditionalBlocks = (int)Math.ceil((double)additionalCapacity/(DNFSBlock.getCapacity()/2));
            for (int i = 0; i < numAdditionalBlocks; i++) {
                DNFSBlock block = this.getBlockStorage().createBlock();
                this.getINode().addBlock(block);
            }
        }
        DNFSBlockCompositionOffset offset = findPosition(offsetInBytes);
        DNFSBlock block = offset.getBlock();

//        TODO: Handle the case when bufferSize is larger than half a block
        if (bufferSize > offset.getBlock().getFreeSize()){
            this.splitBlock(block);
        }

        int bytesWritten = block.write(byteBuffer, bufferSize, offsetInBytes);
        return bytesWritten;
    }

    @Override
    public long read(ByteBuffer byteBuffer, long bytesToRead, long offset) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {

        // First block
//        TODO: Double check this code!!!
        long summedUpSize = 0;
        long bytesNotRead = bytesToRead;
        for (Number160 blockId : this.getINode().getBlockIDs()) {
            DNFSBlock block = this.getBlockStorage().getBlock(blockId);

            long blockSize = block.getSize();
            if (summedUpSize + blockSize > offset){
                long bytesRead = block.read(byteBuffer, bytesNotRead, Math.max(0, offset - summedUpSize));
                summedUpSize += bytesRead;
            }
            else{
                summedUpSize += blockSize;

            }


            if (summedUpSize > offset + bytesToRead){
                break;
            }
        }

        return summedUpSize - offset;
    }

    @Override
    public long truncate(long offset) {
        return 0;
    }

    @Override
    public long append(ByteBuffer byteBuffer, long bufferSize) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {
        return this.write(byteBuffer, bufferSize, this.size());
    }


    private DNFSBlockCompositionOffset findPosition(final long offset) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {

        long bytesLeft = offset;
        for (Number160 blockID : iNode.getBlockIDs()) {
            DNFSBlock block = this.blockStorage.getBlock(blockID);
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
            DNFSException.DNFSNetworkNoConnection
    {
        DNFSBlock newBlock = this.getBlockStorage().createBlock();

        long halfSize = block.getSize()/2;
        ByteBuffer contentNewBlock = ByteBuffer.wrap(new byte[(int) halfSize]);
        block.read(contentNewBlock, halfSize, halfSize);
        block.truncate(halfSize);
        newBlock.write(contentNewBlock, halfSize, halfSize);
        this.getINode().addBlock(newBlock, block);
        return block;

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
