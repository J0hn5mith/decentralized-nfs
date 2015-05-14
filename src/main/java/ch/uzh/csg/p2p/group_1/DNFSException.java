package ch.uzh.csg.p2p.group_1;

/**
 * Created by janmeier on 10.04.15.
 */
public class DNFSException extends Throwable {

    static public class DNFSNotFileException extends DNFSException {

    }

    
    static public class NoSuchFileOrFolder extends DNFSException{
   
    }


    static public class DNFSNotFolderException extends NoSuchFileOrFolder  {

    }

    
    static public class DNFSPathNotFound extends DNFSException {

    }

    static public class DNFSBlockStorageException extends DNFSException {

    }

    static public class DNFSNetworkNoConnection extends DNFSException {
        public DNFSNetworkNoConnection() {
            super("Your network is not connected.");
        }
    }

    static public class DNFSNetworkSetupException extends DNFSException {
        public DNFSNetworkSetupException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkPutException extends DNFSException {
        public DNFSNetworkPutException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkGetException extends DNFSException {
        public DNFSNetworkGetException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkDeleteException extends DNFSException {
        public DNFSNetworkDeleteException(String message) {
            super(message);
        }
    }
    
    
    public DNFSException() {
        super();
    }
    
    
    public DNFSException(String message) {
        super(message);
    }

}

