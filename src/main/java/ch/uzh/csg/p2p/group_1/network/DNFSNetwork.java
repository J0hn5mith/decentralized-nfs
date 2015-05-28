package ch.uzh.csg.p2p.group_1.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ch.uzh.csg.p2p.group_1.Main;
import ch.uzh.csg.p2p.group_1.network.key_value_storage.interfaces.IKeyValueStorage;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSNetworkNotInit;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSNetworkSendException;
import ch.uzh.csg.p2p.group_1.network.interfaces.DNFSINetwork;
import net.tomp2p.connection.ChannelCreator;
import net.tomp2p.connection.DefaultConnectionConfiguration;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureChannelCreator;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.Storage;


public class DNFSNetwork implements DNFSINetwork{
    final private static Logger LOGGER = Logger.getLogger(DNFSNetwork.class);


    private Random _random;
    private boolean _initialized = false;
    private PeerDHT _peer;
    private Map<Integer, Boolean> _locks;
    private int _nextLock;


    /**
     * @param port
     * @throws ch.uzh.csg.p2p.group_1.exceptions.DNFSException.DNFSNetworkSetupException
     */
    public DNFSNetwork(int port, IKeyValueStorage keyValueStorage) throws DNFSException.DNFSNetworkSetupException {
        _random = new Random(System.currentTimeMillis());
        _locks = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
//        _locks = new HashMap<Integer, Boolean>();
        _nextLock = 0;
        this._peer = createPeer(port, keyValueStorage);
        this._initialized = true;
        LOGGER.setLevel(Main.LOGGER_LEVEL);
    }

    
    public DNFSNetwork(PeerDHT peer){
        _random = new Random(System.currentTimeMillis());
        _locks = new HashMap<Integer, Boolean>();
        _nextLock = 0;
        this._peer = peer;
        this._initialized = true;
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
        this._initialized = false;

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

        this._initialized = true;
    }




    /**
     * @param key
     * @return
     * @throws DNFSException.DNFSNetworkGetException
     */
    public boolean keyExists(Number160 key) throws
            DNFSException.NetworkException {
        
        initializationBouncer();
        
        try {
            return this.get(key) != null;
        } catch (DNFSException.DNFSNetworkGetException e) {
            return false;
        }
    }


