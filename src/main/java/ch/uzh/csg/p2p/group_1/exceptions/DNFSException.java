package ch.uzh.csg.p2p.group_1.exceptions;

/**
 * Created by janmeier on 10.04.15.
 */
public class DNFSException extends Exception {

    private static final long serialVersionUID = 2740073990296063507L;


    static public class DNFSNotFileException extends DNFSException {
        private static final long serialVersionUID = -3918444955252984445L;
    }

    
    static public class NoSuchFileOrDirectory extends DNFSException{
        private static final long serialVersionUID = 1229382105660628170L;
    }


    static public class DNFSNotDirectoryException extends NoSuchFileOrDirectory  {
        private static final long serialVersionUID = -5132810907932619010L;
    }

    
    static public class DNFSPathNotFound extends DNFSException {
        private static final long serialVersionUID = -7866969856858819069L;
    }

    static public class DNFSBlockStorageException extends DNFSException {
        private static final long serialVersionUID = 8480393673517541259L;
        
        public DNFSBlockStorageException(String message) {
            super(message);
        }

        public DNFSBlockStorageException(String message, Exception e) {

        }

    }

    static public class INodeStorageException extends DNFSException {

        public INodeStorageException(String message) {
            super(message);
        }
        public INodeStorageException(String message, Exception e) {
            super(message, e);
        }
    }

    static public class DNFSNetworkNotInit extends DNFSException {
        private static final long serialVersionUID = 8895499485868404342L;

        public DNFSNetworkNotInit() {
            super("Your network is not initialized.");
        }
    }

    static public class DNFSNetworkSetupException extends DNFSException {
        private static final long serialVersionUID = 482847664421090530L;

        public DNFSNetworkSetupException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkPutException extends DNFSException {
        private static final long serialVersionUID = -8121432314741122319L;

        public DNFSNetworkPutException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkGetException extends DNFSException {
        private static final long serialVersionUID = -9205989666839957529L;

        public DNFSNetworkGetException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkDeleteException extends DNFSException {
        private static final long serialVersionUID = -1935907951987843485L;

        public DNFSNetworkDeleteException(String message) {
            super(message);
        }
    }
    
    
    static public class DNFSNetworkSendException extends DNFSException {
        private static final long serialVersionUID = 3029814494456359295L;

        public DNFSNetworkSendException(String message) {
            super(message);
        }
    }
    
    
    public DNFSException() {
        super();
    }
    
    
    public DNFSException(String message) {
        super(message);
    }

    public DNFSException(String message, Exception e) {
        super(message, e);
    }

    static public class DNFSKeyValueStorageException extends DNFSException{
        private static final long serialVersionUID = -5687156715217687765L;

        public DNFSKeyValueStorageException(String message) {
            super(message);
        }
    }

    static public class DNFSSettingsException extends DNFSException {
        private static final long serialVersionUID = 5006111210087209771L;

        public DNFSSettingsException(String message) {
            super(message);
        }
    }

    static public class NetworkException extends DNFSException {
        public NetworkException(String message) {
            super(message);
        }
    }

}

