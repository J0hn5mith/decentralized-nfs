package ch.uzh.csg.p2p.group_1;


import net.tomp2p.peers.Number160;


public interface IKeyValueStorage {
    
    
    public void startUp() throws Exception;
    public void shutDown() throws Exception;
    
    public boolean exists(Number160 key);    
    public boolean set(Number160 key, KeyValueData value);
    public KeyValueData get(Number160 key);
    public boolean delete(Number160 key);

}