    /**
     * @return
     * @throws DNFSException.DNFSNetworkGetException
     */
    public Number160 getUniqueKey() throws
            DNFSException.NetworkException {
        
        initializationBouncer();
        
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
            DNFSException.DNFSNetworkPutException, DNFSException.NetworkException {

        initializationBouncer();
        
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
            DNFSException.DNFSNetworkGetException, DNFSException.NetworkException {
        
        initializationBouncer();
        
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
            DNFSException.DNFSNetworkDeleteException, DNFSException.NetworkException {
        
        initializationBouncer();
        
        FutureRemove futureRemove = _peer.remove(key).start();
        futureRemove.awaitUninterruptibly();
        if(!futureRemove.isSuccess()) {
            throw new DNFSException.DNFSNetworkDeleteException("Could not delete data.");
        }
    }


    /**
     * Returns the first responding peer which is responsible for the key.
     * @param key
     * @return
     */
    public PeerAddress getFirstResponder(Number160 key) throws
            DNFSException.DNFSNetworkGetException, DNFSException.NetworkException {
        
        initializationBouncer();
        
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
            DNFSException.DNFSNetworkGetException, DNFSException.NetworkException {
        
        initializationBouncer();
        
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
    
    
    public Object sendTo(PeerAddress address, Object data) throws DNFSNetworkSendException, DNFSException.NetworkException {
        
        initializationBouncer();
       
        final ArrayList<Object> responses = new ArrayList<Object>();
        final ArrayList<Throwable> exceptions = new ArrayList<Throwable>();
        
        final Integer lockIndex = new Integer(_nextLock);
        _nextLock++;
        _locks.put(lockIndex, true);
        
        FutureDirect direct = _peer.peer().sendDirect(address).object(data).start();
        direct.addListener(new BaseFutureListener<FutureDirect>() {

            @Override
            public void exceptionCaught(Throwable exception) throws Exception {
                exceptions.add(exception);
                _locks.put(lockIndex, false);
            }

            @Override
            public void operationComplete(FutureDirect response) throws Exception {
                responses.add(response.object());
                _locks.put(lockIndex, false);
            }
            
        });
        
        try {
            int counter = 0;
            while(counter < 150 && (_locks == null || lockIndex == null ||
                    !_locks.containsKey(lockIndex) || _locks.get(lockIndex) == null ||
                    _locks.get(lockIndex).equals(true))) {
                Thread.sleep(3);
                counter++;
            }
        } catch (InterruptedException e) {
            throw new DNFSException.DNFSNetworkSendException("Waiting thread interrupted: " + e.getMessage());
        }
        
        _locks.remove(lockIndex);
        
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
     * @throws DNFSNetworkNotInit
     * @throws DNFSNetworkSendException
     */
    public ArrayList<Object> sendToAll(ArrayList<PeerAddress> addresses, Object data) throws DNFSNetworkSendException, DNFSException.NetworkException {
        
        initializationBouncer();
        
        ArrayList<FutureDirect> directs = new ArrayList<FutureDirect>();
        final ArrayList<Object> responses = new ArrayList<Object>();
        final ArrayList<Throwable> exceptions = new ArrayList<Throwable>();
        
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
     * @throws DNFSException.DNFSNetworkNotInit
     */
    private void initializationBouncer() throws DNFSException.NetworkException {
        if (!this._initialized) {
            throw new DNFSException.NetworkException("Network is not initialized.");
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

    /**
     * @param port
     * @throws DNFSException.DNFSNetworkSetupException
     */
    private PeerDHT createPeer(int port, IKeyValueStorage keyValueStorage) throws
            DNFSException.DNFSNetworkSetupException {
        try {
            Number160 key = Number160.createHash(_random.nextLong());
            PeerBuilder builder = new PeerBuilder(key).ports(port);
            PeerBuilderDHT builderDHT =  new PeerBuilderDHT(builder.start());
            Storage storage = new StorageMemory();
            return builderDHT.start();

        } catch (IOException e) {
            throw new DNFSException.DNFSNetworkSetupException("IOException: " + e.getMessage());
        }
    }
    

    public void registerPeerChangeListener(PeerMapChangeListener listener) {
        _peer.peerBean().peerMap().addPeerMapChangeListener(listener);
    }
    
    public boolean isConnected() throws DNFSException.NetworkException {
        initializationBouncer();
        
        Iterator<PeerAddress> iterator = _peer.peerBean().peerMap().all().iterator();
        while (iterator.hasNext()) {
            if (isConnected(iterator.next())) {
                // as soon as we get a response from at least one other peer we are connected
                return true;
            }
        }
        // if no one answered we are not connected to anyone
        return false;
    }
    
    public boolean isConnected(PeerAddress peerAddress) throws DNFSException.NetworkException {
        initializationBouncer();
        
        FutureChannelCreator fcc = _peer.peer().connectionBean().reservation().create(1, 1);
        fcc.awaitUninterruptibly();

        ChannelCreator cc = fcc.channelCreator();

        FutureResponse fr = _peer.peer().pingRPC().pingUDP(peerAddress, cc, new DefaultConnectionConfiguration());
        fr.awaitUninterruptibly();

        if (fr.isSuccess()) {
            return true;
        }
        return false;
    }
    
    public void disconnect() throws DNFSException.NetworkException {
        initializationBouncer();
        
        _peer.peer().announceShutdown().start().awaitUninterruptibly();
        _peer.shutdown().awaitListenersUninterruptibly();
        this._initialized = false;  
    }
    
    public void setConnectionTimeout(int connectionTimeOut){
        _peer.peer().connectionBean().DEFAULT_CONNECTION_TIMEOUT_TCP = connectionTimeOut;
    }
}

