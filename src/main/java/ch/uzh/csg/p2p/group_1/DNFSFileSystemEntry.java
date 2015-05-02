package ch.uzh.csg.p2p.group_1;

/**
 * Created by janmeier on 02.05.15.
 */
public abstract class DNFSFileSystemEntry {
    private DNFSiNode iNode;

    public DNFSFileSystemEntry(DNFSiNode iNode) {
        this.iNode = iNode;
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

}
