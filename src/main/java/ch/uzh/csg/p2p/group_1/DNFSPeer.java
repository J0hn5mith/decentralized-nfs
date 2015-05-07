package ch.uzh.csg.p2p.group_1;

import java.io.IOException;

import net.tomp2p.peers.Number160;

public class DNFSPeer implements DNFSIPeer {
    
    
    public void setup(int port) {
        try {
            DNFSNetwork.setup(port);
            DNFSNetwork.start();
        } catch(Exception e) {
            System.err.println(e.toString());
        }
    }
    

    @Override
    public DNFSBlock createBlock() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DNFSBlock getBlock(Number160 id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateBlock(DNFSBlock block) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteBlock(Number160 id) {
        // TODO Auto-generated method stub

    }

    @Override
    public DNFSiNode createINode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DNFSiNode getINode(Number160 iNodeID) throws DNFSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteINode(Number160 iNodeID) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateINode(DNFSiNode iNode) {
        // TODO Auto-generated method stub

    }

    @Override
    public DNFSiNode getRootINode() throws DNFSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DNFSiNode createRootINode() throws DNFSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setUp() throws IOException {
        // TODO Auto-generated method stub

    }

}
