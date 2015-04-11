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

    List<Number160> blockIds;

    public DNFSiNode() {
        this.blockIds = new ArrayList<Number160>();
    }

    public void setDir(boolean isDir) {
        this.isDir = isDir;
    }

    public boolean isDir(){
        return isDir;
    }

    public int getSize(){
        return 100;
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

    public void addBlock(DNFSBlock block){
        blockIds.add(Number160.createHash(1000));
    }
    public List<Number160> getBlockIDs(){
        return blockIds;
    }
}
