package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

/**
 * Created by janmeier on 16.04.15.
 */
public interface DNFSIiNodeStorage {

    public DNFSiNode getINode(Number160 iNodeID) throws DNFSException;
    public DNFSiNode getNewINode();
    public DNFSiNode deleteINode(Number160 iNodeID);

    public DNFSiNode getRootINode() throws DNFSException;
}
