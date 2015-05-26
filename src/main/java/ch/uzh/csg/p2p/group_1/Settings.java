/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;

import java.net.InetSocketAddress;

public class Settings {

    static private class ConfigFilesKeys{
        static public String BLOCK_ENCRYPTION_KEY = "BlockEncryptionKey";
    }


    private XMLConfiguration config;
    private CommandLine cmd;

    private String mountPoint;
    private int port;
    private boolean startNewServer = false;
    private boolean useLocalStorage = false;
    private InetSocketAddress masterIP;
    private boolean useCustomStorageDirectory;
    private String customStorageDirectory;
    private boolean useVDHT = true;
    private String blockEncryptionKey = null;


    public Settings(String configFile, CommandLine cmd) throws DNFSException.DNFSSettingsException {

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
        this.setCustomStorageDirectory();
        this.setPort();
        this.setUseLocalStorage();
        this.setStartNewServer();
        this.setMasterIP();
        this.setUsevDHT();
        this.setBlockEncryptionKey();
    }

    public Configuration getConfig(){
        return this.config;
    }

    
    public int getPort() {
        return port;
    }

    
    public int getConnectionTimeOut(){
        return this.config.getInt("ConnectionTimeOut", 1000);
    }
    
    
    public int getCheckConnectionFrequency(){
        return this.config.getInt("CheckConnectionFrequency", 4);
    }
    
    
    public boolean getStartNewServer() {
        return this.startNewServer;
    }

    
    public String getMountPoint() {
        return mountPoint;
    }

    
    public boolean getUseLocalStorage() {
        return this.useLocalStorage;
    }

    
    public boolean getUseCustomStorageDirectory() {
        return useCustomStorageDirectory;
    }


    public String getCustomStorageDirectory() {

        return customStorageDirectory;
    }

    public boolean useBlockEncryption(){
        return this.blockEncryptionKey != null;
    }

    public String getBlockEncryptionKey(){
        return this.blockEncryptionKey;
    }


    public boolean useVDHT(){
        return this.useVDHT;
    }


    private void setUseLocalStorage() {
        if(cmd.hasOption("l")) {
            this.useLocalStorage = true;
        }
    }

    
    private void setStartNewServer() {
        if(cmd.hasOption("n")) {
            this.startNewServer = true;
        }

    }

    
    private void setPort() {
        if(cmd.hasOption("p")) {
            this.port = Integer.parseInt(cmd.getOptionValue('p'));
        }
    }

    
    private void setMountPoint() {
        if(cmd.hasOption("m")) {
            this.mountPoint = cmd.getOptionValue("m");
        } else {
            this.mountPoint = this.config.getString("MountPoint");
        }
    }
  
    
    private void setCustomStorageDirectory() {
        if(cmd.hasOption("s")) {
            this.customStorageDirectory = cmd.getOptionValue("s");
            this.useCustomStorageDirectory = true;
        } else {
            this.useCustomStorageDirectory = false;
        }
    }

    private void setMasterIP() throws DNFSException.DNFSSettingsException {
        String addressString = null;
        if (cmd.hasOption("a")) {
            addressString = cmd.getOptionValue("a");
        } else {
            addressString = this.config.getString("MasterIP");
        }
        if(addressString != null) {
            int port = Integer.parseInt(addressString.substring(addressString.lastIndexOf(":") + 1, addressString.length()));
            this.masterIP = new InetSocketAddress(addressString.substring(0, addressString.lastIndexOf(":")), port);
        }
        else{
            if (!this.getStartNewServer()){
                throw new DNFSException.DNFSSettingsException("Unabeld to set master IP");
            }
        }

    }


    private void setUsevDHT(){
        if (cmd.hasOption("v")) {
            this.useVDHT = Boolean.parseBoolean(cmd.getOptionValue("v"));
        } else {
            this.useVDHT = Boolean.parseBoolean(this.config.getString("UseVDHT"));
        }

    }

    private void setBlockEncryptionKey(){
        if(this.config.containsKey(ConfigFilesKeys.BLOCK_ENCRYPTION_KEY)){
            this.blockEncryptionKey = this.config.getString(ConfigFilesKeys.BLOCK_ENCRYPTION_KEY);
        }
    }

    
    public InetSocketAddress getMasterIP() {
        return this.masterIP;

    }

}
