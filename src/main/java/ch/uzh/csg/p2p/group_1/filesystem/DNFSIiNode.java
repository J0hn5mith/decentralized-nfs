package ch.uzh.csg.p2p.group_1.filesystem;

import ch.uzh.csg.p2p.group_1.DNFSBlock;
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

    public int getUseID();
    public void setUserID(int id);

    public long getAccessRights();
    public void setAccessRights(long rights);

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

    public DNFSIiNode getSerializableVersion();
}
