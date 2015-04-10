/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import net.fusejna.FuseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;


public class DecentralizedNetFileSystem implements IDecentralizedNetFileSystem {

    private final Logger LOGGER = Logger.getLogger(this.getClass());
    private DNFSFuseIntegration fuseIntegration;
    private DNFSPathResolver pathResolver;
    private DNFSConfigurator conf;

    /**
     * 
     */
    public DecentralizedNetFileSystem() {
        this.fuseIntegration = new DNFSFuseIntegration();
        LOGGER.debug("DEBUG");
    }
    
    /**
     * 
     */
    public void loadConfig(String configFile) {
        LOGGER.debug("DNFS has loaded the configuration.");
        this.conf = new DNFSConfigurator(configFile);
        try {
            this.conf.setUp();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            LOGGER.error("Could not load the configuration.");
        }
    }

    /**
     * 
     */
    public void setUp() {
        this.pathResolver = new DNFSPathResolver(this.conf);
        this.pathResolver.setUp();
        this.fuseIntegration.setPathRelsover(this.pathResolver);

        LOGGER.debug("DNFS has been set up.");
    }

<<<<<<< HEAD
=======
    /**
     * 
     */
>>>>>>> 63c702d1ad59eab29d0ad1006a760fa0726579d5
    public void start() {
        LOGGER.debug("DNFS has started.");

        try {
            String mountPoint = this.conf.getConfig().getString("MountPoint");
            this.fuseIntegration.mount(mountPoint);
        } catch (FuseException e) {
            e.printStackTrace();
            LOGGER.error("Failed to mount the fuse file system.");
        }
        LOGGER.info("The DNFS started successful.");
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
}
