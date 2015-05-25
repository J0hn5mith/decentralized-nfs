package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import java.nio.ByteBuffer;

/**
 * Created by janmeier on 02.04.15.
 */


public class DNFSFile extends DNFSFileSystemEntry {

    /**
     * 
     * @param iNode
     */
    DNFSFile(DNFSIiNode iNode, IStorage peer){
        super(iNode, peer);
    }

    
    public static DNFSFile createNew(IStorage peer) throws DNFSException {
        return new DNFSFile(peer.createINode(), peer);
    }
    
    
    public static DNFSFile getExisting(DNFSIiNode iNode, IStorage peer){
        DNFSFile file = new DNFSFile(iNode, peer);
        return file;
    }


    public int write(final ByteBuffer buffer, final long bufSize, final long writeOffset) throws
            DNFSException.DNFSBlockStorageException,
            DNFSException.DNFSNetworkNotInit
    {
        int bytesWritten = (int) this.getBlockComposition().write(buffer, bufSize, writeOffset);

        this.getINode().setSize((int) this.getBlockComposition().getSize());
        return bytesWritten;
    }

    
    public int read(final ByteBuffer buffer, final long bytesToRead, final long offset) throws
            DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        return (int) this.getBlockComposition().read(buffer, bytesToRead, offset);

    }

    @Override
    public int delete() {
        return 0;
    }

    public long truncate(long offset) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSBlockStorageException {
        return this.getBlockComposition().truncate(offset);
    }

}
