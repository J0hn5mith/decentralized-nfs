package ch.uzh.csg.p2p.group_1.network.key_value_storage;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ch.uzh.csg.p2p.group_1.network.key_value_storage.interfaces.IKeyValueStorage;
import ch.uzh.csg.p2p.group_1.exceptions.DNFSException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.tomp2p.peers.Number160;


public class FileBasedKeyValueStorage implements IKeyValueStorage {
    final private static Logger LOGGER = Logger.getLogger(FileBasedKeyValueStorage.class);

    
    String directory;


    public FileBasedKeyValueStorage() throws DNFSException.DNFSKeyValueStorageException {
        LOGGER.setLevel(Level.DEBUG);
        try {
            this.directory = this.createTempDirectory().getAbsolutePath();
        } catch(IOException e) {
            throw new DNFSException.DNFSKeyValueStorageException("Unable to create file-based key-value storage temporary folder.");
        }
    }
    

    public FileBasedKeyValueStorage(String directory){
        this.directory = directory;
        if(!Files.exists(Paths.get(this.directory))) {
            File newDirectory = new File(this.directory);
            newDirectory.mkdirs();
        }
    }
    
    
    @Override
    public void startUp() throws DNFSException.DNFSKeyValueStorageException {
        try {
            File empty = new File(this.directory + "/empty");
            empty.createNewFile();
            empty.delete();
        } catch(IOException e) {
            throw new DNFSException.DNFSKeyValueStorageException("Cannot write to file-based key-value storage folder.");
        } catch(OutOfMemoryError e) {
            throw new DNFSException.DNFSKeyValueStorageException("Cannot write to file-based key-value storage folder.");
        } catch(SecurityException e) {
            throw new DNFSException.DNFSKeyValueStorageException("Cannot write to file-based key-value storage folder.");
        }
    }


    @Override
    public void shutDown(){
        File folder = new File(this.directory);
        File[] files = folder.listFiles();
        if(files != null) {
            for(File file : files) {
                file.delete();
            }
        }
        folder.delete();
    }
    
    
    private File createTempDirectory() throws IOException {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if(!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if(!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }
    
    
    @Override
    public boolean exists(Number160 key) {
        Path path = Paths.get(directory + "/" + key.toString());
        return Files.exists(path);
    }

    
    @Override
    public boolean set(Number160 key, KeyValueData value) {
        try {
            Path path = Paths.get(directory + "/" + key.toString());
            Files.write(path, value.getData());
            LOGGER.info("Wrote file");
            return true;
        } catch(IOException | OutOfMemoryError | SecurityException e) {
            LOGGER.error("Could not set data.");
            return false;
        }
    }

    
    @Override
    public KeyValueData get(Number160 key) {
        
        try {
            
            Path path = Paths.get(directory + "/" + key.toString());
            if(Files.exists(path)) {
                byte[] rawData = Files.readAllBytes(path);
                KeyValueData data = new KeyValueData(rawData);
                return data;
            }
            
        } catch(Exception e) {
            return null;
        }
        return null;
    }

    
    @Override
    public boolean delete(Number160 key) {
        
        try {
            
            Path path = Paths.get(directory + "/" + key.toString());
            if(Files.exists(path)) {
                Files.delete(path);
                return true;
            }
            
        } catch(Exception e) {
            return false;
        }
        return false;
    }
    
}
