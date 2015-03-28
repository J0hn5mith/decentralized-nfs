package ch.uzh.csg.p2p.group_1;
/**
 * Created by janmeier on 23.03.15.
 */

import org.apache.log4j.*;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        LOGGER.setLevel(Level.DEBUG);
        DecentralizedNetFileSystem dnfs = new DecentralizedNetFileSystem();
        dnfs.start();

        while(true){

        }

//        LOGGER.info("Main method has started");

    }
}
