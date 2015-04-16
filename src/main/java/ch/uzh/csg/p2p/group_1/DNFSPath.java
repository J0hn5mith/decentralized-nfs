package ch.uzh.csg.p2p.group_1;

import com.google.common.base.Joiner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by janmeier on 16.04.15.
 */

/**
 * The to indexes of this class allow negative indexing.
 * If the index is negative it refers to the offset to the last element +1. This means that -1 refers to the last element and so on.
 */
public class DNFSPath {

    private String pathAsString;
    private List<String> pathAsList;

    public DNFSPath(String path) {
        this.pathAsString = path;
        this.pathAsList = this.splitPath(path);


    }

    public DNFSPath(List<String> path) {
        this.pathAsList = path;
        this.pathAsString = this.assemblePath(path);
    }

    public String assemblePath(List<String> pathParts){
        return File.separator + Joiner.on(File.separator).join(pathParts);

    }

    public List<String> splitPath(String path){
        List<String> parts = new ArrayList<String>(Arrays.asList(path.split(File.separator)));
        for (Iterator<String> iterator = parts.listIterator(); iterator.hasNext(); ) {
            String a = iterator.next();
            if (a.isEmpty()) {
                iterator.remove();
            }
        }
        return parts;
    }

    @Override
    public String toString() {
        return this.pathAsString;
    }

    public List<String> getComponents(){
        return this.pathAsList;
    }

    /**
     * @param index
     * @return
     */
    public String getComponent(int index){
        if(index < 0){
            return this.pathAsList.get(this.pathAsList.size() + index);

        }
        else{
            return this.pathAsList.get(index);
        }
    }

    public List<String> getComponents(int from){
        return this.pathAsList.subList(from, this.pathAsList.size());
    }

    /**
     *
     * If the to parameter is negative the value is interpreted as offset to the end, where -1 refers to the last element!
     * @param from
     * @param to last index (exclusive)
     * @return
     */
    public List<String> getComponents(int from, int to){
        if(to < 0){
            return this.pathAsList.subList(from, this.pathAsList.size() + to);
        }
        else{
            return this.pathAsList.subList(from, to);
        }
    }

    /**
     * Returns the length of the path
     * @return
     */
    public int length(){
        return this.pathAsList.size();

    }

    public DNFSPath getSubPath(int from) {
        return new DNFSPath(this.getComponents(from));
    }

    public DNFSPath getSubPath(int from, int to){
        return new DNFSPath(this.getComponents(from, to));
    }
}
