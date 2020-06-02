package com.threadteam.thread.models;

import android.net.Uri;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class User {


    // Instance-specific variables
    private String _id;
    private String _username;
    private String _profileImageURL;
    private String _aboutUsMessage;
    private String _statusMessage;
    private List<Server> _subscribedServers = new ArrayList<Server>();
    // Template User class placeholder
    public User(){

    }

    public User(String _id, String _username, String _profileImageURL, String _aboutUsMessage, String _statusMessage,List<Server> _subscribedServers) {
        if(_aboutUsMessage.trim().equals("")){
            _aboutUsMessage = "No Description";
        }
        if(_statusMessage.trim().equals("")){
            _statusMessage = "No Status";
        }

        this._id = _id;
        this._username = _username;
        this._profileImageURL = _profileImageURL;
        this._aboutUsMessage = _aboutUsMessage;
        this._statusMessage = _statusMessage;
        this._subscribedServers = _subscribedServers;
    }
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_username() {
        return _username;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public String get_profileImageURL() {
        return _profileImageURL;
    }

    public void set_profileImageURL(String _profileImageURL) {
        this._profileImageURL = _profileImageURL;
    }

    public String get_aboutUsMessage() {
        return _aboutUsMessage;
    }

    public void set_aboutUsMessage(String _aboutUsMessage) {
        this._aboutUsMessage = _aboutUsMessage;
    }

    public String get_statusMessage() {
        return _statusMessage;
    }

    public void set_statusMessage(String _statusMessage) {
        this._statusMessage = _statusMessage;
    }

    public List<Server> get_subscribedServers() {
        return _subscribedServers;
    }

    public void set_subscribedServers(List<Server> _subscribedServers) {
        this._subscribedServers = _subscribedServers;
    }
}
