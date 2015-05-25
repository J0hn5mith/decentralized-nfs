/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkNotInit;
import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkSetupException;
import ch.uzh.csg.p2p.group_1.network.DNFSINetwork;
import ch.uzh.csg.p2p.group_1.network.DNFSNetworkVDHT;
import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;
import net.fusejna.FuseException;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.peers.PeerStatistic;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class DWARFS implements IDNFS {

    final private static Logger LOGGER = Logger.getLogger(DWARFS.class);

    private DNFSFuseIntegration fuseIntegration;
    private DNFSPathResolver pathResolver;
    private DNFSSettings settings;
    private IKeyValueStorage keyValueStorage;
    private DNFSINetwork network;
    private IStorage storage;
    private int connectionTimeOut;
    private int checkConnectionFrequency;
    private int checkConnectionInterval;
    private boolean _connectedToOtherPeers = false;    


    /**
     *
     */
    public DWARFS() {
        LOGGER.setLevel(Level.DEBUG);
    }


    /**
     *
     */
    public void setUp(DNFSSettings settings) {
        
        LOGGER.info("Setting up DWARFS File System...");
        this.fuseIntegration = new DNFSFuseIntegration();
        this.settings = settings;
        this.setConnectionTimeout();
        this.setUpStorage();
        this.pathResolver = new DNFSPathResolver(this.storage);
        this.fuseIntegration.setPathResolver(this.pathResolver);
        if(this.settings.getStartNewServer()) {
            this.createRootFolder();
        }
    }


    /**
     *
     */
    private void setUpStorage() {

        if(this.settings.getUseLocalStorage()) {
            this.storage = new LocalStorage();

        } else {
            try {
                this.setKeyValueStorage();
                this.setNetwork();
                this.setPeerChangeListener();

                if(!this.settings.getStartNewServer()) {
                    this.network.connectToNetwork(0, this.settings.getMasterIP().getHostString(), this.settings.getMasterIP().getPort());
                }
                
            } catch (DNFSNetworkSetupException e) {
                LOGGER.error("Could not set up the network.", e);
                System.exit(-1);
            } catch (DNFSException.DNFSKeyValueStorageException e) {
                LOGGER.error("Could not set up the file-based key-value storage.", e);
                System.exit(-1);
            }
            this.storage = new DNFSPeer(this.network, this.keyValueStorage);
        }

        try {
            this.storage.setUp(this.settings);
        } catch (DNFSException e) {
            LOGGER.error("Could not set up peer.", e);
            System.exit(-1);
        }
        
        this.storage.setConnectionTimeout(connectionTimeOut);
        startConnectionChecking();
    }


    private void setPeerChangeListener(){
        this.network.registerPeerChangeListener(new PeerMapChangeListener() {

            public void peerUpdated(PeerAddress peerAddress,PeerStatistic storedPeerAddress) {}

            public void peerRemoved(PeerAddress peerAddress,PeerStatistic storedPeerAddress) {
                /////////////////////////////
                //
                //  TODO
                //  Send lost copies of files
                //  to other nodes
                //
                /////////////////////////////

                System.out.println("Peer timed out: " + peerAddress);
            }

            public void peerInserted(PeerAddress peerAddress, boolean verified) {
                _connectedToOtherPeers = true;
            }
        });

    }
    
    
    /**
     * 
     */
    private void startInputScanner() {
        final DWARFS dwarfs = this;
        Thread thread = new Thread() {
            private Scanner scanner;
            public void run() {
                scanner = new Scanner(System.in);
                while(true) {
                    String command = scanner.next();
                    
                    if(command.equals("shutdown")) {
                        dwarfs.shutDown();
                        
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
        if(!Files.exists(Paths.get(mountPoint))) {
            File newDirectory = new File(mountPoint);
            newDirectory.mkdirs();
        }
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
        
        startInputScanner();
        
        System.out.println("DWARFS file system started.");
    }


    /**
     *
     */
    public void shutDown() {
        try {
            this.keyValueStorage.shutDown();
            this.storage.shutdown();
        } catch (DNFSException.DNFSKeyValueStorageException e) {
            LOGGER.error("Could not remove temporary folder of the file-based key-value storage.", e);
        } catch (DNFSNetworkNotInit e) {
            LOGGER.error("Peer not initialized", e);
        }
        System.out.println("DWARFS file system shut down.");
        System.exit(0);
    }


    /**
     *
     */
    private void createRootFolder() {
        try {
            DNFSFolder.createRoot(storage);
        } catch (DNFSException e) {
            LOGGER.error("Could not create root folder.", e);
            return;
        }

    }

    private void startConnectionChecking() {
    	final DWARFS dwarfs = this;
        new Thread() {
            public void run() {
                int failedChecks = 0;
                boolean checking = true;
                while(checking){
                    try {
                        if(_connectedToOtherPeers && !storage.isConnected()) {    
                            failedChecks++;
                            
                            if(failedChecks >= checkConnectionFrequency) {
                                System.out.println("Lost connection to all peers.");
                                dwarfs.shutDown();
                                checking = false;
                            }
                        } else {
                            failedChecks = 0;
                        }
                    } catch (DNFSNetworkNotInit e) {
                        LOGGER.error("Network not initialized:" + e);
                    }
                    try {
                        Thread.sleep(checkConnectionInterval);
                    } catch (InterruptedException e) {
                        LOGGER.error("Connection checking was interrupted:" + e);
                    }
                }
            }
          }.start();
    }
    
    
    private void setConnectionTimeout() {
        this.connectionTimeOut = this.settings.getConnectionTimeOut();
        this.checkConnectionFrequency = this.settings.getCheckConnectionFrequency();
        
        if(checkConnectionFrequency != 0) {
            this.checkConnectionInterval = this.connectionTimeOut/this.checkConnectionFrequency;
        } else {
            this.checkConnectionInterval = this.connectionTimeOut;
        }
    }

    private void setNetwork() throws DNFSNetworkSetupException {
        if(this.settings.useVDHT()){
            this.network = new DNFSNetworkVDHT(this.settings.getPort(), this.keyValueStorage);
            LOGGER.info("Network WITH vDHT is used.");
        } else {
            this.network = new DNFSNetwork(this.settings.getPort(), this.keyValueStorage);
            LOGGER.info("Network without vDHT is used.");
        }
    }

    private void setKeyValueStorage() throws DNFSException.DNFSKeyValueStorageException {
        if(this.settings.getUseCustomStorageDirectory()) {
            this.keyValueStorage = new FileBasedKeyValueStorage(this.settings.getCustomStorageDirectory());
        } else {
            this.keyValueStorage = new FileBasedKeyValueStorage();
        }
        this.keyValueStorage.startUp();
    }

}
