package ch.uzh.csg.p2p.group_1;
/**
 * Created by janmeier on 23.03.15.
 */

import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import org.apache.commons.cli.*;

public class Main {

    
    public static void main(String[] args) throws InterruptedException, ParseException {

        CommandLine cmd = parseCommandLineArguments(args);
        DNFSSettings settings = null;
        try {
            settings = new DNFSSettings("./conf/settings.xml", cmd);
        } catch (DNFSException.DNFSSettingsException e) {
            System.err.println("Could not set up settings.");
            e.printStackTrace();
            System.exit(-1);
        }

        DWARFS dwarfs = new DWARFS();
        dwarfs.setUp(settings);
        dwarfs.start();
    }

    
    /**
     * Uses GnuParser to parse command line arguments
     * 
     * @param args Command line arguments
     * @return 
     * @throws ParseException
     */
    private static CommandLine parseCommandLineArguments(String[] args) throws ParseException {
        GnuParser parser = new GnuParser();
        return parser.parse(DNFSCommandLineOptionsFactory.getOptions(), args);
    }
    
}
