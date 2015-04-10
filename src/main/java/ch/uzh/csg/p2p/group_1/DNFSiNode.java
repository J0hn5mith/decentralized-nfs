package ch.uzh.csg.p2p.group_1;

import java.io.Serializable;

/**
 * Created by janmeier on 06.04.15.
 */
public class DNFSiNode implements Serializable{
    private static final long serialVersionUID = 2098774660703813030L;
    private boolean isDir;

    public void setDir(boolean isDir) {
        this.isDir = isDir;
    }

    public boolean isDir(){
        return isDir;
    }
}
