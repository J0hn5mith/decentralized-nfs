package ch.uzh.csg.p2p.group_1;

import ch.uzh.csg.p2p.group_1.network.DNFSNetworkVDHT;
import ch.uzh.csg.p2p.group_1.utils.DNFSTestUtils;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by janmeier on 12.05.15.
 */
public class TestDNFSNetworkVDHTTest {
    private static final Random RND = new Random(42L);
    final int nrPeers = 100;
    final int port = 4001;

    DNFSNetworkVDHT network;
    PeerDHT master;

    PeerDHT[] peers;

    @Before
    public void setUp() {

        try {
            this.peers = DNFSTestUtils.createAndAttachPeersDHT(nrPeers,
                    port);
            DNFSTestUtils.bootstrap(peers);

//            this.network = new DNFSNetworkVDHT(new TestDNFSDummyNetwork(peers[1]));

            master = peers[0];
            Number160 nr = new Number160(RND);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @Test
    public void testVDHTPut() {
        try {
            FuturePut fp;
            Number160 key = Number160.ONE;
            fp = peers[0].put(key)
                    .data(Number160.ZERO, new Data("start -"))
                    .start()
                    .awaitUninterruptibly();
            fp = peers[0].put(key)
                    .data(Number160.ZERO, new Data("HELLO").prepareFlag())
                    .start()
                    .awaitUninterruptibly();

            final CountDownLatch cl = new CountDownLatch(1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        FuturePut fp = peers[0].put(Number160.ONE)
                                .versionKey(Number160.ZERO).putConfirm()
                                .data(new Data()).start().awaitUninterruptibly();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cl.countDown();
                }
            }).start();

            this.network.put(Number160.ONE, (Object) new Data("string to append."));
            System.out.print("Coul put data with vDHT");

            cl.await();

            FutureGet fg = this.peers[10].get(Number160.ONE).contentKey(Number160.ZERO).getLates(true).start().awaitUninterruptibly();

            System.out.println("res: "
                    + fg.rawData().values().iterator().next().values().iterator()
                    .next().object());
            System.out.println("got it: " + fg.failedReason());
            System.out.println("Data: " + (String) fg.data().object());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DNFSException.DNFSNetworkPutException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @After
    public void tearDown() {
        if (master != null) {
            master.shutdown();
        }
    }


}
