package ch.uzh.csg.p2p.group_1.storage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import ch.uzh.csg.p2p.group_1.network.key_value_storage.interfaces.IKeyValueStorage;
import ch.uzh.csg.p2p.group_1.storage.interfaces.IStorage;
import ch.uzh.csg.p2p.group_1.network.key_value_storage.KeyValueData;
import ch.uzh.csg.p2p.group_1.network.DNFSBlockPacket;
import ch.uzh.csg.p2p.group_1.network.DNFSBlockUpdateNotification;
import ch.uzh.csg.p2p.group_1.network.interfaces.DNFSINetwork;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSNetworkGetException;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSNetworkPutException;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSNetworkSendException;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.network.DNFSNetworkINode;
import ch.uzh.csg.p2p.group_1.Settings;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Storage implements IStorage {
    final private static Logger LOGGER = Logger.getLogger(Storage.class.getName());

    private static final Number160 ROOT_INODE_KEY = Number160.createHash(0);

    private DNFSINetwork _network;
    private IKeyValueStorage _keyValueStorage;
    private BlockFactory blockFactory;

    public Storage(DNFSINetwork network, IKeyValueStorage keyValueStorage) {
        _network = network;
        _keyValueStorage = keyValueStorage;
    }


    @Override
    public DNFSBlock createBlock() throws DNFSException.DNFSBlockStorageException {
        Number160 id = null;
        try {
            id = _network.getUniqueKey();
            DNFSBlock block = this.blockFactory.getBlock(id, this);
            updateBlock(block);
            return block;
        } catch (DNFSException.NetworkException e) {
            throw new DNFSException.DNFSBlockStorageException("", e);
        }
    }


    @Override
    public DNFSBlock getBlock(Number160 id) throws DNFSException.DNFSBlockStorageException {
        try {
            PeerAddress responder = null;
            responder = _network.getFirstResponder(id);
            DNFSBlockPacket packet = new DNFSBlockPacket(DNFSBlockPacket.Type.REQUEST, id);
            Object answer = _network.sendTo(responder, packet);
            byte[] data = ((DNFSBlockPacket) answer).getData();
            return this.blockFactory.getBlock(id, data, this);
        } catch(DNFSNetworkGetException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkGetException: ", e);
        } catch (DNFSNetworkSendException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkSendException: ", e);
        } catch (DNFSException.NetworkException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkSendException: ", e);
        }
    }


    @Override
    public void updateBlock(DNFSBlock block) throws DNFSException.DNFSBlockStorageException {

        try {

            Number160 id = block.getId();
            byte[] data = block.getByteArray();

            _keyValueStorage.set(id, new KeyValueData(data));
            LOGGER.debug("SAVED: " + id); // TODO

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);

            PeerAddress localAddress = _network.getPeerAddress();

            DNFSBlockUpdateNotification notification = new DNFSBlockUpdateNotification(id, localAddress, hash);
            LOGGER.debug("NOW PUTTING: " + id); // TODO
            _network.put(id, (Object) notification);

        } catch(DNFSNetworkPutException e) {
            throw new DNFSException.DNFSBlockStorageException("DNFSNetworkPutException: ", e);
        } catch(NoSuchAlgorithmException e) {
            throw new DNFSException.DNFSBlockStorageException("NoSuchAlgorithmException: ", e);
        } catch(DNFSException.NetworkException e) {
            throw new DNFSException.DNFSBlockStorageException("NoSuchAlgorithmException: ", e);
        }
    }


    @Override
    public void deleteBlock(Number160 id) throws DNFSException.DNFSBlockStorageException {
        try {
            ArrayList<PeerAddress> responders = _network.getAllResponders(id);
            DNFSBlockPacket packet = new DNFSBlockPacket(DNFSBlockPacket.Type.DELETE, id);
            _network.sendToAll(responders, (Object) packet);
        } catch(DNFSNetworkGetException e) {
            throw new DNFSException.DNFSBlockStorageException("Could delete block because network was not available.", e);
        } catch(DNFSNetworkSendException e) {
            throw new DNFSException.DNFSBlockStorageException("Could delete block because network was not available.", e);
        } catch (DNFSException.NetworkException e) {
            throw new DNFSException.DNFSBlockStorageException("Could delete block because network was not available.", e);
        }

    }


    @Override
    public DNFSIiNode createINode() throws DNFSException.INodeStorageException {
        Number160 iNodeID = null;
        try {
            iNodeID = _network.getUniqueKey();
            DNFSIiNode iNode = new DNFSiNode(iNodeID);
            Object data = (Object) iNode;
            iNode.addBlock(this.createBlock());
            _network.put(iNodeID, data);
            return new DNFSNetworkINode(iNode, this);
        } catch (DNFSException.NetworkException e) {
            throw new DNFSException.INodeStorageException("Could not get unique key from for new iNode.", e);
        } catch (DNFSNetworkPutException | DNFSException.DNFSBlockStorageException e) {
            throw new DNFSException.INodeStorageException("Could not put new iNode.", e);
        }
    }


    @Override
    public DNFSIiNode getINode(Number160 iNodeID) throws DNFSException.INodeStorageException {
        Object data = null;

        try {
            data = _network.get(iNodeID);
        } catch (DNFSException.NetworkException e) {
            e.printStackTrace();
            throw new DNFSException.INodeStorageException("Unable to getINode data.", e);
        } catch (DNFSNetworkGetException e) {
            e.printStackTrace();
            throw new DNFSException.INodeStorageException("Unable to getINode data.", e);
        }

        DNFSIiNode iNode = (DNFSIiNode) data;
        return new DNFSNetworkINode(iNode, this);
    }


    @Override
    public void deleteINode(Number160 iNodeID) throws DNFSException.INodeStorageException {
        try {
            _network.delete(iNodeID);
        } catch (DNFSException.NetworkException e) {
            throw new DNFSException.INodeStorageException("", e);
        } catch (DNFSException.DNFSNetworkDeleteException e) {
            throw new DNFSException.INodeStorageException("", e);
        }
    }


    @Override
    public void updateINode(DNFSIiNode iNode) throws DNFSException.INodeStorageException {
        Object data = (Object) iNode.getSerializableVersion();
        try {
            _network.put(iNode.getId(), data);
        } catch (DNFSException.NetworkException e) {
            throw new DNFSException.INodeStorageException("", e);
        } catch (DNFSNetworkPutException e) {
            throw new DNFSException.INodeStorageException("", e);
        }
    }


    @Override
    public DNFSIiNode getRootINode() throws DNFSException.INodeStorageException {
            return getINode(ROOT_INODE_KEY);
    }


    @Override
    public DNFSIiNode createRootINode() throws DNFSException.INodeStorageException {
        DNFSIiNode iNode = new DNFSiNode(ROOT_INODE_KEY);
        Object data = (Object) iNode;
        try {
            _network.put(ROOT_INODE_KEY, data);
        } catch (DNFSException.NetworkException e) {
            throw new DNFSException.INodeStorageException("", e);
        } catch (DNFSNetworkPutException e) {
            throw new DNFSException.INodeStorageException("", e);
        }
        return new DNFSNetworkINode(iNode, this);
    }


    @Override
    public void setUp(Settings settings) throws DNFSException {
        this.blockFactory = new BlockFactory(settings);

        _network.registerObjectDataReply(new ObjectDataReply() {

            @Override
            public Object reply(PeerAddress sender, Object request) throws Exception {

                DNFSBlockPacket requestPacket = (DNFSBlockPacket) request;

                try {

                    if(requestPacket.is(DNFSBlockPacket.Type.REQUEST)) {
                        KeyValueData keyValue = _keyValueStorage.get(requestPacket.getId());
                        if(keyValue == null) {
//                            LOGGER.debug("DIDNT FIND: " + requestPacket.getId()); // TODO
                            throw new Exception();
                        }
//                        LOGGER.debug("DEVLIVERING: " + requestPacket.getId()); //TODO
                        return new DNFSBlockPacket(DNFSBlockPacket.Type.DELIVER, requestPacket.getId(), keyValue.getData());

                    } else if(requestPacket.is(DNFSBlockPacket.Type.DELETE)) {
                        _keyValueStorage.delete(requestPacket.getId());
                        return new DNFSBlockPacket(DNFSBlockPacket.Type.DELETE_ACK, requestPacket.getId());

                    }
                } catch(Exception e) {
                    return new DNFSBlockPacket(DNFSBlockPacket.Type.FAILURE, requestPacket.getId());
                }

                return null;
            }

        });

    }

    public void shutdown() throws DNFSException.NetworkException
    {
            _network.disconnect();
    }

    public boolean isConnected() throws DNFSException.NetworkException {
            return _network.isConnected();
    }

    public boolean isConnected(PeerAddress peerAddress) throws DNFSException.NetworkException {
        return _network.isConnected(peerAddress);
    }

    @Override
    public void setConnectionTimeout(int connectionTimeOut){
        _network.setConnectionTimeout(connectionTimeOut);
    }

}
