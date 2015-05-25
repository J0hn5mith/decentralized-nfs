package ch.uzh.csg.p2p.group_1.storage.interfaces;

import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import net.tomp2p.peers.Number160;

/**
 * Created by janmeier on 16.04.15.
 */
public interface DNFSIiNodeStorage {

    public DNFSIiNode createINode() throws DNFSException;
    public DNFSIiNode getINode(Number160 iNodeID) throws DNFSException;
    public void deleteINode(Number160 iNodeID) throws DNFSException;
    public void updateINode(DNFSIiNode iNode) throws DNFSException;

    public DNFSIiNode getRootINode() throws DNFSException;
    public DNFSIiNode createRootINode() throws DNFSException;
}
