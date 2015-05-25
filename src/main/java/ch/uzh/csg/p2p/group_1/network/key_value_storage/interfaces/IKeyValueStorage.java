package ch.uzh.csg.p2p.group_1.network.key_value_storage.interfaces;


import ch.uzh.csg.p2p.group_1.network.key_value_storage.KeyValueData;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import net.tomp2p.peers.Number160;


public interface IKeyValueStorage {
    
    
    public void startUp() throws DNFSException.DNFSKeyValueStorageException;
    public void shutDown() throws DNFSException.DNFSKeyValueStorageException;
    
    public boolean exists(Number160 key);    
    public boolean set(Number160 key, KeyValueData value);
    public KeyValueData get(Number160 key);
    public boolean delete(Number160 key);

}
