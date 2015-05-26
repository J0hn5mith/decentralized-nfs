package ch.uzh.csg.p2p.group_1.file_system;

import ch.uzh.csg.p2p.group_1.Settings;
import ch.uzh.csg.p2p.group_1.storage.DNFSBlock;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIBlockStorage;
import net.tomp2p.peers.Number160;

/**
 * Created by janmeier on 26.05.15.
 */
public class BlockFactory {

    Settings setings;

    BlockFactory(Settings settings){
        this.setings = settings;
    }

    public DNFSBlock getBlock(Number160 id, byte[] data, DNFSIBlockStorage blockStorage){
        return new DNFSBlock(id, data, blockStorage);
    }

    public DNFSBlock getBlock(Number160 id, DNFSIBlockStorage blockStorage){
        return new DNFSBlock(id, blockStorage);
    }
}
