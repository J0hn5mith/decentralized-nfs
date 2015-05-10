package ch.uzh.csg.p2p.group_1;

import java.io.IOException;

import net.tomp2p.peers.Number160;
import org.apache.log4j.Logger;

public class DNFSPeer implements DNFSIPeer {
    final private static Logger LOGGER = Logger.getLogger(DNFSPeer.class.getName());

    
    private static final Number160 ROOT_INODE_KEY = Number160.createHash(0);
    

    @Override
    public DNFSBlock createBlock() throws DNFSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DNFSBlock getBlock(Number160 id) throws DNFSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateBlock(DNFSBlock block) throws DNFSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteBlock(Number160 id) throws DNFSException {
        // TODO Auto-generated method stub

    }

    @Override
    public DNFSiNode createINode() throws DNFSException {
        Number160 iNodeID = DNFSNetwork.getUniqueKey();
        DNFSiNode iNode = new DNFSiNode(iNodeID);
        Object data = (Object) iNode; // TODO do better serialization
        DNFSNetwork.put(iNodeID, data);
        return iNode;
    }

    @Override
    public DNFSiNode getINode(Number160 iNodeID) throws DNFSException {
        Object data = DNFSNetwork.get(iNodeID);
        DNFSiNode iNode = (DNFSiNode) data; // TODO do better serialization
        return iNode;
    }

    @Override
    public void deleteINode(Number160 iNodeID) throws DNFSException {
        DNFSNetwork.delete(iNodeID);
    }

    @Override
    public void updateINode(DNFSiNode iNode) throws DNFSException {
        DNFSNetwork.delete(iNode.id); // TODO: change this once we have vDHT
        Object data = (Object) iNode; // TODO do better serialization
        DNFSNetwork.put(iNode.id, data);
    }

    @Override
    public DNFSiNode getRootINode() throws DNFSException {
        return getINode(ROOT_INODE_KEY);
    }

    @Override
    public DNFSiNode createRootINode() throws DNFSException {
        DNFSiNode iNode = new DNFSiNode(ROOT_INODE_KEY);
        Object data = (Object) iNode; // TODO do better serialization
        DNFSNetwork.put(ROOT_INODE_KEY, data);
        LOGGER.info("Successfully create root iNode");
        return iNode;
    }

    @Override
    public void setUp() throws DNFSException {
    }

}
