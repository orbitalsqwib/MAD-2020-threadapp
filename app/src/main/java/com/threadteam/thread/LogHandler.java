package com.threadteam.thread;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseError;

/**
 * Handles logging for the app.
 * Logging can be toggled on/off here.
 * Contains default message functions/constants to standardise logs.
 *
 * @author Eugene Long
 * @version 1.0
 * @since 1.0
 */

public class LogHandler {

    // DATA STORE
    private String activityName;

    // PRIVATE CONSTANTS
    private static final String LogTAG = "ThreadApp: ";
    private static final boolean loggingEnabled = true;

    // PUBLIC DEFAULT LOG MESSAGE CONSTANTS

    // STATES
    public static final int STATE_ON_CREATE = -1;
    public static final int STATE_ON_START = -2;
    public static final int STATE_ON_RESUME = -3;
    public static final int STATE_ON_PAUSE = -4;
    public static final int STATE_ON_STOP = -5;
    public static final int STATE_ON_RESTART = -6;
    public static final int STATE_ON_DESTROY = -7;

    // ACTIONS
    public static final int TOOLBAR_BOUND = 0;
    public static final int TOOLBAR_SETUP = 1;
    public static final int VIEW_OBJECTS_BOUND = 2;
    public static final int VIEW_OBJECTS_SETUP = 3;

    // FIREBASE
    public static final int FIREBASE_INITIALISED = 4;
    public static final int FIREBASE_LISTENERS_INITIALISED = 5;
    public static final int FIREBASE_USER_NOT_FOUND = 6;
    public static final int FIREBASE_USER_FOUND = 7;

    // CONSTRUCTOR
    public LogHandler(String activityName) {
        this.activityName = activityName;
    }

    // INSTANCE METHODS
    public void printDefaultLog(@NonNull int defaultLogMessage) {
        String logMessage;
        switch (defaultLogMessage) {
            case -1:
                logMessage = "State: Creating!";
                break;
            case -2:
                logMessage = "State: Starting!";
                break;
            case -3:
                logMessage = "State: Resuming!";
                break;
            case -4:
                logMessage = "State: Pausing!";
                break;
            case -5:
                logMessage = "State: Stopping!";
                break;
            case -6:
                logMessage = "State: Restarting!";
                break;
            case -7:
                logMessage = "State: Destroying!";
                break;
            case 0:
                logMessage = "Toolbars have been bound!";
                break;
            case 1:
                logMessage = "Toolbars have been setup!";
                break;
            case 2:
                logMessage = "View Objects have been bound!";
                break;
            case 3:
                logMessage = "View Objects have been setup!";
                break;
            case 4:
                logMessage = "Firebase has been initialized!";
                break;
            case 5:
                logMessage = "Firebase: Value Event Listeners have been initialized!";
                break;
            case 6:
                logMessage = "Firebase: Current User cannot be found!";
                break;
            case 7:
                logMessage = "Firebase: Current User has been found!";
                break;
            default:
                logMessage = "Invalid default log message! Check that you've used a correct default log message value!";
        }
        printLogWithMessage(logMessage);
    }

    public void printDatabaseErrorLog(@NonNull DatabaseError databaseError) {
        printLogWithMessage("Database Error! Error description follows: " + databaseError.getDetails());
    }

    /**
     * The default logging method for Firebase Realtime Database results.
     * @param proceedingCode Code for data being checked proceeding "dataSnapshot", i.e ".getKey()"
     * @param valueName Human readable name for what the value being checked is. i.e "Server Id"
     * @param listenerName Name of the listener where the check occurs. To aid debugging
     * @param resultValue The result returned by Firebase as a string.
     */

    public void printDatabaseResultLog(@NonNull String proceedingCode, @NonNull String valueName, @NonNull String listenerName, @NonNull String resultValue) {
        printLogWithMessage("Database returned " + resultValue + " for dataSnapshot" + proceedingCode + " (" + valueName + ") in " + listenerName + "!");
    }

    public void printActivityIntentLog(@NonNull String targetActivityName) {
        printLogWithMessage("Transitioning from " + this.activityName + " to " + targetActivityName + "!");
    }

    public void printIntentExtrasLog(@NonNull String extraKey, @NonNull String extraValue) {
        printLogWithMessage("Intent Payload has key-value pair: " + extraKey + " : " + extraValue + "!");
    }

    public void printGetExtrasResultLog(@NonNull String extraKey, @NonNull String extraValue) {
        printLogWithMessage("Retrieved key-value pair: " + extraKey + " : " + extraValue + " from intent!");
    }

    public void printLogWithMessage(@NonNull String logMessage) {
        if (loggingEnabled) {
            Log.v(LogTAG, this.activityName + " : " + logMessage);
        }
    }

    // PUBLIC STATIC METHODS
    public static void staticPrintLog(@NonNull String logMessage) {
        if(loggingEnabled) {
            Log.v(LogTAG, logMessage);
        }
    }

}
