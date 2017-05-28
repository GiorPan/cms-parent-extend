package cmsApp.helpers;

/**
 * Created by giorgos on 24/5/2017.
 */
public class Bucket {

    private String uri;

    private String uid;

    private String name;

    private String author;

    private String lockOwn;


    public Bucket(){

    }

    public String getLockOwn() {
        return lockOwn;
    }

    public void setLockOwn(String lockOwn) {
        this.lockOwn = lockOwn;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
