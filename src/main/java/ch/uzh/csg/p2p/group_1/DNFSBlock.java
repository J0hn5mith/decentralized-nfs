package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by janmeier on 10.04.15.
 */
public class DNFSBlock implements Serializable {
    private static final long serialVersionUID = 2098774660703813030L;

    Number160 id;
    String data;

    public DNFSBlock(Number160 id) {
        this.data = "";
        this.id = id;
    }

    public DNFSBlock(Number160 id, String data) {
        this(id);
        this.data = data;
    }

    public Number160 getId() {
        return id;
    }

    public void setId(Number160 id) {
        this.id = id;
    }

    public InputStream getInputStream(){
        return new ByteArrayInputStream(data.getBytes());
    }

    public void append(String appendString){
        this.data = this.data + appendString;
    }
}
