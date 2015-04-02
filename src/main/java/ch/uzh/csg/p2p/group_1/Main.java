package ch.uzh.csg.p2p.group_1;
/**
 * Created by janmeier on 23.03.15.
 */

import org.apache.log4j.*;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {

        LOGGER.setLevel(Level.DEBUG);
        DecentralizedNetFileSystem dnfs = new DecentralizedNetFileSystem();
        dnfs.loadConfig("./conf/settings.xml");
        dnfs.setUp();
        dnfs.start();

        while(true){
            Thread.sleep(500);

        }


    }
}
