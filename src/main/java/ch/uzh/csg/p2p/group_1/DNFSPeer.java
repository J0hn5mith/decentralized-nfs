package ch.uzh.csg.p2p.group_1;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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
    
    
    public DNFSPeer(DNFSNetwork network, KeyValueStorageInterface keyValueStorage) {
        _network = network;
        _keyValueStorage = keyValueStorage;
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
            DNFSBlockPacket packet = new DNFSBlockPacket(DNFSBlockPacket.Type.REQUEST, id);
            Object answer = _network.sendTo(responder, packet);
            byte[] data = ((DNFSBlockPacket) answer).getData();
            return new DNFSBlock(id, data, this);
            
        } catch(DNFSNetworkGetException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkGetException: " + e.getMessage());
        } catch (DNFSNetworkSendException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkSendException: " + e.getMessage());
        }
    }

    
    @Override
    public void updateBlock(DNFSBlock block) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {
        
        try {
            
            Number160 id = block.id;
            byte[] data = block.getByteArray();
            
            _keyValueStorage.set(id, new KeyValueData(data));
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            
            PeerAddress localAddress = _network.getPeerAddress();
            
            DNFSBlockUpdateNotification notification = new DNFSBlockUpdateNotification(id, localAddress, hash);
            _network.put(id, (Object) notification);
            
        } catch(DNFSNetworkPutException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkPutException: " + e.getMessage());
        } catch(NoSuchAlgorithmException e) {
            throw new DNFSException.DNFSBlockStorageException("NoSuchAlgorithmException: " + e.getMessage());
        }
    }

    
    @Override
    public void deleteBlock(Number160 id) throws DNFSException.DNFSBlockStorageException, DNFSException.DNFSNetworkNoConnection {
        try {
            ArrayList<PeerAddress> responders = _network.getAllResponders(id);
            DNFSBlockPacket packet = new DNFSBlockPacket(DNFSBlockPacket.Type.DELETE, id);
            _network.sendToAll(responders, (Object) packet);
        } catch(DNFSNetworkGetException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkGetException: " + e.getMessage());
        } catch (DNFSNetworkSendException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkSendException: " + e.getMessage());
        }
        
    }

    
    @Override
    public DNFSIiNode createINode() throws DNFSException {
        Number160 iNodeID = _network.getUniqueKey();
        DNFSIiNode iNode = new DNFSiNode(iNodeID);
        Object data = (Object) iNode;
        _network.put(iNodeID, data);
        return new DNFSNetworkINode(iNode, this);
    }

    
    @Override
    public DNFSIiNode getINode(Number160 iNodeID) throws DNFSException {
        Object data = _network.get(iNodeID);
        DNFSIiNode iNode = (DNFSIiNode) data;
        return new DNFSNetworkINode(iNode, this);
    }

    
    @Override
    public void deleteINode(Number160 iNodeID) throws DNFSException {
        _network.delete(iNodeID);
    }

    
    @Override
    public void updateINode(DNFSIiNode iNode) throws DNFSException {
        _network.delete(iNode.getId()); // TODO: change this once we have vDHT
        Object data = (Object) iNode.getSerializableVersion();
        _network.put(iNode.getId(), data);
    }

    
    @Override
    public DNFSIiNode getRootINode() throws DNFSException {
        return getINode(ROOT_INODE_KEY);
    }

    
    @Override
    public DNFSIiNode createRootINode() throws DNFSException {
        DNFSIiNode iNode = new DNFSiNode(ROOT_INODE_KEY);
        Object data = (Object) iNode;
        _network.put(ROOT_INODE_KEY, data);
        LOGGER.info("Successfully create root iNode");
        return iNode;
    }

    
    @Override
    public void setUp(DNFSSettings settings) throws DNFSException {
        
        _network.registerObjectDataReply(new ObjectDataReply() {

            @Override
            public Object reply(PeerAddress sender, Object request) throws Exception {
                DNFSBlockPacket requestPacket = (DNFSBlockPacket) request;
                
                if(requestPacket.is(DNFSBlockPacket.Type.REQUEST)) {
                    KeyValueData keyValue = _keyValueStorage.get(requestPacket.getId());
                    return new DNFSBlockPacket(DNFSBlockPacket.Type.DELIVER, requestPacket.getId(), keyValue.getData());
                
                } else if(requestPacket.is(DNFSBlockPacket.Type.DELETE)) {
                    _keyValueStorage.delete(requestPacket.getId());
                    return new DNFSBlockPacket(DNFSBlockPacket.Type.DELETE_ACK, requestPacket.getId());
                
                }
                
                return null;
            }
            
        });
    }

}
