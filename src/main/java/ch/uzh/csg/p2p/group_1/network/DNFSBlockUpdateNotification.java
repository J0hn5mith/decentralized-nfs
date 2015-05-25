package ch.uzh.csg.p2p.group_1.network;

import java.io.Serializable;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

public class DNFSBlockUpdateNotification implements Serializable {
    private static final long serialVersionUID = -3660168112397269893L;
    
    private Number160 _id;
    private PeerAddress _updateProvider;
    private byte[] _newHash;
    
    
    public DNFSBlockUpdateNotification(Number160 id, PeerAddress updateProvider, byte[] newHash) {
        _id = id;
        _updateProvider = updateProvider;
        _newHash = newHash;
    }
    
    
    public Number160 getId() {
        return _id;
    }
    
    
    public PeerAddress getUpdateProvider() {
        return _updateProvider;
    }
    
    
    public byte[] getNewHash() {
        return _newHash;
    }

}
