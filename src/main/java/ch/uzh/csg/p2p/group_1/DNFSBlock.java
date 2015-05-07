package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

/**
 * Created by janmeier on 10.04.15.
 */
public class DNFSBlock implements Serializable {
    final private static Logger LOGGER = Logger.getLogger(DNFSFuseIntegrationCommented.class.getName());
    private static final long serialVersionUID = 2098774660703813030L;
    public static int BLOCK_SIZE = 100000;

    Number160 id;
    private ByteBuffer data;

    public DNFSBlock(Number160 id) {
        this.id = id;
        this.data = ByteBuffer.allocate(0);
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

    public InputStream getInputStream() {
        return new ByteArrayInputStream(data.array());
    }

    public int append(String appendString) {
        int writeOffset = this.data.array().length;
        int bufSize = appendString.getBytes().length;
        ByteBuffer buffer = ByteBuffer.wrap(appendString.getBytes());

        return this.write(buffer, bufSize, writeOffset);
    }

    public int write(ByteBuffer buffer, final long bufferSize, final long offset) {

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
        return (int) bufferSize;
    }

    public int read(final ByteBuffer byteBuffer, long bytesToRead, final long offset) {
        bytesToRead = Math.min(this.data.capacity() - offset, bytesToRead);

        byteBuffer.position(0);
        byteBuffer.put(this.data.array(), (int) offset, (int) bytesToRead);
        return (int) bytesToRead;
    }

    public int truncate(final long offset)
    {
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
