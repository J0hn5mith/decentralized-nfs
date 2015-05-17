package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.network.DNFSBlockComposition;

/**
 * Created by janmeier on 02.05.15.
 */
public abstract class DNFSFileSystemEntry {


    private final DNFSBlockComposition blockComposition;
    private DNFSIiNode iNode;
    private DNFSIPeer peer;
    

    /**
     * 
     * @param iNode
     */
    public DNFSFileSystemEntry(DNFSIiNode iNode, DNFSIPeer peer) {
        this.iNode = iNode;
        this.peer = peer;
        this.blockComposition = new DNFSBlockComposition(iNode, peer);
    }

    
    /**
     *
     * @return
     */
    public DNFSIiNode getINode() {
        return iNode;
    }


    /**
     * 
     * @return
     */
    public DNFSIPeer getPeer() {
        return peer;
    }

    public DNFSBlockComposition getBlockComposition() {
        return blockComposition;
    }
    //    /**
//     *
//     * @param peer
//     */
//    public void setPeer(DNFSIPeer peer) {
//        this.peer = peer;
//    }

    
    /**
     * 
     * @return
     */
    abstract public int delete();

}
