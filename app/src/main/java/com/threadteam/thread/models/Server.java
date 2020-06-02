package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class Server {

    /*
    DATA MODEL FOR THREAD APP ~ with reference from https://firebase.google.com/docs/database/android/structure-data
    updated as of 3:07AM, 28 May 2020
    - users
        - userId -> view profile
            - user_data
            - subscribed_servers
    - servers -> view servers
        - serverId <- use serverId for loading information when user goes into a server-related activity
            - name
            - owner
    - members -> view members when in server
        - serverId
            - memberId
    - messages -> view chat when in server
        - serverId
            - messageId
                - sender
                - messageText
                - timestamp
     */

    // Server identifier
    @Exclude
    private String _id;

    // ServerID child attributes
    private String _ownerID;
    private String _name;
    private String _desc;

    // Empty constructor for Firebase Object
    public Server() {}

    // For Server creation without id
    public Server(String ownerID, String serverName, String serverDesc) {
        this._ownerID = ownerID;
        this._name = serverName;
        this._desc = serverDesc;
    }

    // Getter-Setters
    public String get_ownerID() { return _ownerID; }

    public void set_ownerID(String _ownerID) { this._ownerID = _ownerID; }

    public String get_id() { return _id; }

    public void set_id(String _id) { this._id = _id; }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) { this._name = _name; }

    public String get_desc() { return _desc; }

    public void set_desc(String _desc) { this._desc = _desc; }
}
