package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.filesystem.DNFSAccessRights;
import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import net.fusejna.types.TypeGid;
import net.fusejna.types.TypeUid;
import net.tomp2p.peers.Number160;

import java.io.Serializable;
import java.util.*;

/**
 * Created by janmeier on 06.04.15.
 * <p/>
 * Notes from meeting at 30.4
 * - Implicit updating after each change
 */

public class DNFSiNode implements Serializable, DNFSIiNode {
    private static final long serialVersionUID = 2098774660703813030L;
    private boolean isDir;
    Number160 id;
    List<Number160> blockIds;
    int size = 10;
    TypeUid uid;
    TypeGid gid;
    long mode = 16895;


    public DNFSiNode(Number160 id) {
        this.id = id;
        this.blockIds = new ArrayList<Number160>();
    }

    public Number160 getId() {
        return id;
    }

    public void setDir(boolean isDir) {
        this.isDir = isDir;
    }

    public boolean isDir() {
        return isDir;
    }

    public int getSize() {
        return this.size;
    }

    public int setSize(int size) {
        this.size = size;
        return size;
    }


    public TypeUid getUid() {
        return null;
    }

    @Override
    public void setUid(TypeUid uid) {
        this.uid = uid;
    }

    @Override
    public TypeGid getGid() {
        return null;
    }

    @Override
    public void setGid(TypeGid gid) {

    }

    @Override
    public long getMode() {
        return this.mode;
    }

    @Override
    public void setMode(long mode) {
        this.mode = mode;
    }

    public int getGroupID() {
        return 100;
    }

    public int intGetFileMode() {
        return 777;
    }

    public Date getTimeStamp() {
        return new Date();
    }


    public DNFSBlock addBlock(DNFSBlock block) {
        blockIds.add(block.id);
        return block;
    }

    public DNFSBlock addBlock(DNFSBlock block, DNFSBlock afterBlock) {
        int index = blockIds.indexOf(afterBlock.getId());
        blockIds.add(index + 1, block.getId());
        return block;
    }

    @Override
    public void removeBlock(DNFSBlock block) {
        this.removeBlockID(block.getId());
    }

    @Override
    public void removeBlocks(List<Number160> blocks) {
        this.blockIds.removeAll(blocks);
    }

    @Override
    public void removeBlockID(Number160 id) {
        this.blockIds.remove(id);
    }

    public List<Number160> getBlockIDs() {
        return blockIds;
    }

    public int getNumBlocks() {
        return this.blockIds.size();
    }

    @Override
    public DNFSAccessRights getAccessRights() {
        return new DNFSAccessRights(this.getMode(), this.getUid(), this.getGid());
    }

    @Override
    public DNFSIiNode getSerializableVersion() {
        return this;
    }


    /**
     * Utilities
     */

    public DNFSBlock addBlock(DNFSIPeer peer) {
        try {
            DNFSBlock block = peer.createBlock();
            return this.addBlock(block);
        } catch (DNFSException e) {
            // TODO: DEAL WITH THIS
            System.out.println(e.getMessage());
        }
        return null;
    }

}
