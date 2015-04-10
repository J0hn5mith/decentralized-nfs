/**
 * Created by janmeier on 10.04.15.
 */

package ch.uzh.csg.p2p.group_1;


public abstract class DNFSAbstractFile {

    private DNFSiNode iNode;

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
    
}
