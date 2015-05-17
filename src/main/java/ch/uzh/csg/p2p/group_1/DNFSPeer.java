package ch.uzh.csg.p2p.group_1;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.apache.log4j.Logger;

import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkGetException;
import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkPutException;
import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkSendException;
import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.network.DNFSNetworkINode;
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
    public DNFSBlock createBlock() throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {
        Number160 id = _network.getUniqueKey();
        DNFSBlock block = new DNFSBlock(id, this);
        updateBlock(block);       
        return block;
    }

    
    @Override
    public DNFSBlock getBlock(Number160 id) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {
        try {
            PeerAddress responder = _network.getFirstResponder(id);
            //TODO
            byte[] data = new byte[0];
            
            return new DNFSBlock(id, data, this);
            
        } catch(DNFSNetworkGetException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkGetException: " + e.getMessage());
        }
    }

    
    @Override
    public void updateBlock(DNFSBlock block) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {
        
        try {
            
            Number160 id = block.id;
            byte[] data = block.getByteArray();
            
            boolean success = false;
            
            while(!success) {
                
                DNFSBlockPacket packet;
                ArrayList<PeerAddress> responders;
                
                DNFSBlockLock lock = new DNFSBlockLock(true);
                _network.put(id, (Object) lock);
                
                packet = new DNFSBlockPacket(DNFSBlockPacket.Type.STORE, id, data);
                responders = _network.getAllResponders(id);
                _network.sendToAll(responders, (Object) packet);
                
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hash = md.digest(data);
                
                success = true;
                packet = new DNFSBlockPacket(DNFSBlockPacket.Type.CHECK_HASH, id, hash);
                responders = _network.getAllResponders(id);
                ArrayList<Object> hashResponses = _network.sendToAll(responders, (Object) packet);
                
                for(Object hashResponse : hashResponses) {
                    DNFSBlockPacket responsePacket = (DNFSBlockPacket) hashResponse;
                    if(responsePacket.is(DNFSBlockPacket.Type.HASH_FAIL)) {
                        success = false;
                    }
                }
            }
            
            DNFSBlockLock lock = new DNFSBlockLock(false);
            _network.put(id, (Object) lock);
            
        } catch(DNFSNetworkPutException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkPutException: " + e.getMessage());
        } catch(DNFSNetworkGetException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkGetException: " + e.getMessage());
        } catch(DNFSNetworkSendException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkSendException: " + e.getMessage());
        } catch(NoSuchAlgorithmException e) {
            throw new DNFSException.DNFSBlockStorageException("NoSuchAlgorithmException: " + e.getMessage());
        }
        
        _keyValueStorage.set(block.id, new KeyValueData(block.getByteArray())); // TODO not local
    }

    
    @Override
    public void deleteBlock(Number160 id) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {
        try {
            PeerAddress resonder = _network.getFirstResponder(id);
        } catch(DNFSNetworkGetException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkGetException: " + e.getMessage());
        }
        
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
        
        _network.registerObjectDataReply(new ObjectDataReply() {

            @Override
            public Object reply(PeerAddress sender, Object request) throws Exception {
                DNFSBlockPacket requestPacket = (DNFSBlockPacket) request;
                
                if(requestPacket.is(DNFSBlockPacket.Type.STORE)) {
                    _keyValueStorage.set(requestPacket.getId(), new KeyValueData());
                    return new DNFSBlockPacket(DNFSBlockPacket.Type.STORE_ACK);
                
                } else if(requestPacket.is(DNFSBlockPacket.Type.CHECK_HASH)) {
                    byte[] data = _keyValueStorage.get(requestPacket.getId()).getData();
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] hash = md.digest(data);
                    if(Arrays.equals(hash, requestPacket.getData())) {
                        return new DNFSBlockPacket(DNFSBlockPacket.Type.HASH_OK);
                    } else {
                        return new DNFSBlockPacket(DNFSBlockPacket.Type.HASH_FAIL);
                    }
                    
                }
                
                return null;
            }
            
        });
        
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
