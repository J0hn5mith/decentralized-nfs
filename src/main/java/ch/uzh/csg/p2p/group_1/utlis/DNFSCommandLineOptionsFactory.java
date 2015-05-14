/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1.utlis;

import org.apache.commons.cli.*;

public class DNFSCommandLineOptionsFactory {
    
    
    public static Options getOptions() {
        Options options = new Options();
        options.addOption("p", "port", true, "overwrite the default port");
        options.addOption("n", "new-server", false, "start a new server");
        options.addOption("a", "ip-address", true, "provide the ip of an existing server");
        options.addOption("d", "dummy-peer", false, "use a dummy peer that requires no network connection");
        options.addOption("m", "mount-point", true, "specify a custom mount point");
        return options;
    }

}
