package ch.uzh.csg.p2p.group_1;

import java.io.Serializable;

/**
 * Created by janmeier on 02.04.15.
 */
public class DNFSData<T> implements Serializable {
	
    private static final long serialVersionUID = 2098774660703812030L;

    private T key;
    private T data;

    /**
     * 
     * @param key
     * @param data
     */
    DNFSData(T key, T data) {
        this.key = key;
        this.data = data;
    }

    /**
     * 
     * @return
     */
    public T getData(){
        return this.data;
    }
    
}
