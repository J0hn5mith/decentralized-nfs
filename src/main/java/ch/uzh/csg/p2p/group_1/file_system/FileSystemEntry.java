package ch.uzh.csg.p2p.group_1.file_system;

import ch.uzh.csg.p2p.group_1.storage.interfaces.IStorage;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.storage.DNFSBlockComposition;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;

/**
 * Created by janmeier on 02.05.15.
 */
public abstract class FileSystemEntry {


    private final DNFSBlockComposition blockComposition;
    private DNFSIiNode iNode;
    private IStorage storage;
    

    public FileSystemEntry(DNFSIiNode iNode, IStorage storage) {
        this.iNode = iNode;
        this.storage = storage;
        this.blockComposition = new DNFSBlockComposition(iNode, storage);
    }


    public DNFSIiNode getINode() {
        return iNode;
    }

    
    public IStorage getStorage() {
        return storage;
    }

    
    public DNFSBlockComposition getBlockComposition() {
        return blockComposition;
    }

    
    abstract public void delete() throws DNFSException;

}
