/**
 * Created by janmeier on 28.03.15.
 */

package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkSetupException;
import ch.uzh.csg.p2p.group_1.utlis.DNFSSettings;
import net.fusejna.FuseException;
import net.tomp2p.peers.Number160;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class DecentralizedNetFileSystem implements IDecentralizedNetFileSystem {

    final private static Logger LOGGER = Logger.getLogger(DecentralizedNetFileSystem.class);

    private DNFSFuseIntegration fuseIntegration;
    private DNFSPathResolver pathResolver;
    private DNFSSettings settings;
    private DNFSNetwork network;
    private DNFSIPeer peer;
    

    /**
     * 
     */
    public DecentralizedNetFileSystem() {
        LOGGER.setLevel(Level.INFO);
        this.fuseIntegration = new DNFSFuseIntegration();
    }
    
    
    /**
     * 
     */
    public void setUp(DNFSSettings settings) {
        
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
                this.network = new DNFSNetwork(this.settings.getPort());
            } catch (DNFSNetworkSetupException e) {
                LOGGER.error("Could not set up the network.", e);
                System.exit(-1);
            }
            this.peer = new DNFSPeer(this.network);
        }

        try {
            this.peer.setUp(this.settings);
        } catch (DNFSException e) {
            LOGGER.error("Could not set up peer.", e);
            System.exit(-1);
        }
    }


    /**
     * 
     */
    public void start() {
        
        LOGGER.info("Starting DNFS with mountpoint \"" + this.settings.getMountPoint() + "\"");
        
        Thread thread = new Thread() { // TODO Remove after testing
            public void run(){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                testitest();
            }
        };
        thread.start();
        
        try {
            this.fuseIntegration.mount(this.settings.getMountPoint());
        } catch (FuseException e) {
            LOGGER.error("Failed to mount the fuse file system.");
            e.printStackTrace();
        }
    }
    
    
    /**
     * TODO: this is just for testing
     */
    private void testitest() {
        System.out.println("---------- Started testing");
        try {
       
            DNFSBlock block1 = peer.createBlock();
            Number160 key = block1.getId();
            System.out.println("Created: " + block1.getId());
            
            String mydata = "Hallihallo";
            DNFSBlock block2 = new DNFSBlock(key, mydata.getBytes());
            peer.updateBlock(block2);
            
            DNFSBlock block3 = peer.getBlock(key);
            String string1 = new String(block3.getByteArray());
            System.out.println("Got Content: " + string1);
            
            
            
            
            
            
        } catch(Throwable e) {
            e.printStackTrace();
        }
        System.out.println("----------- Testing finished");
    }

    
    /**
     * 
     */
    public void pause() {
        LOGGER.debug("DNFS has paused.");
    }

    
    /**
     * 
     */
    public void resume() {
        LOGGER.debug("DNFS has resumed.");
    }

    
    /**
     * 
     */
    public void shutDown() {
        LOGGER.debug("DNFS has shut down.");
    }

    
    /**
     * 
     */
    private void createRootFolder() {
        try {
            DNFSiNode rootINode = this.getPeer().createRootINode();
            DNFSFolder.createNew(rootINode, this.getPeer());
        } catch (DNFSException e) {
            LOGGER.error("Could not create root folder.", e);
            return;
        }

    }
    
    
    public DNFSIPeer getPeer() {
        return peer;
    }

    public void setPeer(DNFSIPeer peer) {
        this.peer = peer;
    }
}
