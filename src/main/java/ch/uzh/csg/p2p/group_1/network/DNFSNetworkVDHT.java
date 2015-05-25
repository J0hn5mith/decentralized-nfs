/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1.network;

import ch.uzh.csg.p2p.group_1.DNFSException;
import ch.uzh.csg.p2p.group_1.DNFSNetwork;
import ch.uzh.csg.p2p.group_1.DNFSStorageLayer;
import ch.uzh.csg.p2p.group_1.IKeyValueStorage;
import net.tomp2p.dht.*;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.*;


import net.tomp2p.storage.Storage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DNFSNetworkVDHT implements DNFSINetwork {
    final private static Logger LOGGER = Logger.getLogger(DNFSNetworkVDHT.class.getName());
    private boolean _initialized = false;
    private DNFSNetwork network;
    

    public DNFSNetworkVDHT(int port, IKeyValueStorage keyValueStorage) throws DNFSException.DNFSNetworkSetupException {
        this.network = new DNFSNetwork(this.createPeer(port, keyValueStorage));
        this._initialized = true;
        LOGGER.setLevel(Level.INFO);
    }

    public void registerObjectDataReply(ObjectDataReply reply) {
        this.network.registerObjectDataReply(reply);
    }

    public void connectToNetwork(int port, String masterIpAddress, int masterPort) throws DNFSException.DNFSNetworkSetupException {
        this._initialized = false;
        this.network.connectToNetwork(port, masterIpAddress, masterPort);
        this._initialized = true;
    }

    public boolean keyExists(Number160 key) throws
            DNFSException.DNFSNetworkNotInit {
        return this.network.keyExists(key);
    }

    @Override
    public Number160 getUniqueKey() throws
            DNFSException.DNFSNetworkNotInit {
        return this.network.getUniqueKey();

    }

    @Override
    public void put(Number160 key, Object object) throws DNFSException.DNFSNetworkPutException, DNFSException.DNFSNetworkNotInit {
        Data data;

        try {
            data = new Data(object);
        } catch (IOException e) {
            throw new DNFSException.DNFSNetworkPutException("Failed to convert object into data object.");
        }

        if (!this.keyExists(key)) {
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
            DNFSException.DNFSNetworkNotInit,
            DNFSException.DNFSNetworkGetException {
        return this.network.get(key);
    }

    @Override
    public void delete(Number160 key) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSNetworkDeleteException {
        this.network.delete(key);
    }

    @Override
    public PeerAddress getFirstResponder(Number160 key) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSNetworkGetException {
        return this.network.getFirstResponder(key);
    }

    @Override
    public Object sendTo(PeerAddress address, Object data) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSNetworkSendException {
        return this.network.sendTo(address, data);
    }

    @Override
    public ArrayList<Object> sendToAll(ArrayList<PeerAddress> addresses, Object data) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSNetworkSendException {
        return sendToAll(addresses, data);
    }

    
    @Override
    public PeerAddress getPeerAddress() {
        return this.network.getPeerAddress();
    }

    
    @Override
    public ArrayList<PeerAddress> getAllResponders(Number160 key) throws DNFSException.DNFSNetworkNotInit, DNFSException.DNFSNetworkGetException {
        return this.getAllResponders(key);
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

                    LOGGER.info("Found different file versions.");

                    return null;

                }
            }
            last = fgData;
        }
        return last.getVersionKey();
    }

    
    private boolean setPrepare(Number160 key, Data data, VersionKey versionKey) {

        System.out.println("setPrepared()" + key); // TODO
        
        data.prepareFlag();
        FuturePut fp = this.network.getPeer()
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
                    this.network.getPeer().remove(key).versionKey(versionKey.getVersionKey()).start()
                            .awaitUninterruptibly();
                    return false;
                }
            }
            last = fgData;
        }
        return true;
    }

    
    private void confirm(Number160 key, VersionKey versionKey) {
        FuturePut fp = this.network.getPeer().put(key)
                .versionKey(versionKey.getVersionKey()).putConfirm()
                .data(new Data()).start().awaitUninterruptibly();
        LOGGER.info("Confirmed put in dht" + fp.failedReason());
    }


    private List<FutureGetRawData> getLatestData(Number160 key) {
        FutureGet fg = this.network.getPeer().get(key).contentKey(Number160.ZERO).getLatest().start()
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
     * @throws DNFSException.DNFSNetworkNotInit
     */
    private void initializationBouncer() throws DNFSException.DNFSNetworkNotInit {
        if (!this._initialized) {
            throw new DNFSException.DNFSNetworkNotInit();
        }
    }


    private void putFirstTime(Number160 key, Data data) {

        this.network.getPeer().put(key)
                .data(Number160.ZERO, data)
                .start()
                .awaitUninterruptibly();
    }

    public void registerPeerChangeListener(PeerMapChangeListener listener) {
        this.network.registerPeerChangeListener(listener);
    }

    public boolean isConnected() throws DNFSException.DNFSNetworkNotInit {
        return this.network.isConnected();
    }

    public boolean isConnected(PeerAddress peerAddress) throws DNFSException.DNFSNetworkNotInit {
        return this.network.isConnected(peerAddress);
    }

    public void disconnect() throws DNFSException.DNFSNetworkNotInit {
        this.network.disconnect();
    }

    public void setConnectionTimeout(int connectionTimeOut) {
        this.network.setConnectionTimeout(connectionTimeOut);
    }


    private PeerDHT createPeer(int port, IKeyValueStorage keyValueStorage) throws
            DNFSException.DNFSNetworkSetupException {
        try {
            Random _random = new Random(System.currentTimeMillis());
            Number160 key = Number160.createHash(_random.nextLong());
            PeerBuilder builder = new PeerBuilder(key).ports(port);
            PeerBuilderDHT builderDHT =  new PeerBuilderDHT(builder.start());
            Storage storage = new StorageMemory();
            StorageLayer storageLayer = new DNFSStorageLayer(storage, this, keyValueStorage);
            return builderDHT.storageLayer(storageLayer).start();
        } catch (IOException e) {
            throw new DNFSException.DNFSNetworkSetupException("IOException: " + e.getMessage());
        }
    }
}
