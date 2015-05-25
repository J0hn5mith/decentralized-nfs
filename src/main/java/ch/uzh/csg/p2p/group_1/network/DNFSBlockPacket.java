package ch.uzh.csg.p2p.group_1.network;

import java.io.Serializable;

import net.tomp2p.peers.Number160;


public class DNFSBlockPacket implements Serializable {
    private static final long serialVersionUID = -4456848549958796409L;


    public enum Type {
        REQUEST,
        DELIVER,
        DELETE,
        DELETE_ACK,
        FAILURE
    }
    
    
    private Type _type;
    private Number160 _id;
    private byte[] _data;
    
    
    public DNFSBlockPacket(Type type, Number160 id) {
        _type = type;
        _id = id;
    }
    
    
    public DNFSBlockPacket(Type type, Number160 id, byte[] data) {
        _type = type;
        _id = id;
        _data = data;
    }
    
    
    public boolean is(Type suggestion) {
        return _type == suggestion;
    }
    
    
    public Type getType() {
        return _type;
    }
    
    
    public Number160 getId() {
        return _id;
    }
    

    public byte[] getData() {
        return _data;
    }
    
}
