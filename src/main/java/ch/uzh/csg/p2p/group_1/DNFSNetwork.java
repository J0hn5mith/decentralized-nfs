package ch.uzh.csg.p2p.group_1;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;


public class DNFSNetwork {
    
    
    private static Random _random;
    
    private static int _port = 0;
    
    private static boolean _connected = false;
    private static PeerDHT _peer;

    public PeerDHT getPeer(){
        return this._peer;
    }
    
    /**
     * 
     * @param port
     * @throws DNFSException.DNFSNetworkSetupException
     */
    public static void createNetwork(int port) throws DNFSException.DNFSNetworkSetupException {
        _random = new Random(System.currentTimeMillis());
        setupPeer(port);
        
        _connected = true;
    }
    
    
    /**
     * 
     * @param port
     * @param masterIpAddress
     * @param masterPort
     * @throws DNFSException.DNFSNetworkSetupException
     */
    public static void connectToNetwork(int port, String masterIpAddress, int masterPort) throws DNFSException.DNFSNetworkSetupException {
        
        _random = new Random(System.currentTimeMillis());
        setupPeer(port);
        
        try {
            InetAddress masterInetAddress = InetAddress.getByName(masterIpAddress);
            PeerAddress masterAddress = new PeerAddress(Number160.ZERO, masterInetAddress, masterPort, masterPort);
            
            FutureDiscover futureDiscover = _peer.peer().discover().peerAddress(masterAddress).start();
            futureDiscover.awaitUninterruptibly();
            if(!futureDiscover.isSuccess()) {
                throw new DNFSException.DNFSNetworkSetupException("Discover failed because peer is probably behind a NAT: " + futureDiscover.failedReason());
            }
            
            PeerAddress bootstrapAddress = futureDiscover.reporter();
            FutureBootstrap futureBootstrap = _peer.peer().bootstrap().peerAddress(bootstrapAddress).start();
            futureBootstrap.awaitUninterruptibly();
            if(!futureBootstrap.isSuccess()) {
                throw new DNFSException.DNFSNetworkSetupException("Failed to connect");
            }
            
        } catch(Exception e) {
            throw new DNFSException.DNFSNetworkSetupException(e.getMessage());
        }

        _connected = true;
    }
    
    
    /**
     * 
     * @param port
     * @throws DNFSException.DNFSNetworkSetupException
     */
    private static void setupPeer(int port) throws DNFSException.DNFSNetworkSetupException {
        
        try {
            _port = port;
            
            Number160 key = Number160.createHash(_random.nextLong());
            
            PeerBuilder builder = new PeerBuilder(key).ports(_port);
            _peer = new PeerBuilderDHT(builder.start()).start();
            
        } catch(IOException e) {
            throw new DNFSException.DNFSNetworkSetupException("IOException: " + e.getMessage());
        }
    }
    

    /**
     * 
     * @param key
     * @return
     * @throws DNFSException.DNFSNetworkGetException
     */
    public static boolean keyExists(Number160 key) throws DNFSException.DNFSNetworkGetException {
        return get(key) != null;
    }
    
    
    /**
     * 
     * @return
     * @throws DNFSException.DNFSNetworkGetException
     */
    public static Number160 getUniqueKey() throws DNFSException.DNFSNetworkGetException {
        Number160 key = Number160.createHash(_random.nextLong());
        while(keyExists(key)) {
            key = Number160.createHash(_random.nextLong());
        }
        return key;
    }
    
    
    /**
     * 
     * @param key
     * @param data
     * @throws DNFSException.DNFSNetworkPutException
     */
    public static void put(Number160 key, Object data) throws DNFSException.DNFSNetworkPutException {
        if(_connected) {
            try {
                FuturePut futurePut = _peer.put(key).data(new Data(data)).start();
                futurePut.awaitUninterruptibly();
                if(!futurePut.isSuccess()) {
                    throw new DNFSException.DNFSNetworkPutException("Could not put data.");
                }
            } catch(IOException e) {
                throw new DNFSException.DNFSNetworkPutException("IOException: " + e.getMessage());
            }
        } else {
            throw new DNFSException.DNFSNetworkPutException("Not connected to network.");
        }
    }


    /**
     * 
     * @param key
     * @return
     * @throws DNFSException.DNFSNetworkGetException
     */
    public static Object get(Number160 key) throws DNFSException.DNFSNetworkGetException {
        if(_connected) {
            try {
                FutureGet futureGet = _peer.get(key).start();
                futureGet.awaitUninterruptibly();
                if(futureGet.isSuccess() && !futureGet.isEmpty()) {
                    return futureGet.data().object();
                } else {
                    throw new DNFSException.DNFSNetworkGetException("Could not get data.");
                }
            } catch(IOException e) {
                throw new DNFSException.DNFSNetworkGetException("IOException: " + e.getMessage());
            } catch(ClassNotFoundException e) {
                throw new DNFSException.DNFSNetworkGetException("ClassNotFoundException: " + e.getMessage());
            }
        } else {
            throw new DNFSException.DNFSNetworkGetException("Not connected to network.");
        }
    }
    
    
    /**
     * 
     * @param key
     * @throws DNFSException.DNFSNetworkDeleteException
     */
    public static void delete(Number160 key) throws DNFSException.DNFSNetworkDeleteException {
        if(_connected) {
            FutureRemove futureRemove = _peer.remove(key).start();
            futureRemove.awaitUninterruptibly();
            if(!futureRemove.isSuccess()) {
                throw new DNFSException.DNFSNetworkDeleteException("Could not delete data.");
            }
        } else {
            throw new DNFSException.DNFSNetworkDeleteException("Not connected to network.");
        }
    }
   
   
    /**
     * 
     * @param key
     * @return
     */
    public static PeerAddress getFirstResponder(Number160 key) throws DNFSException.DNFSNetworkGetException {
        if(_connected) {
            FutureGet futureGet = _peer.get(key).start();
            futureGet.awaitUninterruptibly();
            if(futureGet.isSuccess() && !futureGet.isEmpty()) {
                
                PeerAddress responder = futureGet.rawData().entrySet().iterator().next().getKey();
                
                // ZU TESTZWECKEN (to make sure other peer answers)
                Iterator<Entry<PeerAddress, Map<Number640, Data>>> x = futureGet.rawData().entrySet().iterator();
                while(x.hasNext() && _peer.peerAddress().equals(responder)) {
                    responder = x.next().getKey();
                }      
                // ENDE ZU TESTZWECKEN 
                return responder;
                
            } else {
                throw new DNFSException.DNFSNetworkGetException("Could not get response.");
            }
        } else {
            throw new DNFSException.DNFSNetworkGetException("Not connected to network.");
        }
    }
    
}