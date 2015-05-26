package ch.uzh.csg.p2p.group_1.storage;

import ch.uzh.csg.p2p.group_1.storage.interfaces.DNFSIBlockStorage;
import net.tomp2p.peers.Number160;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

/**
 * Created by janmeier on 26.05.15.
 */
public class BlockEncrypted extends DNFSBlock{
    private Cipher encryptCipher = null;
    private Cipher decryptCipher = null;

    public BlockEncrypted(Number160 id, DNFSIBlockStorage blockStorage, Cipher encryptCipher, Cipher decryptCipher) {
        super(id, blockStorage);
        this.encryptCipher = encryptCipher;
        this.decryptCipher = decryptCipher;
        this.data = ByteBuffer.wrap(decrypt(super.getByteArray()));
    }

    public BlockEncrypted(Number160 id, byte[] byteArray, DNFSIBlockStorage blockStorage, Cipher encryptCipher, Cipher decryptCipher) {
        super(id, byteArray, blockStorage);
        this.encryptCipher = encryptCipher;
        this.decryptCipher = decryptCipher;
        this.data = ByteBuffer.wrap(decrypt(super.getByteArray()));
    }

    public byte[] getByteArray(){
        return encrypt(super.getByteArray());
    }

    private byte[] encrypt(byte[] data) {
        try {
            return this.encryptCipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decrypt(byte[] data){
        try{
            return this.decryptCipher.doFinal(data);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
