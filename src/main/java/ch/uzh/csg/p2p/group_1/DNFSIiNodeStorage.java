package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

/**
 * Created by janmeier on 16.04.15.
 */
public interface DNFSIiNodeStorage {

    public DNFSiNode createINode() throws DNFSException;
    public DNFSiNode getINode(Number160 iNodeID) throws DNFSException;
    public void deleteINode(Number160 iNodeID) throws DNFSException;
    public void updateINode(DNFSiNode iNode) throws DNFSException;

    public DNFSiNode getRootINode() throws DNFSException;
    public DNFSiNode createRootINode() throws DNFSException;
}
