/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import java.util.Scanner;

import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkSetupException;
import ch.uzh.csg.p2p.group_1.filesystem.DNFSIiNode;
import ch.uzh.csg.p2p.group_1.network.DNFSINetwork;
import ch.uzh.csg.p2p.group_1.network.DNFSNetworkVDHT;
import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;
import net.fusejna.FuseException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class DecentralizedNetFileSystem implements IDecentralizedNetFileSystem {

    final private static Logger LOGGER = Logger.getLogger(DecentralizedNetFileSystem.class);

    private DNFSFuseIntegration fuseIntegration;
    private DNFSPathResolver pathResolver;
    private DNFSSettings settings;
    private IKeyValueStorage keyValueStorage;
    private DNFSINetwork network;
    private DNFSIPeer peer;


    /**
     *
     */
    public DecentralizedNetFileSystem() {
        LOGGER.setLevel(Level.DEBUG);
    }


    /**
     *
     */
    public void setUp(DNFSSettings settings) {
        
        System.out.println("Setting up DWARFS file system...");
        
        this.fuseIntegration = new DNFSFuseIntegration();

        this.settings = settings;

        this.setUpPeer();

        this.pathResolver = new DNFSPathResolver(this.peer);
        this.fuseIntegration.setPathResolver(this.pathResolver);

        if(this.settings.getStartNewServer()) {
            this.createRootFolder();
        }
    }


    /**
     *
     */
    private void setUpPeer() {

        if(this.settings.getUseDummyPeer()) {
            this.peer = new DNFSDummyPeer();

        } else {

            try {
                if(this.settings.getUseCustomStorageDirectory()) {
                    this.keyValueStorage = new FileBasedKeyValueStorage(this.settings.getCustomStorageDirectory());
                } else {
                    this.keyValueStorage = new FileBasedKeyValueStorage();
                }
                this.keyValueStorage.startUp();
                
                if(this.settings.useVDHT()){
                    this.network = new DNFSNetworkVDHT(this.settings.getPort(), this.keyValueStorage);
                } else {
                    this.network = new DNFSNetwork(this.settings.getPort(), this.keyValueStorage);
                }

                if(!this.settings.getStartNewServer()){
                    this.network.connectToNetwork(0, this.settings.getMasterIP().getHostString(), this.settings.getMasterIP().getPort());
                }
                
            } catch (DNFSNetworkSetupException e) {
                LOGGER.error("Could not set up the network.", e);
                System.exit(-1);
            } catch (Exception e) {
                LOGGER.error("Could not set up the file-based key-value storage.", e);
                System.exit(-1);
            }
            this.peer = new DNFSPeer(this.network, this.keyValueStorage);
        }

        try {
            this.peer.setUp(this.settings);
        } catch (DNFSException e) {
            LOGGER.error("Could not set up peer.", e);
            System.exit(-1);
        }
        
        startInputScanner();
    }
    
    
    /**
     * 
     */
    private void startInputScanner() {
        final DecentralizedNetFileSystem dnfs = this;
        Thread thread = new Thread() {
            private Scanner scanner;
            public void run() {
                scanner = new Scanner(System.in);
                while(true) {
                    String command = scanner.next();
                    
                    if(command.equals("shutdown")) {
                        dnfs.shutDown();
                        
                    } else {
                        System.out.println("Unknown command: " + command);
                        System.out.println("Valid command:");
                        System.out.println("\tshutdown : Unmount and shut down DWARFS file system");
                    }
                }
            }
        };
        thread.start();
    }


    /**
     *
     */
    public void start() {
        
        final String mountPoint = this.settings.getMountPoint();
        final DNFSFuseIntegration fuseIntegration = this.fuseIntegration;

        Thread thread = new Thread() {
            public void run() {
                try {
                    fuseIntegration.mount(mountPoint);
                } catch (FuseException e) {
                    LOGGER.error("Failed to mount the fuse file system.");
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        
        System.out.println("DWARFS file system started.");
    }


    /**
     *
     */
    public void shutDown() {
        try {
            this.keyValueStorage.shutDown();
        } catch (Exception e) {
            LOGGER.error("Could not remove temporary folder of the file-based key-value storage.", e);
        }
        System.out.println("DWARFS file system shut down.");
        System.exit(0);
    }


    /**
     *
     */
    private void createRootFolder() {
        try {
            DNFSIiNode rootINode = this.getPeer().createRootINode();
            DNFSFolder.createNew(rootINode, this.getPeer());
        } catch (DNFSException e) {
            LOGGER.error("Could not create root folder.", e);
            return;
        }

    }


    public DNFSIPeer getPeer() {
        return peer;
    }

}
