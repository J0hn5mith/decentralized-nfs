package ch.uzh.csg.p2p.group_1;

import java.io.IOException;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;

public class DNFSPeer implements DNFSIPeer {
    
    
    final private static Logger LOGGER = Logger.getLogger(DNFSPeer.class.getName());

    private static final Number160 ROOT_INODE_KEY = Number160.createHash(0);
    
    private static KeyValueStorageInterface keyValueStorage;
    

    @Override
    public DNFSBlock createBlock() throws DNFSException {
        Number160 id = DNFSNetwork.getUniqueKey();
        DNFSBlock block = new DNFSBlock(id);
       //Number160 testFillerContent = Number160.createHash(0);
        //DNFSNetwork.put(id, new Object());
         
        keyValueStorage.set(id, new KeyValueData()); // TODO not local
       
        return block;
    }

    
    @Override
    public DNFSBlock getBlock(Number160 id) throws DNFSException {
        //PeerAddress resonder = DNFSNetwork.getFirstResponder(id);
        
        // TODO not local
        
        byte[] data = keyValueStorage.get(id).getData();
        
        return new DNFSBlock(id, data);
    }

    
    @Override
    public void updateBlock(DNFSBlock block) throws DNFSException {
        //PeerAddress resonder = DNFSNetwork.getFirstResponder(block.id);
        
        keyValueStorage.set(block.id, new KeyValueData(block.getByteArray())); // TODO not local
    }

    
    @Override
    public void deleteBlock(Number160 id) throws DNFSException {
        //PeerAddress resonder = DNFSNetwork.getFirstResponder(id);
        
        // TODO not local
        keyValueStorage.delete(id);
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
    public void setUp(DNFSSettings settings) throws DNFSException {
        keyValueStorage = new FileBasedKeyValueStorage();
        String storageDirectory = settings.getFileBasedStorageDirectory();
        ((FileBasedKeyValueStorage) keyValueStorage).setDirectory(storageDirectory);
    }

}
