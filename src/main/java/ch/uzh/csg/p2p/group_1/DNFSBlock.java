package ch.uzh.csg.p2p.group_1;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by janmeier on 10.04.15.
 */
public class DNFSBlock implements Serializable {
    private static final long serialVersionUID = 2098774660703813030L;

    String data;

    public DNFSBlock() {
        this.data = "";
    }

    public DNFSBlock(String data) {
        this.data = data;
    }

    public InputStream getInputStream(){
        return new ByteArrayInputStream(data.getBytes());
    }
}
