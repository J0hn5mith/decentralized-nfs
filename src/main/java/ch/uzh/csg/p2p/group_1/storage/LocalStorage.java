package ch.uzh.csg.p2p.group_1.storage;

import ch.uzh.csg.p2p.group_1.storage.interfaces.IStorage;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSNetworkNotInit;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.file_system.Directory;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ch.uzh.csg.p2p.group_1.Settings;

/**
 * Created by janmeier on 16.04.15.
 */
public class LocalStorage implements IStorage {

    private Map<Number160, DNFSBlock> blocks;
    private Map<Number160, DNFSIiNode> iNodes;
    private DNFSIiNode rootINode;
    private Random random;

    public LocalStorage() {
        this.blocks = new HashMap<Number160, DNFSBlock>();
        this.iNodes = new HashMap<Number160, DNFSIiNode>();

        this.random = new Random();
    }

    @Override
    public DNFSIiNode createINode() {
        DNFSIiNode iNode = new DNFSiNode(this.getNewINodeID());
        this.iNodes.put(iNode.getId(), iNode);
        return iNode;
    }

    /**
     * @param iNodeID
     * @return
     * @throws ch.uzh.csg.p2p.group_1.exceptions.DNFSException
     */
    public DNFSIiNode getINode(Number160 iNodeID) throws DNFSException {
        DNFSIiNode node = this.iNodes.get(iNodeID);
        if (node == null){
            throw new DNFSException();
        }

        return node;
    }

    @Override
    public void deleteINode(Number160 iNodeID) {

    }


    @Override
    public void updateINode(DNFSIiNode iNode) {

    }

    /**
     * @return
     * @throws DNFSException
     */
    public DNFSIiNode getRootINode() throws DNFSException {
        return this.rootINode;
    }

    @Override
    public DNFSIiNode createRootINode() throws DNFSException {
        return this.rootINode;
    }

    public Number160 getNewINodeID() {
        int randomInt = this.random.nextInt();
        Number160 id = Number160.createHash(randomInt);
        return id;
    }

    public Number160 getNewBlockID() {
        return Number160.createHash(this.random.nextInt());
    }

    @Override
    public DNFSBlock createBlock() {
        DNFSBlock block = new DNFSBlock(this.getNewBlockID(), this);
        this.blocks.put(block.getId(), block);
        return block;
    }

    public DNFSBlock getBlock(Number160 id) {
        return this.blocks.get(id);
    }

    @Override
    public void updateBlock(DNFSBlock block) {

    }

    @Override
    public void deleteBlock(Number160 id) {

    }


    public void setUp(Settings settings) throws DNFSException {
        Directory rootFolder = Directory.createNew(this);
        this.rootINode = rootFolder.getINode();
        
        /*DNFSFile testFile = DNFSFile.createNew(this);
        rootFolder.addChild(testFile.getINode(), "Test_File_1.txt");

        testFile = DNFSFile.createNew(this);
        rootFolder.addChild(testFile.getINode(), "Test_File_2.txt");

        DNFSFolder testFolder = DNFSFolder.createNew(this);
        rootFolder.addChild(testFolder.getINode(), "test_folder");*/
    }
    
    
    public void shutdown() throws DNFSNetworkNotInit {
        
    }
    
    public boolean isConnected() throws DNFSNetworkNotInit {
        return true;
    }
    
    public boolean isConnected(PeerAddress peerAddress) throws DNFSNetworkNotInit {
        return true;
    }

    @Override
    public void setConnectionTimeout(int connectionTimeOut){
      
    }
}
