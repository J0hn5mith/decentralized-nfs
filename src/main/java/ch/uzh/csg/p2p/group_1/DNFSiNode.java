package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public int getUseID(){
        return 10;
    }

    public int getGroupID(){
        return 100;
    }

    public int intGetFileMode(){
        return 777;
    }

    public Date getTimeStamp(){
        return new Date();
    }

    public List<Number160> getBlockIDs(){
        List<Number160> ids = new ArrayList<Number160>();
        ids.add(Number160.createHash(123));
        return ids;
    }
}
