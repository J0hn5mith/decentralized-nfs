package ch.uzh.csg.p2p.group_1.storage;

import ch.uzh.csg.p2p.group_1.Settings;
import ch.uzh.csg.p2p.group_1.storage.DNFSBlock;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIBlockStorage;
import net.tomp2p.peers.Number160;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import sun.jvm.hotspot.opto.Block;

/**
 * Created by janmeier on 26.05.15.
 */
public class BlockFactory {
    final private static Logger LOGGER = Logger.getLogger(BlockFactory.class);

    Settings setings;

    protected BlockFactory(Settings settings){
        this.LOGGER.setLevel(Level.DEBUG);
        this.setings = settings;
    }

    public DNFSBlock getBlock(Number160 id, byte[] data, DNFSIBlockStorage blockStorage){
        if(setings.useBlockEncryption()){
            LOGGER.warn("Block encryption is turned on but normal blocks are used.");
            return new DNFSBlock(id, data, blockStorage);
        }
        else{
            return new DNFSBlock(id, data, blockStorage);
        }
    }

    public DNFSBlock getBlock(Number160 id, DNFSIBlockStorage blockStorage){
        if(setings.useBlockEncryption()){
            LOGGER.warn("Block encryption is turned on but normal blocks are used.");
            return new DNFSBlock(id, blockStorage);
        }
        else {
            return new DNFSBlock(id, blockStorage);
        }
    }
}
