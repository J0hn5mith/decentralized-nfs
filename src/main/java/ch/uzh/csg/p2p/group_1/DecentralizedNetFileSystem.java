/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkSetupException;
import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.network.DNFSINetwork;
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
    private DNFSINetwork network;
    private DNFSIPeer peer;


    /**
     *
     */
    public DecentralizedNetFileSystem() {
        LOGGER.setLevel(Level.WARN);
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

            try {
                this.keyValueStorage = new FileBasedKeyValueStorage();
                String storageDirectory = settings.getFileBasedStorageDirectory();
                ((FileBasedKeyValueStorage) this.keyValueStorage).setDirectory(storageDirectory);
                this.network = new DNFSNetwork(this.settings.getPort(), this.keyValueStorage);
            } catch (DNFSNetworkSetupException e) {
                LOGGER.error("Could not set up the network.", e);
                System.exit(-1);
            } catch (Exception e) {
                LOGGER.error("Could not set up the file-based key-value storage.", e);
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
            String mountPoint = this.settings.getMountPoint();
            if(!Files.exists(Paths.get(mountPoint))) {
                File newDirectory = new File(mountPoint);
                newDirectory.mkdirs();
            }
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
