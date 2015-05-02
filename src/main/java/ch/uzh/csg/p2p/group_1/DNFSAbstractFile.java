/**
 * Created by janmeier on 10.04.15.
 */

package ch.uzh.csg.p2p.group_1;


public abstract class DNFSAbstractFile extends DNFSFileSystemEntry {


    public DNFSIPeer getPeer() {
        return peer;
    }

    public void setPeer(DNFSIPeer peer) {
        this.peer = peer;
    }

    private DNFSIPeer peer;


    DNFSAbstractFile(DNFSiNode iNode, DNFSIPeer peer){
        super(iNode);
        this.peer = peer;
    }

}
