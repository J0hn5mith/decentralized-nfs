package ch.uzh.csg.p2p.group_1;
/**
 * Created by janmeier on 23.03.15.
 */

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        PropertyConfigurator.configure("src/log4j.properties");
        MDC.put("User", "test user 22");
        MDC.put("OS", "Linux");

//        logger.info("Info die 2.");
        try{
            throw new Exception("Exception_1");
        }
        catch (Exception e){
            logger.error("Error_2", e);
        }
//        logger.debug("DEBUG");
//        logger.warn("Warn");
//        System.out.print("Done");
    }

}
