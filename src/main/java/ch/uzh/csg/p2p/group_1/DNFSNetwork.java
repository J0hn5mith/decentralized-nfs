package ch.uzh.csg.p2p.group_1;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkNoConnection;
import ch.uzh.csg.p2p.group_1.DNFSException.DNFSNetworkSendException;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageLayer;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.Storage;


public class DNFSNetwork {


    private Random _random;

    private int _port = 0;

    private boolean _connected = false;
    private PeerDHT _peer;


    /**
     * @param port
     * @throws DNFSException.DNFSNetworkSetupException
     */
    public DNFSNetwork(int port, KeyValueStorageInterface keyValueStorage) throws DNFSException.DNFSNetworkSetupException {
        _random = new Random(System.currentTimeMillis());
        setupPeer(port, keyValueStorage);
        this._connected = true;
    }
    
    
    /**
     * 
     * @param reply
     */
    public void registerObjectDataReply(ObjectDataReply reply) {
        _peer.peer().objectDataReply(reply);
    }


    /**
     * @param port
     * @param masterIpAddress
     * @param masterPort
     * @throws DNFSException.DNFSNetworkSetupException
     */
    public void connectToNetwork(int port, String masterIpAddress, int masterPort) throws DNFSException.DNFSNetworkSetupException {
        this._connected = false;

        try {
            InetAddress masterInetAddress = InetAddress.getByName(masterIpAddress);
            PeerAddress masterAddress = new PeerAddress(Number160.ZERO, masterInetAddress, masterPort, masterPort);

            FutureDiscover futureDiscover = _peer.peer().discover().peerAddress(masterAddress).start();
            futureDiscover.awaitUninterruptibly();
            if (!futureDiscover.isSuccess()) {
                throw new DNFSException.DNFSNetworkSetupException("Discover failed because peer is probably behind a NAT: " + futureDiscover.failedReason());
            }

            PeerAddress bootstrapAddress = futureDiscover.reporter();
            FutureBootstrap futureBootstrap = _peer.peer().bootstrap().peerAddress(bootstrapAddress).start();
            futureBootstrap.awaitUninterruptibly();
            if (!futureBootstrap.isSuccess()) {
                throw new DNFSException.DNFSNetworkSetupException("Failed to connect");
            }

        } catch (Exception e) {
            throw new DNFSException.DNFSNetworkSetupException(e.getMessage());
        }

        this._connected = true;
    }


    /**
     * @param port
     * @throws DNFSException.DNFSNetworkSetupException
     */
    private void setupPeer(int port, KeyValueStorageInterface keyValueStorage) throws
            DNFSException.DNFSNetworkSetupException {

        try {
            _port = port;

            Number160 key = Number160.createHash(_random.nextLong());

            PeerBuilder builder = new PeerBuilder(key).ports(_port);
            PeerBuilderDHT builderDHT =  new PeerBuilderDHT(builder.start());
            Storage storage = new StorageMemory();
            StorageLayer storageLayer = new DNFSStorageLayer(storage, this, keyValueStorage);
            _peer = builderDHT.storageLayer(storageLayer).start();

        } catch (IOException e) {
            throw new DNFSException.DNFSNetworkSetupException("IOException: " + e.getMessage());
        }
    }


    /**
     * @param key
     * @return
     * @throws DNFSException.DNFSNetworkGetException
     */
    public boolean keyExists(Number160 key) throws
            DNFSException.DNFSNetworkNoConnection {
        
        connectionBouncer();
        
        try {
            return get(key) != null;
        } catch (DNFSException.DNFSNetworkGetException e) {
            return false;
        }
    }


    /**
     * @return
     * @throws DNFSException.DNFSNetworkGetException
     */
    public Number160 getUniqueKey() throws
            DNFSException.DNFSNetworkNoConnection {
        
        connectionBouncer();
        
        Number160 key = Number160.createHash(_random.nextLong());
        while (keyExists(key)) {
            key = Number160.createHash(_random.nextLong());
        }
        return key;
    }


    /**
     * @param key
     * @param data
     * @throws DNFSException.DNFSNetworkPutException
     */
    public void put(Number160 key, Object data) throws
            DNFSException.DNFSNetworkNoConnection,
            DNFSException.DNFSNetworkPutException {
        
        connectionBouncer();
        
        try {
            FuturePut futurePut = _peer.put(key).data(new Data(data)).start();
            futurePut.awaitUninterruptibly();
            if (!futurePut.isSuccess()) {
                throw new DNFSException.DNFSNetworkPutException("Could not put data.");
            }
        } catch (IOException e) {
            throw new DNFSException.DNFSNetworkPutException("IOException: " + e.getMessage());
        }
    }


    /**
     * @param key
     * @return
     * @throws DNFSException.DNFSNetworkGetException
     */
    public Object get(Number160 key) throws
            DNFSException.DNFSNetworkNoConnection,
            DNFSException.DNFSNetworkGetException {
        
        connectionBouncer();
        
        try {
            FutureGet futureGet = _peer.get(key).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess() && !futureGet.isEmpty()) {
                return futureGet.data().object();
            } else {
                throw new DNFSException.DNFSNetworkGetException("Could not get data.");
            }
        } catch (IOException e) {
            throw new DNFSException.DNFSNetworkGetException("IOException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new DNFSException.DNFSNetworkGetException("ClassNotFoundException: " + e.getMessage());
        }
    }


