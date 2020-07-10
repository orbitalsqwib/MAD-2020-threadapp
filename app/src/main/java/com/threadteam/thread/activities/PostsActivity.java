package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.RecyclerTouchListener;
import com.threadteam.thread.Utils;
import com.threadteam.thread.adapters.PostsItemAdapter;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.models.Post;

import java.util.ArrayList;
import java.util.HashMap;

public class PostsActivity extends ServerBaseActivity {

    // LOGGING
    private LogHandler logHandler = new LogHandler("Posts Activity");

    // FIREBASE
    //
    // currentUser:             CURRENT USER FOR THE CURRENT SESSION
    // firebaseAuth:            FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION
    // databaseRef:             FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION
    // postListener:            TODO: document this var

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;
    private ChildEventListener postListener;

    // DATA STORE
    //
    // serverId:                CONTAINS CURRENT SERVER ID DATA
    // adapter:                 ADAPTER FOR POSTS RECYCLER VIEW.
    // scrollToLatestPost:      TOGGLE FOR SCROLL TO BOTTOM UPON POST ADDED. DOES THIS ACTION IF TRUE.

    private String serverId;
    private PostsItemAdapter adapter;
    private Boolean scrollToLatestPost = false;

    // VIEW OBJECTS
    //
    // PostsRecyclerView:       DISPLAYS ALL POSTS IN THE SERVER. USES adapter AS ITS ADAPTER.
    // TopNavToolbar:           TOOLBAR OBJECT THAT HANDLES UPWARDS NAVIGATION AND THE TITLE
    // BottomToolbarAMV:        HANDLES THE MENU FOR THE BOTTOM TOOLBAR.
    // MainActionFAB:           BOTTOM TOOLBAR MAIN ACTION BUTTON. USED TO HANDLE MAIN ACTIONS ON THIS
    //                          ACTIVITY. TODO: document this var

    private RecyclerView PostsRecyclerView;
    private ActionMenuView BottomToolbarAMV;
    private Toolbar TopNavToolbar;
    private ImageButton MainActionFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        // BIND TOOLBARS
        // NOTE:    IT IS IMPORTANT TO GET THE INCLUDE VIEWS BEFORE DOING FIND VIEW BY ID.
        //          THIS ENSURES THAT ANDROID CAN ALWAYS FIND THE CORRECT VIEW OBJECT.

        View topNavView = findViewById(R.id.postsNavBarInclude);
        View bottomToolbarView = findViewById(R.id.postsBottomToolbarInclude);
        TopNavToolbar = (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
        BottomToolbarAMV = (ActionMenuView) bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
        MainActionFAB = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);

