/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import net.fusejna.FuseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.ConfigurationException;


public class DecentralizedNetFileSystem implements IDecentralizedNetFileSystem {

    private DNFSFuseIntegration fuseIntegration;
    private DNFSPathResolver pathResolver;
    private DNFSConfigurator conf;
    private DNFSIPeer peer;

    /**
     * 
     */
    public DecentralizedNetFileSystem() {
        this.fuseIntegration = new DNFSFuseIntegration();
        Main.LOGGER.debug("DEBUG");
    }
    
    /**
     * 
     */
    public void loadConfig(String configFile) {
        Main.LOGGER.debug("DNFS has loaded the configuration.");
        this.conf = new DNFSConfigurator(configFile);
        try {
            this.conf.setUp();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            Main.LOGGER.error("Could not load the configuration.");
        }
        int port = this.conf.getConfig().getInt("Port");
    }

    /**
     * 
     */
    public void setUp(String configFile, CommandLine cmd) {
        this.loadConfig(configFile);

        if(cmd.hasOption('d')){
            this.peer = new DNFSDummyPeer();
        }
        else{
            this.peer = new DNFSPeer();
            try {
                this.peer.createRootINode();
            } catch (DNFSException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        this.pathResolver = new DNFSPathResolver(this.conf, this.peer);
        this.pathResolver.setUp();
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
        
        Main.LOGGER.debug("DNFS has been set up.");
    }


    /**
     * 
     */
    public void start() {
        Main.LOGGER.debug("DNFS has started.");

        try {
            String mountPoint = this.conf.getConfig().getString("MountPoint");
            this.fuseIntegration.mount(mountPoint);
        } catch (FuseException e) {
            e.printStackTrace();
            Main.LOGGER.error("Failed to mount the fuse file system.");
        }
        Main.LOGGER.info("The DNFS started successful.");
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
    
}
