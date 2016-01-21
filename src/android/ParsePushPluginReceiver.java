package com.phonegap.parsepushplugin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ionicframework.instaguide321604.MainActivity;
import com.parse.ParsePushBroadcastReceiver;
import org.json.JSONException;
import org.json.JSONObject;


public class ParsePushPluginReceiver extends ParsePushBroadcastReceiver
{
	public static final String LOGTAG = "ParsePushPluginReceiver";
	public static final String PARSE_DATA_KEY = "com.parse.Data";
	private static final String PAYLOAD = "payload";
	private static final String PARAM = "param";

	private static JSONObject MSG_COUNTS = new JSONObject();


	@Override
	protected void onPushReceive(Context context, Intent intent) {
		Log.d(LOGTAG, "onPushReceive - context: " + context);

        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(getNotificationTag(context, intent), 0, getNotification(context, intent));

		if (!com.phonegap.parsepushplugin.ParsePushPlugin.isDestroy()) {
			// relay the push notification data to the javascript
			com.phonegap.parsepushplugin.ParsePushPlugin.jsCallback(getPushData(intent));
		}
	}

	@Override
    protected void onPushOpen(Context context, Intent intent) {
		Log.d(LOGTAG, "onPushOpen - context: " + context);

		JSONObject pnData = getPushData(intent);
		resetCount(getNotificationTag(context, pnData));

		String uriString = pnData.optString("uri");
		Intent activityIntent = uriString.isEmpty() ? new Intent(context, getActivity(context, intent))
				: new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));

		activityIntent.putExtras(intent)
				.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

		if (pnData.has(PAYLOAD)) {
			JSONObject payload = pnData.optJSONObject(PAYLOAD);
			activityIntent.putExtra(MainActivity.REDIRECT_URI, payload.optString(MainActivity.REDIRECT_URI));
			if (payload.has(PARAM)) {
				JSONObject param = payload.optJSONObject(PARAM);
				if (param.has(MainActivity.QUERY_ID)) {
					activityIntent.putExtra(MainActivity.QUERY_ID, param.optString(MainActivity.QUERY_ID));
				}
				if (param.has(MainActivity.RESPONSE_ID)) {
					activityIntent.putExtra(MainActivity.RESPONSE_ID, param.optString(MainActivity.RESPONSE_ID));
				}
				if (param.has(MainActivity.SEEKER_USER_ID)) {
					activityIntent.putExtra(MainActivity.SEEKER_USER_ID, param.optString(MainActivity.SEEKER_USER_ID));
				}
				if (param.has(MainActivity.EXPERT_USER_ID)) {
					activityIntent.putExtra(MainActivity.EXPERT_USER_ID, param.optString(MainActivity.EXPERT_USER_ID));
				}
			}
		}

		//
		// allow a urlHash parameter for hash as well as query params.
		// This lets the app know what to do at coldstart by opening a PN.
		// For example: navigate to a specific page of the app
		String urlHash = pnData.optString("urlHash");
		if(urlHash.startsWith("#") || urlHash.startsWith("?")){
			activityIntent.putExtra("urlHash", urlHash);
		}
		context.startActivity(activityIntent);
    }

	private static JSONObject getPushData(Intent intent){
		JSONObject pnData = null;
		try {
            pnData = new JSONObject(intent.getStringExtra(PARSE_DATA_KEY));
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException while parsing push data:", e);
        } finally{
        	return pnData;
        }
	}

	private static String getAppName(Context context){
		CharSequence appName = context.getPackageManager()
					                  .getApplicationLabel(context.getApplicationInfo());
		return (String)appName;
	}

	private static String getNotificationTag(Context context, Intent intent){
		return getPushData(intent).optString("title", getAppName(context));
	}

	private static String getNotificationTag(Context context, JSONObject pnData){
		return pnData.optString("title", getAppName(context));
	}

	private static int nextCount(String pnTag){
		try {
			MSG_COUNTS.put(pnTag, MSG_COUNTS.optInt(pnTag, 0) + 1);
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException while computing next pn count for tag: [" + pnTag + "]", e);
        } finally{
        	return MSG_COUNTS.optInt(pnTag, 0);
        }
	}

	private static void resetCount(String pnTag){
		try {
			MSG_COUNTS.put(pnTag, 0);
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException while resetting pn count for tag: [" + pnTag + "]", e);
        }
	}
}