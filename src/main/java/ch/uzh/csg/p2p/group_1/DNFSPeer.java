package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;
import org.apache.log4j.Logger;

import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;

public class DNFSPeer implements DNFSIPeer {
    
    
    final private static Logger LOGGER = Logger.getLogger(DNFSPeer.class.getName());

    private static final Number160 ROOT_INODE_KEY = Number160.createHash(0);
    
    private static DNFSNetwork _network;
    private static KeyValueStorageInterface _keyValueStorage;
    
    
    public DNFSPeer(DNFSNetwork network) {
        _network = network;
    }
    

    @Override
    public DNFSBlock createBlock() throws DNFSException {
        Number160 id = _network.getUniqueKey();
        DNFSBlock block = new DNFSBlock(id);
        
       //Number160 testFillerContent = Number160.createHash(0);
        //_network.put(id, new Object());

        _keyValueStorage.set(id, new KeyValueData()); // TODO not local
       
        return block;
    }

    
    @Override
    public DNFSBlock getBlock(Number160 id) throws DNFSException {
        //PeerAddress resonder = _network.getFirstResponder(id);
        
        // TODO not local
        
        byte[] data = _keyValueStorage.get(id).getData();
        
        return new DNFSBlock(id, data);
    }

    
    @Override
    public void updateBlock(DNFSBlock block) throws DNFSException {
        //PeerAddress resonder = _network.getFirstResponder(block.id);
        
        _keyValueStorage.set(block.id, new KeyValueData(block.getByteArray())); // TODO not local
    }

    
    @Override
    public void deleteBlock(Number160 id) throws DNFSException {
        //PeerAddress resonder = _network.getFirstResponder(id);
        
        // TODO not local
        _keyValueStorage.delete(id);
    }

    
    @Override
    public DNFSiNode createINode() throws DNFSException {
        Number160 iNodeID = _network.getUniqueKey();
        DNFSiNode iNode = new DNFSiNode(iNodeID);
        Object data = (Object) iNode; // TODO do better serialization
        _network.put(iNodeID, data);
        return iNode;
    }

    
    @Override
    public DNFSiNode getINode(Number160 iNodeID) throws DNFSException {
        Object data = _network.get(iNodeID);
        DNFSiNode iNode = (DNFSiNode) data; // TODO do better serialization
        return iNode;
    }

    
    @Override
    public void deleteINode(Number160 iNodeID) throws DNFSException {
        _network.delete(iNodeID);
    }

    
    @Override
    public void updateINode(DNFSiNode iNode) throws DNFSException {
        _network.delete(iNode.id); // TODO: change this once we have vDHT
        Object data = (Object) iNode; // TODO do better serialization
        _network.put(iNode.id, data);
    }

    
    @Override
    public DNFSiNode getRootINode() throws DNFSException {
        return getINode(ROOT_INODE_KEY);
    }

    
    @Override
    public DNFSiNode createRootINode() throws DNFSException {
        DNFSiNode iNode = new DNFSiNode(ROOT_INODE_KEY);
        Object data = (Object) iNode; // TODO do better serialization
        _network.put(ROOT_INODE_KEY, data);
        LOGGER.info("Successfully create root iNode");
        return iNode;
    }

    
    @Override
    public void setUp(DNFSSettings settings) throws DNFSException {
        _keyValueStorage = new FileBasedKeyValueStorage();
        String storageDirectory = settings.getFileBasedStorageDirectory();
        ((FileBasedKeyValueStorage) _keyValueStorage).setDirectory(storageDirectory);
        
        // START STORAGE EXAMPLE
        
//      KeyValueStorageInterface keyValueStorage = new FileBasedKeyValueStorage();
//      String storageDirectory = this.conf.getConfig().getString("FileBasedStorageDirectory"); // muesch usefinde wo s config-objäkt isch
//      ((FileBasedKeyValueStorage) keyValueStorage).setDirectory(storageDirectory); // muesch typecaste zum directory sette.
//
//      Number160 key = Number160.createHash(1000000 * (int)Math.random());
//
//      System.out.println("EXISTS?" + (keyValueStorage.exists(key) ? "Yes" : "No"));
//
//      keyValueStorage.set(key, new KeyValueData("HALLO".getBytes()));
//
//      System.out.println("EXISTS? " + (keyValueStorage.exists(key) ? "Yes" : "No"));
//      System.out.println("VALUE " + new String(keyValueStorage.get(key).getData()));
//
//      keyValueStorage.set(key, new KeyValueData("WORLD".getBytes()));
//
//      System.out.println("EXISTS? " + (keyValueStorage.exists(key) ? "Yes" : "No"));
//      System.out.println("VALUE " + new String(keyValueStorage.get(key).getData()));
//
//      keyValueStorage.delete(key);
//
//      System.out.println("EXISTS? " + (keyValueStorage.exists(key) ? "Yes" : "No"));
      
      // END STORAGE EXAMPLE
    }

}
