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
public class DNFSiNode implements Serializable {
    private static final long serialVersionUID = 2098774660703813030L;
    private boolean isDir;
    Number160 id;
    List<Number160> blockIds;
    int size = 10;

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
        return this.size;
    }
    public int setSize(int size){
        this.size = size;
        return size;
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


    public DNFSBlock  addBlock(DNFSBlock block){
        blockIds.add(block.id);
        return block;
    }

    public DNFSBlock  addBlock(DNFSBlock block, DNFSBlock afterBlock){
        int index = blockIds.indexOf(afterBlock.getId());
        blockIds.add(index + 1, block.getId());
        return block;
    }

    public List<Number160> getBlockIDs(){
        return blockIds;
    }

    public int getNumBlocks(){
        return this.blockIds.size();
    }

    /**
     * Utilities
     */

    public DNFSBlock addBlock(DNFSIPeer peer){
        try {
            DNFSBlock block = peer.createBlock();
            return this.addBlock(block);
        } catch(DNFSException e) {
            // TODO: DEAL WITH THIS
            System.out.println(e.getMessage());
        }
        return null;
    }
}
