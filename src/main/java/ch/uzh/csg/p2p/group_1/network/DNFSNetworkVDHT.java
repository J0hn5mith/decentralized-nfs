/**
 * Created by janmeier on 10.05.15.
 */
package ch.uzh.csg.p2p.group_1.network;

import ch.uzh.csg.p2p.group_1.DNFSException;
import ch.uzh.csg.p2p.group_1.DNFSNetwork;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DNFSNetworkVDHT {
    final private static Logger LOGGER = Logger.getLogger(DNFSNetworkVDHT.class.getName());

    private DNFSNetwork connection;

    public DNFSNetworkVDHT(DNFSNetwork connection) {
        LOGGER.setLevel(Level.INFO);
        this.connection = connection;
    }

    public void put(Number160 key, Object object) throws DNFSException.DNFSNetworkPutException {
        Data data;
        try{
            data = (Data) object;
        }
        catch (Exception e){
            LOGGER.error("Can only put objects of class Data");
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
            }
            else{
                oldVersionKey = null;
                LOGGER.error("Inconsistent versions.");
            }
        }
        confirm(key, versionKey);
        return;
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
        if (latestData.size() == 0){
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
        FuturePut fp = this.connection.getPeer()
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
                    this.connection.getPeer().remove(key).versionKey(versionKey.getVersionKey()).start()
                            .awaitUninterruptibly();
                    return false;
                }
            }
            last = fgData;
        }
        return true;
    }

    private void confirm(Number160 key, VersionKey versionKey) {
        FuturePut fp = this.connection.getPeer().put(key)
                .versionKey(versionKey.getVersionKey()).putConfirm()
                .data(new Data()).start().awaitUninterruptibly();
        LOGGER.info("Confirmed put in dht" + fp.failedReason());
    }


    private List<FutureGetRawData> getLatestData(Number160 key) {
        FutureGet fg = this.connection.getPeer().get(key).contentKey(Number160.ZERO).getLatest().start()
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
}
