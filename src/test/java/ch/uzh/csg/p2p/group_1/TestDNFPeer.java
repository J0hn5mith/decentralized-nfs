package ch.uzh.csg.p2p.group_1;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.storage.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by janmeier on 02.04.15.
 */
public class TestDNFPeer {
    private final String fileName = "test.txt";
    private DNFSPeer peer;

    @Before
    public void setUp(){
        this.peer = new DNFSPeer();
        try {
            this.peer.setUp();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testBasicFunctionality(){
        try {
            this.peer.putFile(this.fileName, "Hello World").awaitUninterruptibly();
            FutureGet result = this.peer.getFile(this.fileName).awaitUninterruptibly();
            Data data = result.data();


            DNFSData<String> dnfsData = (DNFSData<String>) data.object();
            String result_data = dnfsData.getData();

            Assert.assertEquals("Hello World", result_data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
