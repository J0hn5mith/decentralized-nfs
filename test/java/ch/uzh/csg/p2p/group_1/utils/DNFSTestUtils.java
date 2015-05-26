/**
 * Created by janmeier on 12.05.15.
 */
    /*
 * Copyright 2009 Thomas Bocek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.uzh.csg.p2p.group_1.utils;

import java.util.Random;

import java.io.IOException;
import java.util.Random;

 import net.tomp2p.dht.PeerBuilderDHT;
 import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.tracker.PeerBuilderTracker;
import net.tomp2p.tracker.PeerTracker;

/**
 * This simple example creates 10 nodes, bootstraps to the first and put and get data from those 10 nodes.
 *
 * @author Thomas Bocek
 */
public class DNFSTestUtils {
    static final Random RND = new Random(42L);

    /**
     * Bootstraps peers to the first peer in the array.
     *
     * @param peers The peers that should be bootstrapped
     */
    public static void bootstrap(Peer[] peers) {
        //make perfect bootstrap, the regular can take a while
        for (int i = 0; i < peers.length; i++) {
            for (int j = 0; j < peers.length; j++) {
                peers[i].peerBean().peerMap().peerFound(peers[j].peerAddress(), null, null, null);
            }
        }
    }

    public static void bootstrap(PeerDHT[] peers) {
        //make perfect bootstrap, the regular can take a while
        for (int i = 0; i < peers.length; i++) {
            for (int j = 0; j < peers.length; j++) {
                peers[i].peerBean().peerMap().peerFound(peers[j].peerAddress(), null, null, null);
            }
        }
    }

    /**
     * Create peers with a port and attach it to the first peer in the array.
     *
     * @param nr   The number of peers to be created
     * @param port The port that all the peer listens to. The multiplexing is done via the peer Id
     * @return The created peers
     * @throws IOException IOException
     */
    public static Peer[] createAndAttachNodes(int nr, int port) throws IOException {
        Peer[] peers = new Peer[nr];
        for (int i = 0; i < nr; i++) {
            if (i == 0) {
                peers[0] = new PeerBuilder(new Number160(RND)).ports(port).start();
            } else {
                peers[i] = new PeerBuilder(new Number160(RND)).masterPeer(peers[0]).start();
            }
        }
        return peers;
    }

    public static PeerDHT[] createAndAttachPeersDHT(int nr, int port) throws IOException {
        PeerDHT[] peers = new PeerDHT[nr];
        for (int i = 0; i < nr; i++) {
            if (i == 0) {
                peers[0] = new PeerBuilderDHT(new PeerBuilder(new Number160(RND)).ports(port).start()).start();
            } else {
                peers[i] = new PeerBuilderDHT(new PeerBuilder(new Number160(RND)).masterPeer(peers[0].peer()).start()).start();
            }
        }
        return peers;
    }

    public static PeerTracker[] createAndAttachPeersTracker(PeerDHT[] peers) throws IOException {
        PeerTracker[] peers2 = new PeerTracker[peers.length];
        for (int i = 0; i < peers.length; i++) {
            peers2[i] = new PeerBuilderTracker(peers[i].peer()).verifyPeersOnTracker(false).start();
        }
        return peers2;
    }
}
