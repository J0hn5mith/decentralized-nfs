package ch.uzh.csg.p2p.group_1.network;

import ch.uzh.csg.p2p.group_1.DNFSException;
import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkNoConnection;

import java.nio.ByteBuffer;

/**
 * Created by janmeier on 14.05.15.
 */
public interface DNFSIBlockComposition {


    public long size() throws DNFSException.DNFSBlockStorageException, DNFSNetworkNoConnection;
    public long write(ByteBuffer byteBuffer, final long bufferSize, final long offset) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection;
    public long read(final ByteBuffer byteBuffer, long bytesToRead, final long offset) throws DNFSException.DNFSBlockStorageException, DNFSNetworkNoConnection;
    public long truncate(final long offset);
}
