package ch.uzh.csg.p2p.group_1;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.tomp2p.peers.Number160;


public class FileBasedKeyValueStorage implements IKeyValueStorage {
    
    
    String directory = ".";


    public FileBasedKeyValueStorage() throws IOException {
        this.directory = this.createTempDirectory().getAbsolutePath();
        Path path = Paths.get(this.directory + "/empty");
        /*if(!Files.(path)) {
            throw new IOException();
        }*/
    }

    public FileBasedKeyValueStorage(String directory) {
        //TODO: Check if folder actually exists!

        this.directory = directory;
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
            
        } catch(IOException e) {
            return false;
        } catch(OutOfMemoryError e) {
            return false;
        } catch(SecurityException e) {
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

    public File createTempDirectory()
            throws IOException {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }
}
