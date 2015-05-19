/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkSetupException;
import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;
import net.fusejna.FuseException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class DecentralizedNetFileSystem implements IDecentralizedNetFileSystem {

    final private static Logger LOGGER = Logger.getLogger(DecentralizedNetFileSystem.class);

    private DNFSFuseIntegration fuseIntegration;
    private DNFSPathResolver pathResolver;
    private DNFSSettings settings;
    private IKeyValueStorage keyValueStorage;
    private DNFSNetwork network;
    private DNFSIPeer peer;
    

    /**
     * 
     */
    public DecentralizedNetFileSystem() {
        LOGGER.setLevel(Level.INFO);
        this.fuseIntegration = new DNFSFuseIntegration();
    }
    
    
    /**
     * 
     */
    public void setUp(DNFSSettings settings) {
        
        this.settings = settings;

        this.setUpPeer();
        
        this.pathResolver = new DNFSPathResolver(this.peer);
        this.fuseIntegration.setPathResolver(this.pathResolver);

        if(this.settings.getStartNewServer()) {
            this.createRootFolder();
        }
    }
    

    /**
     * 
     */
    private void setUpPeer() {
        
        if(this.settings.getUseDummyPeer()) {
            this.peer = new DNFSDummyPeer();
            
        } else {
            this.keyValueStorage = new FileBasedKeyValueStorage();
            String storageDirectory = settings.getFileBasedStorageDirectory();
            ((FileBasedKeyValueStorage) this.keyValueStorage).setDirectory(storageDirectory);
            try {
                this.network = new DNFSNetwork(this.settings.getPort(), this.keyValueStorage);
            } catch (DNFSNetworkSetupException e) {
                LOGGER.error("Could not set up the network.", e);
                System.exit(-1);
            }
            this.peer = new DNFSPeer(this.network, this.keyValueStorage);
        }

        try {
            this.peer.setUp(this.settings);
        } catch (DNFSException e) {
            LOGGER.error("Could not set up peer.", e);
            System.exit(-1);
        }
    }


    /**
     * 
     */
    public void start() {
        
        LOGGER.info("Starting DNFS with mountpoint \"" + this.settings.getMountPoint() + "\"");
        
        try {
            this.fuseIntegration.mount(this.settings.getMountPoint());
        } catch (FuseException e) {
            LOGGER.error("Failed to mount the fuse file system.");
            e.printStackTrace();
        }
    }

    
    /**
     * 
     */
    public void pause() {
        LOGGER.debug("DNFS has paused.");
    }

    
    /**
     * 
     */
    public void resume() {
        LOGGER.debug("DNFS has resumed.");
    }

    
    /**
     * 
     */
    public void shutDown() {
        LOGGER.debug("DNFS has shut down.");
    }

    
    /**
     * 
     */
    private void createRootFolder() {
        try {
            DNFSIiNode rootINode = this.getPeer().createRootINode();
            DNFSFolder.createNew(rootINode, this.getPeer());
        } catch (DNFSException e) {
            LOGGER.error("Could not create root folder.", e);
            return;
        }

    }
    
    
    public DNFSIPeer getPeer() {
        return peer;
    }

    public void setPeer(DNFSIPeer peer) {
        this.peer = peer;
    }
}
