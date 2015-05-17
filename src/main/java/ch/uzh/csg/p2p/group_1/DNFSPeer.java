package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.network.DNFSNetworkINode;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import org.apache.log4j.Logger;

import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;

public class DNFSPeer implements DNFSIPeer {
    
    
    final private static Logger LOGGER = Logger.getLogger(DNFSPeer.class.getName());

    private static final Number160 ROOT_INODE_KEY = Number160.createHash(0);
    
    private DNFSNetwork _network;
    private KeyValueStorageInterface _keyValueStorage;
    
    
    public DNFSPeer(DNFSNetwork network) {
        _network = network;
    }
    

    @Override
    public DNFSBlock createBlock() throws DNFSException.DNFSNetworkNoConnection {
        Number160 id = _network.getUniqueKey();
        DNFSBlock block = new DNFSBlock(id, this);
        
//       Number160 testFillerContent = Number160.createHash(0);
        byte[] testFillerContent = new byte[0];
        try {
            _network.put(id, testFillerContent);
        } catch (DNFSException.DNFSNetworkPutException e) {
            LOGGER.error("Could not put block.", e);
        }

        _keyValueStorage.set(id, new KeyValueData()); // TODO not local
       
        return block;
    }

    
    @Override
    public DNFSBlock getBlock(Number160 id) throws DNFSException.DNFSNetworkNoConnection {
        byte[] data = new byte[0];
        try {
            PeerAddress responder = _network.getFirstResponder(id);
            Object object = this._network.get(id);
            data = (byte[]) object;
        } catch (DNFSException.DNFSNetworkGetException e) {
            LOGGER.error("Fatal error. Block does not exist.", e);
        }

        return new DNFSBlock(id, data, this);
    }

    
    @Override
    public void updateBlock(DNFSBlock block) throws DNFSException.DNFSNetworkNoConnection {

        try {
            this._network.put(block.getId(), block.getByteArray());
        } catch (DNFSException.DNFSNetworkPutException e) {
            LOGGER.error("Serious problem. Could not update block.", e);
        }
//        PeerAddress resonder = _network.getFirstResponder(block.id);
        
//        _keyValueStorage.set(block.id, new KeyValueData(block.getByteArray())); // TODO not local
    }

    
    @Override
    public void deleteBlock(Number160 id) {
        //PeerAddress resonder = _network.getFirstResponder(id);
        
        // TODO not local
        _keyValueStorage.delete(id);
    }

    
    @Override
    public DNFSIiNode createINode() throws DNFSException {
        Number160 iNodeID = _network.getUniqueKey();
        DNFSIiNode iNode = new DNFSiNode(iNodeID);
        Object data = (Object) iNode; // TODO do better serialization
        _network.put(iNodeID, data);
        return new DNFSNetworkINode(iNode, this);
    }

    
    @Override
    public DNFSIiNode getINode(Number160 iNodeID) throws DNFSException {
        Object data = _network.get(iNodeID);
        DNFSIiNode iNode = (DNFSIiNode) data; // TODO do better serialization
        return new DNFSNetworkINode(iNode, this);
    }

    
    @Override
    public void deleteINode(Number160 iNodeID) throws DNFSException {
        _network.delete(iNodeID);
    }

    
    @Override
    public void updateINode(DNFSIiNode iNode) throws DNFSException {
        _network.delete(iNode.getId()); // TODO: change this once we have vDHT
        Object data = (Object) iNode.getSerializableVersion(); // TODO do better serialization
        _network.put(iNode.getId(), data);
    }

    
    @Override
    public DNFSIiNode getRootINode() throws DNFSException {
        return getINode(ROOT_INODE_KEY);
    }

    
    @Override
    public DNFSIiNode createRootINode() throws DNFSException {
        DNFSIiNode iNode = new DNFSiNode(ROOT_INODE_KEY);
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
