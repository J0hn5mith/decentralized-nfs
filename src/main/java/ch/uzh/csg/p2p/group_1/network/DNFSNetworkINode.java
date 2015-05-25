package ch.uzh.csg.p2p.group_1.network;

import ch.uzh.csg.p2p.group_1.*;
import ch.uzh.csg.p2p.group_1.filesystem.DNFSAccessRights;
import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import net.fusejna.types.TypeGid;
import net.fusejna.types.TypeUid;
import net.tomp2p.peers.Number160;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * Created by janmeier on 17.05.15.
 */
public class DNFSNetworkINode implements DNFSIiNode {
    final private static Logger LOGGER = Logger.getLogger(DNFSNetworkINode.class.getName());

    DNFSIiNode iNode;
    DNFSIiNodeStorage iNodeStorage;

    public DNFSNetworkINode(DNFSIiNode iNode, DNFSIiNodeStorage iNodeStorage) {
        this.iNode = iNode;
        this.iNodeStorage = iNodeStorage;
    }

    @Override
    public Number160 getId() {
        return iNode.getId();
    }

    @Override
    public void setDir(boolean isDir) {
        iNode.setDir(isDir);
        this.update();
    }

    @Override
    public boolean isDir() {
        return this.iNode.isDir();
    }

    @Override
    public int getSize() {
        return this.iNode.getSize();
    }

    @Override
    public int setSize(int size) {
        this.iNode.setSize(size);
        this.update();
        return size;
    }

    @Override
    public TypeUid getUid() {
        return this.iNode.getUid();
    }

    @Override
    public void setUid(TypeUid uid) {
        this.setUid(uid);
        this.update();
    }

    @Override
    public TypeGid getGid() {
        return this.iNode.getGid();
    }

    @Override
    public void setGid(TypeGid gid) {
        this.iNode.setGid(gid);
        this.update();

    }

    @Override
    public long getMode() {
        return this.iNode.getMode();
    }

    @Override
    public void setMode(long rights) {
        this.iNode.setMode(rights);
        this.update();
    }

    @Override
    public int getGroupID() {
        return this.iNode.getGroupID();
    }

    @Override
    public int intGetFileMode() {
        return this.iNode.intGetFileMode();
    }

    @Override
    public Date getTimeStamp() {
        return this.iNode.getTimeStamp();
    }

    @Override
    public DNFSBlock addBlock(DNFSBlock block) {
        this.iNode.addBlock(block);
        this.update();
        return block;
    }

    @Override
    public DNFSBlock addBlock(DNFSBlock block, DNFSBlock after) {
        this.iNode.addBlock(block, after);
        this.update();
        return block;
    }

    @Override
    public void removeBlock(DNFSBlock block) {
        this.iNode.removeBlock(block);
        this.update();

    }

    @Override
    public void removeBlocks(List<Number160> blocks) {
       this.iNode.removeBlocks(blocks);
        this.update();
    }

    @Override
    public void removeBlockID(Number160 id) {
        this.iNode.removeBlockID(id);
        this.update();
    }

    @Override
    public List<Number160> getBlockIDs() {
        return this.iNode.getBlockIDs();
    }

    @Override
    public int getNumBlocks() {
        return this.iNode.getNumBlocks();
    }

    @Override
    public DNFSAccessRights getAccessRights() {
        return this.iNode.getAccessRights();
    }

    @Override
    public DNFSIiNode getSerializableVersion() {
        return this.iNode;
    }

    private void update(){
        try {
            this.iNodeStorage.updateINode(this.iNode);
        } catch (DNFSException e) {
            LOGGER.warn("Could not update iNode.");
        }
    }
}
