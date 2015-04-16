package ch.uzh.csg.p2p.group_1;

import net.tomp2p.peers.Number160;

/**
 * Created by janmeier on 16.04.15.
 */
public interface DNFSIBlockStorage {

    public DNFSBlock getNewBlock();
    public DNFSBlock getBlock(Number160 id);
    public DNFSBlock deleteBlock(Number160 id);

}
