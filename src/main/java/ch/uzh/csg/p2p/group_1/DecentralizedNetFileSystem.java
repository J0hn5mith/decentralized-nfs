/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import org.apache.log4j.Logger;

public class DecentralizedNetFileSystem {
    private final Logger LOGGER = Logger.getLogger(this.getClass());

    public DecentralizedNetFileSystem(){
        LOGGER.debug("DEBUG");
    }

    public void setUp(){
        LOGGER.debug("DNFS has been set up.");
    }

    public void loadConfig(){
        LOGGER.debug("DNFS has loaded the configuration.");

    }

    public void start(){
        LOGGER.debug("DNFS has started.");

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
