package ch.uzh.csg.p2p.group_1.network.key_value_storage;


public class KeyValueData {
    
    private byte[] data;
    
    
    public KeyValueData() {
        data = new byte[0];
    }
    
    
    public KeyValueData(byte[] data_) {
        data = data_;
    }
    
    
    public void setData(byte[] data_) {
        data = data_;
    }


    public byte[] getData() {
        return data;
    }

}
