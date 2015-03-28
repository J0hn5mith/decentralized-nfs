/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import net.fusejna.FuseException;
import org.apache.log4j.Logger;


public class DecentralizedNetFileSystem implements IDecentralizedNetFileSystem {

    private final Logger LOGGER = Logger.getLogger(this.getClass());
    private DNFSFuseIntegration  fuseIntegration;

    public DecentralizedNetFileSystem(){
        LOGGER.debug("DEBUG");
        this.fuseIntegration = new DNFSFuseIntegration();
    }

    public void setUp(){
        LOGGER.debug("DNFS has been set up.");
    }

    public void loadConfig(){
        LOGGER.debug("DNFS has loaded the configuration.");
    }

    public void start(){
        LOGGER.debug("DNFS has started.");

        try {
            this.fuseIntegration.mount("testfs");
        } catch (FuseException e) {
            e.printStackTrace();
            LOGGER.error("Failed to mount the fuse file system.");
        }
        LOGGER.info("The DNFS started successful.");
    }

    public void pause(){
        LOGGER.debug("DNFS has paused.");
    }

    public void resume(){
        LOGGER.debug("DNFS has resumed.");
    }

    public void shutDown(){
        LOGGER.debug("DNFS has shut down.");
    }
}