    /**
     * @param key
     * @throws DNFSException.DNFSNetworkDeleteException
     */
    public void delete(Number160 key) throws
            DNFSException.DNFSNetworkNoConnection,
            DNFSException.DNFSNetworkDeleteException {
        
        connectionBouncer();
        
        FutureRemove futureRemove = _peer.remove(key).start();
        futureRemove.awaitUninterruptibly();
        if(!futureRemove.isSuccess()) {
            throw new DNFSException.DNFSNetworkDeleteException("Could not delete data.");
        }
    }


    /**
     * @param key
     * @return
     */
    public PeerAddress getFirstResponder(Number160 key) throws
            DNFSException.DNFSNetworkNoConnection,
            DNFSException.DNFSNetworkGetException {
        
        connectionBouncer();
        
        FutureGet futureGet = _peer.get(key).start();
        futureGet.awaitUninterruptibly();
        if(futureGet.isSuccess() && !futureGet.isEmpty()) {

            PeerAddress responder = futureGet.rawData().entrySet().iterator().next().getKey();

            // ZU TESTZWECKEN (to make sure other peer answers)
            Iterator<Entry<PeerAddress, Map<Number640, Data>>> x = futureGet.rawData().entrySet().iterator();
            while (x.hasNext() && _peer.peerAddress().equals(responder)) {
                responder = x.next().getKey();
            }
            // ENDE ZU TESTZWECKEN
            return responder;

        } else {
            throw new DNFSException.DNFSNetworkGetException("Could not get response.");
        }
    }
    
    
    /**
     * @param key
     * @return
     */
    public ArrayList<PeerAddress> getAllResponders(Number160 key) throws
            DNFSException.DNFSNetworkNoConnection,
            DNFSException.DNFSNetworkGetException {
        
        connectionBouncer();
        
        FutureGet futureGet = _peer.get(key).start();
        futureGet.awaitUninterruptibly();
        if(futureGet.isSuccess() && !futureGet.isEmpty()) {
            
            ArrayList<PeerAddress> responders = new ArrayList<PeerAddress>();
            
            Set<Entry<PeerAddress, Map<Number640, Data>>> responderSet = futureGet.rawData().entrySet();
            for(Entry<PeerAddress, Map<Number640, Data>> responderEntry : responderSet) {
                responders.add(responderEntry.getKey());
            }

            return responders;

        } else {
            throw new DNFSException.DNFSNetworkGetException("Could not get response.");
        }
    }
    
    
    public Object sendTo(PeerAddress address, Object data) throws DNFSNetworkNoConnection, DNFSNetworkSendException {
        
        connectionBouncer();
       
        ArrayList<Object> responses = new ArrayList<Object>();
        ArrayList<Throwable> exceptions = new ArrayList<Throwable>();
        
        FutureDirect direct = _peer.peer().sendDirect(address).object(data).start();
        direct.addListener(new BaseFutureListener<FutureDirect>() {

            @Override
            public void exceptionCaught(Throwable exception) throws Exception {
                exceptions.add(exception);
            }

            @Override
            public void operationComplete(FutureDirect response) throws Exception {
                responses.add(response.object());
            }
            
        });
        
        direct.awaitUninterruptibly();
        if(!direct.isSuccess()) {
            throw new DNFSException.DNFSNetworkSendException("Direct send failed.");
        }
        if(exceptions.size() > 0) {
            throw new DNFSException.DNFSNetworkSendException("Exception while direct send: " + exceptions.get(0).getMessage());
        }
        if(responses.size() != 1) {
            throw new DNFSException.DNFSNetworkSendException("Didn't receive an answer.");
        }
        return responses.get(0);
    }
    
    
    /**
     * 
     * @param addresses
     * @param data
     * @return
     * @throws DNFSNetworkNoConnection
     * @throws DNFSNetworkSendException
     */
    public ArrayList<Object> sendToAll(ArrayList<PeerAddress> addresses, Object data) throws DNFSNetworkNoConnection, DNFSNetworkSendException {
        
        connectionBouncer();
        
        ArrayList<FutureDirect> directs = new ArrayList<FutureDirect>();
        ArrayList<Object> responses = new ArrayList<Object>();
        ArrayList<Throwable> exceptions = new ArrayList<Throwable>();
        
        for(PeerAddress address : addresses) {
            FutureDirect direct = _peer.peer().sendDirect(address).object(data).start();
            
            direct.addListener(new BaseFutureListener<FutureDirect>() {

                @Override
                public void exceptionCaught(Throwable exception) throws Exception {
                    exceptions.add(exception);
                }

                @Override
                public void operationComplete(FutureDirect response) throws Exception {
                    responses.add(response.object());
                }
                
            });
            directs.add(direct);
        }
        
        for(FutureDirect direct : directs) {
            direct.awaitUninterruptibly();
            if(!direct.isSuccess()) {
                throw new DNFSException.DNFSNetworkSendException("Direct send failed.");
            }
        }
        if(exceptions.size() > 0) {
            throw new DNFSException.DNFSNetworkSendException("Exception while direct send: " + exceptions.get(0).getMessage());
        }
        if(directs.size() != responses.size()) {
            throw new DNFSException.DNFSNetworkSendException("Didn't receive all answers.");
        }
        return responses;
    }


    /**
     * Checks if the network class is connected to the network.
     *
     * @throws DNFSException.DNFSNetworkNoConnection
     */
    private void connectionBouncer() throws DNFSException.DNFSNetworkNoConnection {
        if (!this._connected) {
            throw new DNFSException.DNFSNetworkNoConnection();
        }
    }


    /**
     * @return
     */
    public PeerDHT getPeer() {
        return this._peer;
    }
    
    
    public PeerAddress getPeerAddress() {
        return this._peer.peerAddress();
    }

}