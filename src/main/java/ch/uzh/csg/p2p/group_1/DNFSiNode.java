package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by janmeier on 06.04.15.
 *
 * Notes from meeting at 30.4
 * - Implicit updating after each change
 */
public class DNFSiNode implements Serializable{
    private static final long serialVersionUID = 2098774660703813030L;
    private boolean isDir;
    Number160 id;
    List<Number160> blockIds;

    public DNFSiNode(Number160 id) {
        this.id = id;
        this.blockIds = new ArrayList<Number160>();
    }

    public Number160 getId() {
        return id;
    }

    public void setId(Number160 id) {
        this.id = id;
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
        blockIds.add(block.id);
    }
    public List<Number160> getBlockIDs(){
        return blockIds;
    }
}
