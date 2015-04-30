package ch.uzh.csg.p2p.group_1;


public class KeyValueData {
    
    private byte[] data;
    
    
    public KeyValueData() {
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
