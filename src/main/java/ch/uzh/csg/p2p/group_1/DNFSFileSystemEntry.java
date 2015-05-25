package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.network.DNFSBlockComposition;

/**
 * Created by janmeier on 02.05.15.
 */
public abstract class DNFSFileSystemEntry {


    private final DNFSBlockComposition blockComposition;
    private DNFSIiNode iNode;
    private IStorage storage;
    

    public DNFSFileSystemEntry(DNFSIiNode iNode, IStorage storage) {
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

    
    abstract public int delete();

}
