/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1.utlis;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.XMLConfiguration;

public class DNFSSettings {
    

    private XMLConfiguration config;
    private CommandLine cmd;

    private String mountPoint;
    private int port;
    private boolean startNewServer = false;
    private boolean useDummyPeer = false;

    
    public DNFSSettings(String configFile, CommandLine cmd) {
        
        this.config = new XMLConfiguration();
        this.config.setFileName(configFile);
        this.config.setValidating(false); // We don't have a dtd schema for now
        try {
            this.config.load();
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        this.cmd = cmd;
        
        this.setMountPoint();
        this.setPort();
        this.setUseDummyPeer();
        this.setStartNewServer();

    }

    public int getPort() {
        return port;
    }

    public boolean getStartNewServer(){
        return this.startNewServer;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public boolean getUseDummyPeer() {
        return this.useDummyPeer;
    }
   
    
    public String getFileBasedStorageDirectory() {
        return this.config.getString("FileBasedStorageDirectory");
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
            this.mountPoint = this.config.getString("MountPoint");
        }
    }
}
