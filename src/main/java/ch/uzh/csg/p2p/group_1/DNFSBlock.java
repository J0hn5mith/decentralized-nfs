package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.network.DNFSIBlock;
import net.tomp2p.peers.Number160;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

/**
 * Created by janmeier on 10.04.15.
 */
public class DNFSBlock implements Serializable, DNFSIBlock {
    final private static Logger LOGGER = Logger.getLogger(DNFSBlock.class);
    private static final long serialVersionUID = 2098774660703813030L;
    public static int BLOCK_SIZE = 100000;

    Number160 id;
    DNFSIBlockStorage blockStorage;
    private ByteBuffer data;

    public DNFSBlock(Number160 id, DNFSIBlockStorage blockStorage) {
        this.id = id;
        this.blockStorage = blockStorage;
        this.data = ByteBuffer.allocate(0);
    }
    
    
    public DNFSBlock(Number160 id, byte[] byteArray, DNFSIBlockStorage blockStorage) {
        this.id = id;
        this.data = ByteBuffer.wrap(byteArray);
        this.blockStorage = blockStorage;
    }

    static public long getCapacity(){
        return BLOCK_SIZE;
    }
    

    public Number160 getId() {
        return id;
    }

    public void setId(Number160 id) {
        this.id = id;
    }

    public long getSize() {
        return this.data.array().length;
    }

    public long getFreeSize(){
        return BLOCK_SIZE - this.getSize();

    }

    public DNFSIBlockStorage getBlockStorage() {
        return blockStorage;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(data.array());
    }
    
    
    public byte[] getByteArray() {
        return this.data.array();
    }
    

    public long append(ByteBuffer buffer, final long bufferSize) throws DNFSException.DNFSNetworkNoConnection {
        int writeOffset = this.data.array().length;
        return this.write(buffer, bufferSize, writeOffset);
    }

    @Override
    public void delete() throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {
        this.blockStorage.deleteBlock(this.getId());
    }


    public long write(ByteBuffer buffer, final long bufferSize, final long offset) throws DNFSException.DNFSNetworkNoConnection {

        final int maxWriteIndex = (int) (offset + bufferSize);
        final byte[] bytesToWrite = new byte[(int) bufferSize];
        if (maxWriteIndex > data.capacity()) {
            final ByteBuffer newContents = ByteBuffer.allocate(maxWriteIndex);
            newContents.put(this.data);
            this.data = newContents;
        }

        buffer.get(bytesToWrite, 0, (int) bufferSize);
        this.data.position((int) offset);
        this.data.put(bytesToWrite);
        this.data.position(0);

        try {
            this.getBlockStorage().updateBlock(this);
        } catch (DNFSException.DNFSBlockStorageException e) {
            LOGGER.error("Serious probelm. Could not update block. Updated is ignored.", e);
        }
        return (int) bufferSize;
    }

    
    public long read(final ByteBuffer byteBuffer, long bytesToRead, final long offset) {
        bytesToRead = Math.min(this.data.capacity() - offset, bytesToRead);

        byteBuffer.position(0);
        byteBuffer.put(this.data.array(), (int) offset, (int) bytesToRead);
        return (int) bytesToRead;
    }

    
    public long truncate(final long offset) {
        if (offset < this.data.capacity()) {
            // Need to create a new, smaller buffer
            final ByteBuffer newContents = ByteBuffer.allocate((int) offset);
            final byte[] bytesRead = new byte[(int) offset];
            this.data.get(bytesRead);
            newContents.put(bytesRead);
            this.data = newContents;
        }

        return (int)offset;
    }

}