        // SETUP TOOLBARS
        TopNavToolbar.setTitle("Posts");
        this.setSupportActionBar(TopNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BottomToolbarAMV.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_add_white_24);
        MainActionFAB.setImageDrawable(icon);
        MainActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ToAddPost = new Intent(PostsActivity.this, AddPostActivity.class);
                startActivity(ToAddPost);
            }
        });

        // BIND VIEW OBJECTS
        PostsRecyclerView = (RecyclerView) findViewById(R.id.postsRecyclerView);

        // SETUP VIEW OBJECTS
        adapter = new PostsItemAdapter(new ArrayList<Post>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        PostsRecyclerView.setLayoutManager(layoutManager);
        PostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        PostsRecyclerView.setAdapter(adapter);

        PostsRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(this, PostsRecyclerView, new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        handleTransitionIntoPost(position);
                    }
                })
        );

        final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

                if(dy < 0 && scrollToLatestPost) {
                    logHandler.printLogWithMessage("Scrolled up, toggled scrollToLatestPost = false!");
                    scrollToLatestPost = false;

                } else if(llm != null && llm.findLastCompletelyVisibleItemPosition() == adapter.postList.size()-1) {
                    logHandler.printLogWithMessage("Scrolled to bottom of posts, setting scrollToLatestPost = true!");
                    scrollToLatestPost = true;
                }

            }
        };

        // Get serverId from Intent
        final Intent dataReceiver = getIntent();
        serverId = dataReceiver.getStringExtra("SERVER_ID");

        if (serverId == null) {
            logHandler.printGetExtrasResultLog("SERVER_ID", "null");
        }
        logHandler.printGetExtrasResultLog("SERVER_ID", serverId);

        // INITIALISE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null) {
            logHandler.printDefaultLog(LogHandler.FIREBASE_USER_NOT_FOUND);
            Intent backToLogin = new Intent(PostsActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference();

        // INITIALISE LISTENERS

        // postListener:    HANDLES LOADING OF ALL POSTS, AS WELL AS UPDATING THE ADAPTER
        //                  ON MESSAGE ADDED/DELETED/CHANGED EVENTS
        //                  CORRECT INVOCATION CODE: databaseRef.child("posts")
        //                                                      .child(serverId)
        //                                                      .addChildEventListener(postListener)
        //                  SHOULD BE CANCELLED UPON ACTIVITY STOP!

        postListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                logHandler.printLogWithMessage("Post added/loaded!");

                if(dataSnapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", "null");
                    return;
                }

                if(dataSnapshot.getValue() == null) {
                    logHandler.printDatabaseResultLog(".getValue()", "Post Values", "postListener", "null");
                    return;
                }

                String imageLink = (String) dataSnapshot.child("_imageLink").getValue();
                String title = (String) dataSnapshot.child("_title").getValue();
                String message = (String) dataSnapshot.child("_message").getValue();
                String senderUID = (String) dataSnapshot.child("_senderUID").getValue();
                String sender = (String) dataSnapshot.child("_sender").getValue();
                Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

                Post post;

                if (imageLink == null) {
                    logHandler.printDatabaseResultLog(".child(\"_imageLink\").getValue()", "Image Link", "postListener", "null");
                    return;
                } else if (title == null) {
                    logHandler.printDatabaseResultLog(".child(\"_title\").getValue()", "Title", "postListener", "null");
                    return;
                } else if (message == null) {
                    logHandler.printDatabaseResultLog(".child(\"_message\").getValue()", "Message", "postListener", "null");
                    return;
                } else if (senderUID == null) {
                    logHandler.printDatabaseResultLog(".child(\"_senderUID\").getValue()", "Sender ID", "postListener", "null");
                    return;
                } else if (sender == null) {
                    logHandler.printDatabaseResultLog(".child(\"_sender\").getValue()", "Sender Username", "postListener", "null");
                    return;
                } else if (timestampMillis == null) {
                    logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "postListener", "null");
                    post = new Post(imageLink, title, message, senderUID, sender);
                } else {
                    post = new Post(imageLink, title, message, senderUID, sender, timestampMillis);
                }

                post.set_id(dataSnapshot.getKey());

                logHandler.printDatabaseResultLog("", "Post", "postListener", post.toString());

                adapter.postList.add(post);
                adapter.notifyItemInserted(adapter.postList.size());

                if(scrollToLatestPost) {
                    logHandler.printLogWithMessage("scrollToLatestPost = true; scrolling to latest post now!");
                    PostsRecyclerView.smoothScrollToPosition(Math.max(0, adapter.postList.size() - 1));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                logHandler.printLogWithMessage("Post changed! Updating timestamp!");

                // Note: Future features may introduce editing of posts, but for now it's just for updating the timestamp
                //       when Firebase Cloud Functions gives us the server side timestamp.

                if(dataSnapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", "null");
                    return;
                }
                logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", dataSnapshot.getKey());

                Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

                if(timestampMillis == null) {
                    logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "postListener", "null");
                    return;
                }
                logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "postListener", timestampMillis.toString());

                String postId = dataSnapshot.getKey();

                for(int i=0; i<adapter.postList.size(); i++) {
                    if(adapter.postList.get(i).get_id() != null && adapter.postList.get(i).get_id().equals(postId)) {
                        adapter.postList.get(i).setTimestampMillis(timestampMillis);
                        adapter.notifyItemChanged(i);
                        return;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                logHandler.printLogWithMessage("Post removed! Deleting post from adapter!");

                if(dataSnapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", "null");
                    return;
                }
                logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", dataSnapshot.getKey());

                for(int i=0; i<adapter.postList.size(); i++) {
                    if(adapter.postList.get(i).get_id() != null &&
                            adapter.postList.get(i).get_id().equals(dataSnapshot.getKey())) {
                        adapter.postList.remove(i);
                        adapter.notifyItemRemoved(i);
                        return;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        databaseRef.child("posts")
                .child(serverId)
                .addChildEventListener(postListener);
    }

    private void handleTransitionIntoPost(Integer position) {
        //TODO: go to view post activity (when complete)
    }

    @Override
    protected void onDestroy() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_DESTROY);
        Utils.ToggleMenuItemAlpha(
                this,
                R.id.viewProfileMenuItem,
                "View Profile",
                R.drawable.round_face_white_36,
                "round_face_white_36",
                true,
                logHandler
        );
        super.onDestroy();
    }

    // TOOLBAR OVERRIDE METHODS

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Make ViewServers Button on menu bar look disabled
        Utils.ToggleMenuItemAlpha(
                this,
                R.id.postsMenuItem,
                "Posts",
                R.drawable.round_all_inbox_white_36,
                "round_all_inbox_white_26",
                false,
                logHandler
        );
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        addServerMenuItemsToMenu(menu);
        getMenuInflater().inflate(R.menu.server_menu, BottomToolbarAMV.getMenu());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.postsMenuItem:
                // DISABLED
                return false;

            case R.id.chatMenuItem:
                logHandler.printLogWithMessage("User tapped on Chat Menu Item!");

                HashMap<String, String> extraMap = new HashMap<String, String>();
                extraMap.put("SERVER_ID", serverId);
                Utils.StartActivityOnNewStack(
                        PostsActivity.this,
                        ChatActivity.class,
                        "Chat Activity",
                        extraMap,
                        logHandler);

                // Reset disabled ActionMenuItemView button back to normal state
                Utils.ToggleMenuItemAlpha(
                        this,
                        R.id.postsMenuItem,
                        "Posts",
                        R.drawable.round_all_inbox_white_36,
                        "round_all_inbox_white_26",
                        true,
                        logHandler
                );

                finish();
                return true;

            case R.id.membersMenuItem:
                return false;

            case android.R.id.home:
                logHandler.printLogWithMessage("User tapped on Back Button!");

                Utils.StartActivityOnNewStack(
                        PostsActivity.this,
                        ViewServersActivity.class,
                        "View Servers Activity",
                        null,
                        logHandler);
                break;

            case SHARE_SERVER_MENU_ITEM:
                logHandler.printLogWithMessage("User tapped on Share Server Menu Item!");
                ConstraintLayout baseLayer = (ConstraintLayout) findViewById(R.id.basePostsConstraintLayout);
                showShareServerPopup(baseLayer, serverId);
                break;

            case LEAVE_SERVER_MENU_ITEM:
                logHandler.printLogWithMessage("User tapped on Leave Server Menu Item!");
                handleLeaveServerAlert(serverId, currentUser.getUid());
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}