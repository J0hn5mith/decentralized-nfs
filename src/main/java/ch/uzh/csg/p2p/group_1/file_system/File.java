package ch.uzh.csg.p2p.group_1.file_system;

import ch.uzh.csg.p2p.group_1.storage.interfaces.IStorage;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;

import java.nio.ByteBuffer;

/**
 * Created by janmeier on 02.04.15.
 */


public class File extends FileSystemEntry {

    /**
     * 
     * @param iNode
     */
    File(DNFSIiNode iNode, IStorage storage) {
        super(iNode, storage);
    }

    
    public static File createNew(IStorage storage) throws DNFSException.INodeStorageException {
        return new File(storage.createINode(), storage);
    }
    
    
    public static File getExisting(DNFSIiNode iNode, IStorage storage) {
        File file = new File(iNode, storage);
        return file;
    }


    public int write(final ByteBuffer buffer, final long bufSize, final long writeOffset) throws
            DNFSException.DNFSBlockStorageException,
            DNFSException.DNFSNetworkNotInit {
        int bytesWritten = (int) this.getBlockComposition().write(buffer, bufSize, writeOffset);

        this.getINode().setSize((int) this.getBlockComposition().getSize());
        return bytesWritten;
    }

    
    public int read(final ByteBuffer buffer, final long bytesToRead, final long offset) throws
            DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNotInit {
        return (int) this.getBlockComposition().read(buffer, bytesToRead, offset);

    }

    
    @Override
    public void delete() {
    }

    
    public long truncate(long offset) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSBlockStorageException {
        return this.getBlockComposition().truncate(offset);
    }

}
