package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.PeerAddress;
import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkNotInit;
import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;

/**
 * Created by janmeier on 16.04.15.
 */
public interface DNFSIPeer extends DNFSIBlockStorage, DNFSIiNodeStorage{


    public void setUp(DNFSSettings settings) throws DNFSException;
    
    public void shutdown() throws DNFSNetworkNotInit;
    
    public boolean isConnected() throws DNFSNetworkNotInit;
    
    public boolean isConnected(PeerAddress peerAddress) throws DNFSNetworkNotInit;
}
