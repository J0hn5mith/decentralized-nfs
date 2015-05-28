package ch.uzh.csg.p2p.group_1.storage;

import ch.uzh.csg.p2p.group_1.Main;
import ch.uzh.csg.p2p.group_1.Settings;
import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIBlockStorage;
import net.tomp2p.peers.Number160;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Created by janmeier on 26.05.15.
 */
public class BlockFactory {
    final private static Logger LOGGER = Logger.getLogger(BlockFactory.class);

    Settings settings;

    protected BlockFactory(Settings settings){
        this.LOGGER.setLevel(Main.LOGGER_LEVEL);
        this.settings = settings;
    }

    public DNFSBlock getBlock(Number160 id, byte[] data, DNFSIBlockStorage blockStorage){
        if(settings.useBlockEncryption()){
            return new BlockEncrypted(id, data, blockStorage, this.settings.getEncryptionCypher(), this.settings.getDecryptCipherCypher());
        }
        else{
            return new DNFSBlock(id, data, blockStorage);
        }
    }

    public DNFSBlock getBlock(Number160 id, DNFSIBlockStorage blockStorage){
        if(settings.useBlockEncryption()){
            return new BlockEncrypted(id, blockStorage, this.settings.getEncryptionCypher(), this.settings.getDecryptCipherCypher());
        }
        else {
            return new DNFSBlock(id, blockStorage);
        }
    }
}
