package ch.uzh.csg.p2p.group_1;
/**
 * Created by janmeier on 23.03.15.
 */

import org.apache.commons.cli.*;
import org.apache.log4j.*;

public class Main {
    static CommandLineParser parser;
    static CommandLine cmd;
    static Options options;

    public static final Logger LOGGER = Logger.getLogger(Main.class);

    public static Options getOptions(){
        if (options == null){
            options = new Options();
            options.addOption("p", "port", true, "overwrite the default port");
            options.addOption("n", "new-server", false, "start a new server");
            options.addOption("a", "ip-address", true, "provide the ip of an existing server");
            options.addOption("d", "dummy-peer", true, "use a dummy peer that requires no network connection");
        }
        return options;

    }

    public static void main(String[] args) throws InterruptedException, ParseException {
        parseCommandLineArguments(args);

        boolean startNewServer = cmd.hasOption('n');
        int portOverwrite = -1;
        int ipOverwrite = -1;
        if(cmd.hasOption('p')){
             portOverwrite = Integer.parseInt(cmd.getOptionValue('p'));
        }
        if(cmd.hasOption('a')){
            ipOverwrite = Integer.parseInt(cmd.getOptionValue('a'));
        }

        LOGGER.setLevel(Level.WARN);
        DecentralizedNetFileSystem dnfs = new DecentralizedNetFileSystem();

        if (startNewServer){
            // Do what is needed
            LOGGER.warn("Handling the start new server flag is not implemented.");
        }
        if (portOverwrite != -1){
            // Do what is needed
            LOGGER.warn("Port overwriting is not yet implemented. Port is " + cmd.getOptionValue("p"));
        }
        if (portOverwrite != -1){
            // Do what is needed
            LOGGER.warn("IP overwriting is not yet implemented. IP-Address is " + cmd.getOptionValue("a"));
        }
        dnfs.setUp("./conf/settings.xml", cmd );
        dnfs.start();


    }

    private static void parseCommandLineArguments(String[] args) throws ParseException {
        parser = new GnuParser();
        cmd = parser.parse( getOptions(), args);

    }
}
