package ch.uzh.csg.p2p.group_1.network.interfaces;

import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.rpc.ObjectDataReply;

import java.util.ArrayList;

/**
 * Created by janmeier on 19.05.15.
 */
public interface DNFSINetwork {


    public void registerObjectDataReply(ObjectDataReply reply);

    public void connectToNetwork(int port, String masterIpAddress, int masterPort) throws DNFSException.DNFSNetworkSetupException;

    public boolean keyExists(Number160 key) throws DNFSException.NetworkException;

    public Number160 getUniqueKey() throws DNFSException.NetworkException;

    public void put(Number160 key, Object data) throws
            DNFSException.NetworkException,
            DNFSException.DNFSNetworkPutException;

    public Object get(Number160 key) throws
            DNFSException.NetworkException,
            DNFSException.DNFSNetworkGetException;

    public void delete(Number160 key) throws
            DNFSException.NetworkException,
            DNFSException.DNFSNetworkDeleteException;

    public PeerAddress getFirstResponder(Number160 key) throws
            DNFSException.NetworkException,
            DNFSException.DNFSNetworkGetException;

    public Object sendTo(PeerAddress address, Object data) throws
            DNFSException.NetworkException,
            DNFSException.DNFSNetworkSendException;
    public ArrayList<Object> sendToAll(ArrayList<PeerAddress> addresses, Object data) throws DNFSException.NetworkException, DNFSException.DNFSNetworkSendException;

    public PeerAddress getPeerAddress();

    public ArrayList<PeerAddress> getAllResponders(Number160 key) throws DNFSException.NetworkException, DNFSException.DNFSNetworkGetException;
    
    public void registerPeerChangeListener(PeerMapChangeListener listener);
    
    public boolean isConnected() throws DNFSException.NetworkException;
    
    public boolean isConnected(PeerAddress peerAddress) throws DNFSException.NetworkException;
    
    public void disconnect() throws DNFSException.NetworkException;

    public void setConnectionTimeout(int connectionTimeOut);
    
}
