package ch.uzh.csg.p2p.group_1;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.tomp2p.peers.Number160;


public class FileBasedKeyValueStorage implements IKeyValueStorage {
    
    
    String directory = ".";


    public FileBasedKeyValueStorage() throws Exception {
        try {
            this.directory = this.createTempDirectory().getAbsolutePath();
        } catch(IOException e) {
            throw new Exception("Unable to create file-based key-value storage temporary folder.");
        }
        checkWritability();
    }
    

    public FileBasedKeyValueStorage(String directory) throws Exception {
        this.directory = directory;
        if(!Files.exists(Paths.get(this.directory))) {
            File newDirectory = new File(this.directory);
            newDirectory.mkdirs();
        }
        checkWritability();
    }
    
    
    private void checkWritability() throws Exception {
        try {
            File empty = new File(this.directory + "/empty");
            empty.createNewFile();
            empty.delete();
        } catch(IOException e) {
            throw new Exception("Cannot write to file-based key-value storage folder.");
        } catch(OutOfMemoryError e) {
            throw new Exception("Cannot write to file-based key-value storage folder.");
        } catch(SecurityException e) {
            throw new Exception("Cannot write to file-based key-value storage folder.");
        }
    }
    

    public void setDirectory(String path) {
        directory = path;
    }
    
    
    public boolean exists(Number160 key) {
        Path path = Paths.get(directory + "/" + key.toString());
        return Files.exists(path);
    }

    
    public boolean set(Number160 key, KeyValueData value) {
        
        try {
            
            Path path = Paths.get(directory + "/" + key.toString());
            Files.write(path, value.getData());
            return true;
            
        } catch(IOException | OutOfMemoryError | SecurityException e) {
            return false;
        }
    }

    
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

    
    public File createTempDirectory() throws IOException {
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
}
