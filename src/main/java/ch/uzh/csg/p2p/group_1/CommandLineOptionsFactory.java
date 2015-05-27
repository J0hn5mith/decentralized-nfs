/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1;

import org.apache.commons.cli.*;

public class CommandLineOptionsFactory {
    
    
    public static Options getOptions() {
        Options options = new Options();

        options.addOption("p", "port", true, "overwrite the default port");
        options.addOption("n", "new-server", false, "start a new server");
        options.addOption("a", "ip-address", true, "provide the ip of an existing server");
        options.addOption("l", "local-storage", false, "Use a local storage that requires no network connection");
        options.addOption("m", "mount-point", true, "specify a custom mount point");
        options.addOption("s", "storage-directory", true, "specify a custom directory for the temporary storage");
        options.addOption("v", "vdht", true, "specify if vDHT should be used.");
        options.addOption("t", "terminal-commands", false, "allow terminal commands.");

        return options;
    }

}
