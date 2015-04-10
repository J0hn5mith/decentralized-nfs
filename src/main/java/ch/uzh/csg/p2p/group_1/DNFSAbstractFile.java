/**
 * Created by janmeier on 10.04.15.
 */

package ch.uzh.csg.p2p.group_1;


public abstract class DNFSAbstractFile {

    private DNFSiNode iNode;
    private DNFSPathResolver pathResolver;


    DNFSAbstractFile(DNFSiNode iNode, DNFSPathResolver pathResolver){
        this.iNode = iNode;
        this.pathResolver = pathResolver;

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
    
    public DNFSPathResolver getPathResolver() {
        return pathResolver;
    }

    public void setPathResolver(DNFSPathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }
    
}
