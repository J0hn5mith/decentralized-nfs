package ch.uzh.csg.p2p.group_1.fuse_integration;

import net.fusejna.StructFuseFileInfo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by janmeier on 27.05.15.
 */
public class BufferedFuseIntegration extends FuseIntegration{

    Map<String, ByteBuffer> writeBuffers;

    public BufferedFuseIntegration() {
        super();
        this.writeBuffers = new HashMap<String, ByteBuffer>();
    }

    @Override
    public int write(String path, ByteBuffer buf, long bufSize, long writeOffset, StructFuseFileInfo.FileInfoWrapper info) {

        ByteBuffer byteBuffer;

        if(!this.writeBuffers.keySet().contains(path)){
            byteBuffer = ByteBuffer.wrap(new byte[0]);
        }
        else {
            byteBuffer = this.writeBuffers.get(path);
        }

        if(writeOffset + bufSize > byteBuffer.array().length){
            ByteBuffer newByteBuffer = ByteBuffer.wrap(new byte[(int)(writeOffset + bufSize)]);
            byteBuffer.position((int)writeOffset);

            newByteBuffer.put(byteBuffer.array(), 0, byteBuffer.array().length);
            byteBuffer = newByteBuffer;
        }
        byteBuffer.position((int)writeOffset);
        byteBuffer.put(buf);

        this.writeBuffers.remove(path);
        this.writeBuffers.put(path, byteBuffer);
        return (int) bufSize;

    }

    @Override
    public int flush(String path, StructFuseFileInfo.FileInfoWrapper info) {
        ByteBuffer buffer = this.writeBuffers.get(path);
        if(buffer != null){
            buffer.rewind();
            super.write(path, buffer, buffer.array().length, 0, info);
        }
        else{
            System.out.print("File has been flushed that does not exist." + path);
        }
        return 0;
    }
}
