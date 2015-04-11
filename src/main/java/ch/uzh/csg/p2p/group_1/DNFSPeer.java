package ch.uzh.csg.p2p.group_1;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.Random;

/**
 * Created by janmeier on 02.04.15.
 */
public class DNFSPeer {
	
    private PeerDHT peer;

    /**
     * 
     */
    public DNFSPeer() {
    }

    /**
     * 
     * @throws IOException
     */
    public void setUp() throws IOException {
        final Random RND = new Random(42L);
        this.peer = new PeerBuilderDHT(new PeerBuilder(new Number160(RND)).ports(6111988).start()).start();
    }

    /**
     * 
     * @return
     * @throws DNFSException
     */
    public DNFSiNode getRootINode() throws DNFSException {
        return new DNFSiNode();
    }

    /**
     * 
     * @param iNodeID
     * @return
     * @throws DNFSException
     */
    public DNFSiNode getINode(Number160 iNodeID) throws DNFSException {
        return new DNFSiNode();

    }

    public DNFSBlock getBlock(Number160 id){

        if(id.equals(Number160.createHash(1000))){
            return new DNFSBlock("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc,");

        }
        else if (id.equals(Number160.createHash(1))){
            return new DNFSBlock("1 ./\n2 hello.txt\n3 heeey.txt\n4 another_file.txt");
        }
        return new DNFSBlock("1 ./\n2 hello.txt\n3 heeey.txt\n4 another_file.txt");
    }

    /**
     * 
     * @param path
     * @param file
     * @return
     * @throws IOException
     */
    public FuturePut putFile(String path, String file) throws IOException {
        DNFSData<String> data = new DNFSData<String>(path, file);
        return peer.put(this.createKey(path)).data(new Data(data)).start();
    }

    /**
     * 
     * @param path
     * @return
     */
    public FutureGet getFile(String path) {
        return this.peer.get(this.createKey(path)).start();
    }

    /**
     * 
     * @param key
     * @return
     */
    private Number160 createKey(String key) {
        return Number160.createHash(key);
    }

}
