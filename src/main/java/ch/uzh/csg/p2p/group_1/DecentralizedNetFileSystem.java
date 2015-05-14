/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkSetupException;
import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;
import net.fusejna.FuseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class DecentralizedNetFileSystem implements IDecentralizedNetFileSystem {

    final private static Logger LOGGER = Logger.getLogger(DNFSPeer.class.getName());


    private DNFSFuseIntegration fuseIntegration;
    private DNFSPathResolver pathResolver;
    private DNFSSettings settings;
    private DNFSIPeer peer;

    private String mountPoint;


    /**
     * 
     */
    public DecentralizedNetFileSystem() {
        this.LOGGER.setLevel(Level.INFO);
        this.fuseIntegration = new DNFSFuseIntegration();
    }
    
    /**
     * 
     */
    public void setUp(String configFile, CommandLine cmd) {
        this.settings = new DNFSSettings(configFile, cmd);

        try {
            DNFSNetwork.createNetwork(this.settings.getPort());
        } catch (DNFSNetworkSetupException e) {
            LOGGER.error("Could not set up the network.", e);
            System.exit(-1);
        }

        this.setUpPeer();
        this.pathResolver = new DNFSPathResolver(this.peer);
        this.fuseIntegration.setPathResolver(this.pathResolver);


        // START STORAGE EXAMPLE
        
//        KeyValueStorageInterface keyValueStorage = new FileBasedKeyValueStorage();
//        String storageDirectory = this.conf.getConfig().getString("FileBasedStorageDirectory"); // muesch usefinde wo s config-objäkt isch
//        ((FileBasedKeyValueStorage) keyValueStorage).setDirectory(storageDirectory); // muesch typecaste zum directory sette.
//
//        Number160 key = Number160.createHash(1000000 * (int)Math.random());
//
//        System.out.println("EXISTS?" + (keyValueStorage.exists(key) ? "Yes" : "No"));
//
//        keyValueStorage.set(key, new KeyValueData("HALLO".getBytes()));
//
//        System.out.println("EXISTS? " + (keyValueStorage.exists(key) ? "Yes" : "No"));
//        System.out.println("VALUE " + new String(keyValueStorage.get(key).getData()));
//
//        keyValueStorage.set(key, new KeyValueData("WORLD".getBytes()));
//
//        System.out.println("EXISTS? " + (keyValueStorage.exists(key) ? "Yes" : "No"));
//        System.out.println("VALUE " + new String(keyValueStorage.get(key).getData()));
//
//        keyValueStorage.delete(key);
//
//        System.out.println("EXISTS? " + (keyValueStorage.exists(key) ? "Yes" : "No"));
        
        // END STORAGE EXAMPLE
        
        LOGGER.info("DNFS has been set up.with mount point " + this.settings.getMountPoint());
    }


    /**
     * 
     */
    public void start() {
        Main.LOGGER.debug("DNFS has started.");

        try {
            this.fuseIntegration.mount(this.settings.getMountPoint());
        } catch (FuseException e) {
            Main.LOGGER.error("Failed to mount the fuse file system.");
            e.printStackTrace();
        }
        LOGGER.info("The DNFS started successful");
    }

    /**
     * 
     */
    public void pause() {
        Main.LOGGER.debug("DNFS has paused.");
    }

    /**
     * 
     */
    public void resume() {
        Main.LOGGER.debug("DNFS has resumed.");
    }

    /**
     * 
     */
    public void shutDown() {
        Main.LOGGER.debug("DNFS has shut down.");
    }


    private void setUpPeer(){
        if(this.settings.getUseDummyPeer()){
            this.peer = new DNFSDummyPeer();
        }
        else {
            this.peer = new DNFSPeer();
        }
        try {
            this.peer.setUp(this.settings);
            this.peer.createRootINode();
        } catch (DNFSException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
