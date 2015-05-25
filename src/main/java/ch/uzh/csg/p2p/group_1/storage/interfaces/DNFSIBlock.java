package ch.uzh.csg.p2p.group_1.storage.interfaces;

import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;

import java.nio.ByteBuffer;

/**
 * Created by janmeier on 14.05.15.
 */
public interface DNFSIBlock {


    public long getSize() throws DNFSException.DNFSBlockStorageException;
    public long write(ByteBuffer byteBuffer, final long bufferSize, final long offset) throws DNFSException.DNFSBlockStorageException;
    public long read(final ByteBuffer byteBuffer, long bytesToRead, final long offset) throws DNFSException.DNFSBlockStorageException;
     public long truncate(final long offset) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit;
    public long append(ByteBuffer byteBuffer, final long bufferSize) throws DNFSException.DNFSBlockStorageException;
    public void delete() throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit;
}

