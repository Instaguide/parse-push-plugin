package com.phonegap.parsepushplugin;

import java.util.List;
import java.util.ArrayList;
import java.lang.Exception;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.parse.Parse;
import com.parse.ParsePush;
import com.parse.ParseInstallation;

import android.util.Log;

public class ParsePushPlugin extends CordovaPlugin {
    public static final String ACTION_GET_INSTALLATION_ID = "getInstallationId";
    public static final String ACTION_GET_INSTALLATION_OBJECT_ID = "getInstallationObjectId";
    public static final String ACTION_GET_SUBSCRIPTIONS = "getSubscriptions";
    public static final String ACTION_SUBSCRIBE = "subscribe";
    public static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    public static final String ACTION_REGISTER_CALLBACK = "registerCallback";

    private static CallbackContext gEventCallback = null;

    //////private static String gECB;
    private static CordovaWebView gWebView;
    private static boolean gForeground = false;
    private static boolean isDestroy = false;


    public static final String LOGTAG = "ParsePushPlugin";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    	if (action.equals(ACTION_REGISTER_CALLBACK)){
    		gEventCallback = callbackContext;
    		return true;
    	}

        if (action.equals(ACTION_GET_INSTALLATION_ID)) {
            this.getInstallationId(callbackContext);
            return true;
        }

        if (action.equals(ACTION_GET_INSTALLATION_OBJECT_ID)) {
            this.getInstallationObjectId(callbackContext);
            return true;
        }
        if (action.equals(ACTION_GET_SUBSCRIPTIONS)) {
            this.getSubscriptions(callbackContext);
            return true;
        }
        if (action.equals(ACTION_SUBSCRIBE)) {
            this.subscribe(args.getString(0), callbackContext);
            return true;
        }
        if (action.equals(ACTION_UNSUBSCRIBE)) {
            this.unsubscribe(args.getString(0), callbackContext);
            return true;
        }
        return false;
    }


    private void getInstallationId(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                String installationId = ParseInstallation.getCurrentInstallation().getInstallationId();
                callbackContext.success(installationId);
            }
        });
    }

    private void getInstallationObjectId(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                String objectId = ParseInstallation.getCurrentInstallation().getObjectId();
                callbackContext.success(objectId);
            }
        });
    }

    private void getSubscriptions(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
            	List<String> subscriptions = ParseInstallation.getCurrentInstallation().getList("channels");
                callbackContext.success(subscriptions.toString());
            }
        });
    }

    private void subscribe(final String channel, final CallbackContext callbackContext) {
    	ParsePush.subscribeInBackground(channel);
        callbackContext.success();
    }

    private void unsubscribe(final String channel, final CallbackContext callbackContext) {
    	ParsePush.unsubscribeInBackground(channel);
        callbackContext.success();
    }

    /*
     * keep reusing the saved callback context to call the javascript PN handler
     */
    public static void jsCallback(JSONObject _json){
    	jsCallback(_json, "RECEIVE");
    }
    public static void jsCallback(JSONObject _json, String pushAction){
    	List<PluginResult> cbParams = new ArrayList<PluginResult>();
    	cbParams.add(new PluginResult(PluginResult.Status.OK, _json));
    	cbParams.add(new PluginResult(PluginResult.Status.OK, pushAction));

    	PluginResult dataResult = new PluginResult(PluginResult.Status.OK, cbParams);
        dataResult.setKeepCallback(true);

        if(gEventCallback != null){
            gEventCallback.sendPluginResult(dataResult);
        }
    }


    public static boolean isJavascriptReady(){
    	/////return gECB != null && !gECB.isEmpty() && gWebView != null;
    	return gWebView != null;
    }

    @Override
    protected void pluginInitialize() {
    	/////gECB = null;
    	gWebView = this.webView;
    	gForeground = true;
        isDestroy = false;
        Log.d(LOGTAG, "IG:: inside pluginInitialize, app isDestroy: " + isDestroy);
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        gForeground = false;
        Log.d(LOGTAG, "IG:: inside onPause, app isDestroy: " + isDestroy);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        gForeground = true;
        isDestroy = false;
        Log.d(LOGTAG, "IG:: inside onResume, app isDestroy: " + isDestroy);
    }


    @Override
    public void onDestroy() {
    	/////gECB = null;
    	gWebView = null;
    	gForeground = false;
        isDestroy = true;
        Log.d(LOGTAG, "IG:: inside onDestroy, app isDestroy: " + isDestroy);
        super.onDestroy();
    }

    public static boolean isInForeground(){
      return gForeground;
    }

    public static boolean isDestroy() {
        return isDestroy;
    }
}
