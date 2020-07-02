package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Post {

    // DATA STORE

    // UNIQUE IDENTIFIER, EXCLUDE FROM FIREBASE CHILD VALUES.
    // SHOULD BE USED AS A KEY
    @Exclude
    private String _id;

    private String _imageLink;
    private String _title;
    private String _message;
    private String _senderID;
    private String _senderUsername;

    // TIMESTAMP FOR LOCAL USE. SHOULD NOT BE SYNCED WITH FIREBASE.
    // FETCH FIREBASE'S timestamp CHILD VALUE FOR A MORE ACCURATE TIMESTAMP.
    @Exclude
    private Long timestampMillis;

    // CONSTRUCTORS

    // FIREBASE REQUIRED BLANK CONSTRUCTOR
    public Post() {}

    public Post(String imageLink, String title, String message, String senderID, String senderUsername) {
        this._imageLink = imageLink;
        this._title = title;
        this._message = message;
        this._senderID = senderID;
        this._senderUsername = senderUsername;

        // LEAVE TIMESTAMP EMPTY FOR SOLE-SERVER SIDE GENERATION
    }

    public Post(String imageLink, String title, String message, String senderID, String senderUsername, Long timestampMillis) {
        this._imageLink = imageLink;
        this._title = title;
        this._message = message;
        this._senderID = senderID;
        this._senderUsername = senderUsername;

        this.timestampMillis = timestampMillis;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_imageLink() {
        return _imageLink;
    }

    public void set_imageLink(String _imageLink) {
        this._imageLink = _imageLink;
    }

    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public String get_message() {
        return _message;
    }

    public void set_message(String _message) {
        this._message = _message;
    }

    public String get_senderID() {
        return _senderID;
    }

    public void set_senderID(String _senderID) {
        this._senderID = _senderID;
    }

    public String get_senderUsername() {
        return _senderUsername;
    }

    public void set_senderUsername(String _senderUsername) {
        this._senderUsername = _senderUsername;
    }

    public Long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(Long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    // METHOD OVERRIDES

    @Override
    public String toString() {
        return "Post{" +
                "_id='" + _id + '\'' +
                ", _imageLink='" + _imageLink + '\'' +
                ", _title='" + _title + '\'' +
                ", _message='" + _message + '\'' +
                ", _senderID='" + _senderID + '\'' +
                ", _senderUsername='" + _senderUsername + '\'' +
                ", timestampMillis=" + timestampMillis +
                '}';
    }
}
