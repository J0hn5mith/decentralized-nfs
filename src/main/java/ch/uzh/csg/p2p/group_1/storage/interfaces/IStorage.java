package ch.uzh.csg.p2p.group_1.storage.interfaces;

import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import net.tomp2p.peers.PeerAddress;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSNetworkNotInit;
import ch.uzh.csg.p2p.group_1.Settings;

/**
 * Created by janmeier on 16.04.15.
 */
public interface IStorage extends DNFSIBlockStorage, DNFSIiNodeStorage {

    public void setUp(Settings settings) throws DNFSException;
    
    public void shutdown() throws DNFSException.NetworkException;
    
    public boolean isConnected() throws  DNFSException.NetworkException;
    
    public boolean isConnected(PeerAddress peerAddress) throws DNFSNetworkNotInit, DNFSException.NetworkException;
    
    public void setConnectionTimeout(int connectionTimeOut);
}
