package ch.uzh.csg.p2p.group_1.storage.interfaces;

import ch.uzh.csg.p2p.group_1.storage.DNFSBlock;
import ch.uzh.csg.p2p.group_1.file_system.DNFSAccessRights;
import net.fusejna.types.TypeGid;
import net.fusejna.types.TypeUid;
import net.tomp2p.peers.Number160;

import java.util.Date;
import java.util.List;

/**
 * Created by janmeier on 17.05.15.
 */
public interface DNFSIiNode {
    public Number160 getId();

    public void setDir(boolean isDir);

    public boolean isDir();

    public int getSize();

    public int setSize(int size);

    public TypeUid getUid();
    public void setUid(TypeUid uid);
    public TypeGid getGid();
    public void setGid(TypeGid gid);

    public long getMode();
    public void setMode(long mode);

    public int getGroupID();

    public int intGetFileMode();

    public Date getTimeStamp();

    public DNFSBlock addBlock(DNFSBlock block);
    public DNFSBlock addBlock(DNFSBlock block, DNFSBlock after);
    public void removeBlock(DNFSBlock block);
    public void removeBlocks(List<Number160> blocks);
    public void removeBlockID(Number160 id);
    public List<Number160> getBlockIDs();
    public int getNumBlocks();

    public DNFSAccessRights getAccessRights();

    public DNFSIiNode getSerializableVersion();
}
