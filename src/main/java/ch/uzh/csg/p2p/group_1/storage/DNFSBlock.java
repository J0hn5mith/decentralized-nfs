package ch.uzh.csg.p2p.group_1.storage;

import ch.uzh.csg.p2p.group_1.Main;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIBlockStorage;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIBlock;
import net.tomp2p.peers.Number160;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Created by janmeier on 10.04.15.
 */
public class DNFSBlock implements Serializable, DNFSIBlock {
    final private static Logger LOGGER = Logger.getLogger(DNFSBlock.class);
    private static final long serialVersionUID = 2098774660703813030L;
    public static int BLOCK_SIZE = 16 * 65536;

    Number160 id;
    DNFSIBlockStorage blockStorage;
    protected ByteBuffer data;

    public DNFSBlock(Number160 id, DNFSIBlockStorage blockStorage) {
        this.id = id;
        this.blockStorage = blockStorage;
        this.data = ByteBuffer.allocate(0);
        LOGGER.setLevel(Main.LOGGER_LEVEL);
    }
    
    
    public DNFSBlock(Number160 id, byte[] byteArray, DNFSIBlockStorage blockStorage) {
        this.id = id;
        //System.out.println("BUFFER LENGTH WRAPPED: " + byteArray.length); // TODO
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


    public long append(ByteBuffer buffer, final long bufferSize){
        int writeOffset = this.data.array().length;
        return this.write(buffer, bufferSize, writeOffset);
    }

    @Override
    public void delete() throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        this.blockStorage.deleteBlock(this.getId());
    }


    public long write(ByteBuffer buffer, final long bufferSize, final long offset){
        int numBytesPossibleToWrite = (int) Math.min(bufferSize, (getCapacity() - offset));
        LOGGER.debug(String.format("Block is able to write %d bytes.", numBytesPossibleToWrite));
        final byte[] bytesToWrite = new byte[(int) numBytesPossibleToWrite];

        if (numBytesPossibleToWrite + offset > data.array().length) {
            final ByteBuffer newContents = ByteBuffer.allocate((int) (numBytesPossibleToWrite+offset));
            newContents.put(this.data);
            this.data = newContents;
        }

        buffer.get(bytesToWrite, 0, numBytesPossibleToWrite);
        this.data.position((int) offset);
        this.data.put(bytesToWrite);
        this.data.position(0);

        try {
            this.getBlockStorage().updateBlock(this);
        } catch (DNFSException.DNFSBlockStorageException e) {
            LOGGER.error("Serious problem. Could not update block. Updated is ignored.", e);
        }
        return (int) numBytesPossibleToWrite;
    }

    
    public long read(final ByteBuffer byteBuffer, long bytesToRead, final long offset) {
        //System.out.println("BYTESTOREAD: " + bytesToRead + "; data.capacity: " + this.data.capacity()); //TODO
        int maxBytesReadable = (int) Math.min(this.data.array().length - offset, bytesToRead);
        //System.out.println("MAXBYTESREADABLE:" + maxBytesReadable); // TODO
        byteBuffer.put(this.data.array(), (int) offset, maxBytesReadable);
        LOGGER.debug(String.format("Read %d bytes with with offset %d", maxBytesReadable, offset));
        return maxBytesReadable;
    }

    
    public long truncate(final long offset) {
        
        if (offset < this.data.capacity()) {
            final byte[] bytesRead = new byte[(int) offset];
            this.data.get(bytesRead);
            this.data = ByteBuffer.wrap(bytesRead);
        }
        
        try {
            this.getBlockStorage().updateBlock(this);
        } catch (DNFSException.DNFSBlockStorageException e) {
            LOGGER.error("Serious probelm. Could not update block. Updated is ignored.", e);
        }
        return (int)offset;
    }

}
