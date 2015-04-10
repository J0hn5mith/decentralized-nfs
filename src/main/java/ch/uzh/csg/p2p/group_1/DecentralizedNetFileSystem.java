/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import net.fusejna.FuseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;


public class DecentralizedNetFileSystem implements IDecentralizedNetFileSystem {

    private DNFSFuseIntegration fuseIntegration;
    private DNFSPathResolver pathResolver;
    private DNFSConfigurator conf;

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
    }

    /**
     * 
     */
    public void setUp() {
        this.pathResolver = new DNFSPathResolver(this.conf);
        this.pathResolver.setUp();
        this.fuseIntegration.setPathResolver(this.pathResolver);

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
