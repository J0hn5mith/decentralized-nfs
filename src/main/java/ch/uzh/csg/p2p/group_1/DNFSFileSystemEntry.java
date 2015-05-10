package ch.uzh.csg.p2p.group_1;

/**
 * Created by janmeier on 02.05.15.
 */
public abstract class DNFSFileSystemEntry {
    
    
    private DNFSiNode iNode;
    private DNFSIPeer peer;
    

    /**
     * 
     * @param iNode
     */
    public DNFSFileSystemEntry(DNFSiNode iNode, DNFSIPeer peer) {
        this.iNode = iNode;
        this.peer = peer;
    }

    
    /**
     *
     * @return
     */
    public DNFSiNode getINode() {
        return iNode;
    }

    
    /**
     *
     * @param iNode
     */
    public void setiNode(DNFSiNode iNode) {
        this.iNode = iNode;
    }
    
    
    /**
     * 
     * @return
     */
    public DNFSIPeer getPeer() {
        return peer;
    }

    
    /**
     * 
     * @param peer
     */
    public void setPeer(DNFSIPeer peer) {
        this.peer = peer;
    }

    
    /**
     * 
     * @return
     */
    abstract public int delete();

}
