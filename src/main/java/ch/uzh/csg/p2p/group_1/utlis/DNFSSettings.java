/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1.utlis;
import ch.uzh.csg.p2p.group_1.DNFSConfigurator;
import org.apache.commons.cli.CommandLine;

import javax.naming.ConfigurationException;

public class DNFSSettings {
    private CommandLine cmd;
    private DNFSConfigurator conf;


    private String mountPoint;
    private int port;
    private boolean startNewServer = false;
    private boolean useDummyPeer = false;

    public DNFSSettings(String configFile, CommandLine cmd) {
        this.cmd = cmd;
        this.conf = new DNFSConfigurator(configFile);
        try {
            this.conf.setUp();
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            e.printStackTrace();
        }

        this.setMountPoint();
        this.setPort();
        this.setUseDummyPeer();
        this.setStartNewServer();

    }

    public int getPort() {
        return port;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public boolean getUseDummyPeer() {
        return this.useDummyPeer;
    }
   
    
    public String getFileBasedStorageDirectory() {
        return this.conf.getConfig().getString("FileBasedStorageDirectory");
    }


    private void setUseDummyPeer(){
        if (cmd.hasOption("d")) {
            this.useDummyPeer = true;
        }
    }

    private void setStartNewServer(){
        if (cmd.hasOption("n")) {
            this.startNewServer = true;
        }

    }

    private void setPort() {
        if (cmd.hasOption("p")) {
            this.port = Integer.parseInt(cmd.getOptionValue('p'));
        }
    }

    private void setMountPoint(){
        if(cmd.hasOption("m")){
            this.mountPoint = cmd.getOptionValue("m");
        }
        else{
            this.mountPoint = this.conf.getConfig().getString("MountPoint");
        }
    }
}
