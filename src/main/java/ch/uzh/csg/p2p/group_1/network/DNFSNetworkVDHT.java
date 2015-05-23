/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1.network;

import ch.uzh.csg.p2p.group_1.DNFSException;
import ch.uzh.csg.p2p.group_1.DNFSStorageLayer;
import ch.uzh.csg.p2p.group_1.IKeyValueStorage;
import net.tomp2p.dht.*;
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

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.Map.Entry;

import net.tomp2p.storage.Storage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DNFSNetworkVDHT implements DNFSINetwork {
    final private static Logger LOGGER = Logger.getLogger(DNFSNetworkVDHT.class.getName());

    private Random _random;

    private int _port = 0;

    private boolean _connected = false;
    private PeerDHT _peer;

    public DNFSNetworkVDHT(int port, IKeyValueStorage keyValueStorage) {
        _random = new Random(System.currentTimeMillis());

        LOGGER.setLevel(Level.DEBUG);
        try {
            setupPeer(port, keyValueStorage);
        } catch (DNFSException.DNFSNetworkSetupException e) {
            LOGGER.error("FATAL ERROR", e);
        }
        this._connected = true;
    }

    @Override
    public void registerObjectDataReply(ObjectDataReply reply) {
        _peer.peer().objectDataReply(reply);

    }

    @Override
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

    @Override
    public boolean keyExists(Number160 key) throws
            DNFSException.DNFSNetworkNoConnection {
        connectionBouncer();

        try {
            return get(key) != null;
        } catch (DNFSException.DNFSNetworkGetException e) {
            return false;
        }
    }

    @Override
    public Number160 getUniqueKey() throws
            DNFSException.DNFSNetworkNoConnection {

        connectionBouncer();

        Number160 key = Number160.createHash(_random.nextLong());
        while (keyExists(key)) {
            key = Number160.createHash(_random.nextLong());
        }
        return key;
    }

    @Override
    public void put(Number160 key, Object object) throws DNFSException.DNFSNetworkPutException, DNFSException.DNFSNetworkNoConnection {
        Data data ;

        try {
            data = new Data(object);
        } catch (IOException e) {
            throw new DNFSException.DNFSNetworkPutException("Failed to convert object into data object.");
        };

        if(!this.keyExists(key)){
            this.putFirstTime(key, data);
            return;

        }
        VersionKey oldVersionKey = null;
        VersionKey versionKey = null;
        boolean prepared = false;

        while (oldVersionKey == null && !prepared) {
            oldVersionKey = checkVersions(key);
            if (oldVersionKey != null) {
                data.addBasedOn(oldVersionKey.getVersionKey());
                long newVersionNumber = oldVersionKey.getVersion() + 1;
                versionKey = new VersionKey(newVersionNumber, data.hash());
                prepared = setPrepare(key, data, versionKey);
            } else {
                oldVersionKey = null;
            }
        }
        confirm(key, versionKey);
        LOGGER.debug("Data has been successfully putted.");
        return;
    }

    @Override
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

    @Override
    public void delete(Number160 key) throws DNFSException.DNFSNetworkNoConnection, DNFSException.DNFSNetworkDeleteException {
        connectionBouncer();

        FutureRemove futureRemove = _peer.remove(key).start();
        futureRemove.awaitUninterruptibly();
        if (!futureRemove.isSuccess()) {
            throw new DNFSException.DNFSNetworkDeleteException("Could not delete data.");
        }

    }

    @Override
    public PeerAddress getFirstResponder(Number160 key) throws DNFSException.DNFSNetworkNoConnection, DNFSException.DNFSNetworkGetException {
        connectionBouncer();

        FutureGet futureGet = _peer.get(key).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess() && !futureGet.isEmpty()) {

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

    @Override
    public Object sendTo(PeerAddress address, Object data) throws DNFSException.DNFSNetworkNoConnection, DNFSException.DNFSNetworkSendException {
        connectionBouncer();

        final ArrayList<Object> responses = new ArrayList<Object>();
        final ArrayList<Throwable> exceptions = new ArrayList<Throwable>();

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

    @Override
    public ArrayList<Object> sendToAll(ArrayList<PeerAddress> addresses, Object data) throws DNFSException.DNFSNetworkNoConnection, DNFSException.DNFSNetworkSendException {
        connectionBouncer();

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

    @Override
    public PeerAddress getPeerAddress() {
        return this._peer.peerAddress();
    }

    @Override
    public ArrayList<PeerAddress> getAllResponders(Number160 key) throws DNFSException.DNFSNetworkNoConnection, DNFSException.DNFSNetworkGetException {
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

    /**
     * Returns the VersionKey for the specified object in the dht of the latest version if they are all the same.
     * null is returned, if there are different versions.
     *
     * @param key
     * @return
     */
    private VersionKey checkVersions(Number160 key) {

        FutureGetRawData last = null;
        List<FutureGetRawData> latestData = this.getLatestData(key);
        if (latestData.size() == 0) {
            return null;
        }
        for (FutureGetRawData fgData : latestData) {
            if (last != null) {
                if (!last.equals(fgData)) {
                    return null;
                }
            }
            last = fgData;
        }
        return last.getVersionKey();
    }

    private boolean setPrepare(Number160 key, Data data, VersionKey versionKey) {

        data.prepareFlag();
        FuturePut fp = this._peer
                .put(key)
                .data(
                        Number160.ZERO,
                        data,
                        versionKey.getVersionKey()
                ).start().awaitUninterruptibly();
        LOGGER.info("Set prepared " + fp.failedReason());


        FutureGetRawData last = null;
        for (FutureGetRawData fgData : FutureGetRawData.listFromRawData(fp.rawResult())) {

            if (last != null) {
                if (!last.equals(fgData)) {
                    this._peer.remove(key).versionKey(versionKey.getVersionKey()).start()
                            .awaitUninterruptibly();
                    return false;
                }
            }
            last = fgData;
        }
        return true;
    }

    private void confirm(Number160 key, VersionKey versionKey) {
        FuturePut fp = this._peer.put(key)
                .versionKey(versionKey.getVersionKey()).putConfirm()
                .data(new Data()).start().awaitUninterruptibly();
        LOGGER.info("Confirmed put in dht" + fp.failedReason());
    }


    private List<FutureGetRawData> getLatestData(Number160 key) {
        FutureGet fg = this._peer.get(key).contentKey(Number160.ZERO).getLatest().start()
                .awaitUninterruptibly();
        return FutureGetRawData.listFromRawData(fg.rawData());
    }


    private static class FutureGetRawData<T> {
        PeerAddress address;
        Number640 key;
        T data;

        static public <K> List<FutureGetRawData> listFromRawData(Map<PeerAddress, Map<Number640, K>> rawData) {
            List<FutureGetRawData> latestData = new ArrayList<FutureGetRawData>();

            for (Map.Entry<PeerAddress, Map<Number640, K>> entry : rawData.entrySet()) {
                latestData.add(new FutureGetRawData(entry));
            }

            return latestData;

        }

        public FutureGetRawData(Map.Entry<PeerAddress, Map<Number640, T>> entry) {
            this.address = address;
            this.key = entry.getValue().keySet().iterator().next();
            this.data = entry.getValue().values().iterator().next();
        }

        public boolean equals(FutureGetRawData<T> other) {
            if (!this.key.equals(other.key)) {
                return false;
            }
            if (!this.data.equals(other.data)) {
                return false;
            }
            return true;
        }

        public long getVersion() {
            return this.key.versionKey().timestamp();
        }

        public VersionKey getVersionKey() {
            return new VersionKey(this.key.versionKey());
        }
    }

    private static class VersionKey {
        Number160 versionKey;
        Number160 dataHash;
        long versionNumber;


        public VersionKey(Number160 versionKey) {
            this.versionKey = versionKey;
            this.versionNumber = versionKey.timestamp();
            this.dataHash = versionKey.number96();

        }

        public VersionKey(long versionNumber, Number160 dataHash) {
            this.versionNumber = versionNumber;
            this.dataHash = dataHash;
            this.versionKey = new Number160(versionNumber, dataHash);
        }

        public Number160 getVersionKey() {
            return versionKey;
        }

        public Number160 getDataHash() {
            return this.dataHash;

        }

        public long getVersion() {
            return this.versionKey.timestamp();
        }

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
     * @param port
     * @throws DNFSException.DNFSNetworkSetupException
     */
    private void setupPeer(int port, IKeyValueStorage keyValueStorage) throws
            DNFSException.DNFSNetworkSetupException {

        try {
            _port = port;

            Number160 key = Number160.createHash(_random.nextLong());

            PeerBuilder builder = new PeerBuilder(key).ports(_port);
            PeerBuilderDHT builderDHT = new PeerBuilderDHT(builder.start());
            Storage storage = new StorageMemory();
            StorageLayer storageLayer = new DNFSStorageLayer(storage, this, keyValueStorage);
            _peer = builderDHT.storageLayer(storageLayer).start();

        } catch (IOException e) {
            throw new DNFSException.DNFSNetworkSetupException("IOException: " + e.getMessage());
        }
    }

    private void putFirstTime(Number160 key, Data data){

        this._peer.put(key)
                .data(Number160.ZERO, data)
                .start()
                .awaitUninterruptibly();
    }
}
