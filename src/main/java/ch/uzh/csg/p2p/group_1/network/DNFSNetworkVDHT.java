package ch.uzh.csg.p2p.group_1.network;

import ch.uzh.csg.p2p.group_1.DNFSException;
import ch.uzh.csg.p2p.group_1.DNFSNetwork;
import net.tomp2p.peers.Number160;

/**
 * Created by janmeier on 10.05.15.
 */
public class DNFSNetworkVDHT extends DNFSNetwork {


    public static void put(Number160 key, Object data) throws DNFSException.DNFSNetworkPutException {
        while(!checkVersions(key) && setPrepare(key)){}
        confirm(key);
    }

    private static boolean checkVersions(Number160 key){
        return false;
    }

    private static boolean setPrepare(Number160 key){
        return false;
    }

    private static void confirm(Number160 key){
    }
}
