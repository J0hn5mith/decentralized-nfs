package ch.uzh.csg.p2p.group_1.network;

import ch.uzh.csg.p2p.group_1.DNFSException;

import java.nio.ByteBuffer;

/**
 * Created by janmeier on 14.05.15.
 */
public interface DNFSIBlockComposition {


    public long size() throws DNFSException.DNFSBlockStorageException;
    public long write(ByteBuffer byteBuffer, final long bufferSize, final long offset) throws DNFSException.DNFSBlockStorageException;
    public long read(final ByteBuffer byteBuffer, long bytesToRead, final long offset) throws DNFSException.DNFSBlockStorageException;
    public long offset(final long offset);
}
