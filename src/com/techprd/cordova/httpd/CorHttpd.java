package src.com.techprd.cordova.httpd;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.content.Context;
import android.content.res.AssetManager;

/**
 * This class echoes a string called from JavaScript.
 */
public class CorHttpd extends CordovaPlugin {

    /**
     * Common tag used for logging statements.
     */
    private static final String LOG_TAG = "CorHttpd";

    /**
     * Cordova Actions.
     */
    private static final String ACTION_START_SERVER = "startServer";
    private static final String ACTION_STOP_SERVER = "stopServer";
    private static final String ACTION_GET_URL = "getURL";
    private static final String ACTION_GET_LOCAL_PATH = "getLocalPath";

    private static final String OPT_WWW_ROOT = "www_root";
    private static final String OPT_PORT = "port";
    private static final String OPT_LOCALHOST_ONLY = "localhost_only";

    private int port = 8888;
    private boolean localhost_only = false;

    private String localPath = "";
    private ArrayList<WebServer> webServers = new ArrayList<WebServer>();
    private String url = "";

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;
        if (ACTION_START_SERVER.equals(action)) {
            result = startServer(inputs, callbackContext);

        } else if (ACTION_STOP_SERVER.equals(action)) {
            result = stopServer(inputs, callbackContext);

        } else if (ACTION_GET_URL.equals(action)) {
            result = getURL(inputs, callbackContext);

        } else if (ACTION_GET_LOCAL_PATH.equals(action)) {
            result = getLocalPath(inputs, callbackContext);

        } else {
            Log.d(LOG_TAG, String.format("Invalid action passed: %s", action));
            result = new PluginResult(Status.INVALID_ACTION);
        }

        if (result != null) callbackContext.sendPluginResult(result);

        return true;
    }

    private String __getLocalIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        if (addr instanceof Inet4Address) {
                            String sAddr = addr.getHostAddress().toUpperCase();
                            if (intf.getDisplayName().startsWith("wlan")) {
                                Log.w(LOG_TAG, "local IP: " + sAddr);
                                return sAddr;
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(LOG_TAG, ex.toString());
        }

        return "127.0.0.1";
    }

    private PluginResult startServer(JSONArray inputs, CallbackContext callbackContext) {
        Log.d(LOG_TAG, "startServer");

        JSONObject options = inputs.optJSONObject(0);
        if (options == null) return null;

        String www_root = options.optString(OPT_WWW_ROOT);
        port = options.optInt(OPT_PORT, 8888);
        localhost_only = options.optBoolean(OPT_LOCALHOST_ONLY, false);

        if (www_root.startsWith("/")) {
            //localPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            localPath = www_root;
        } else {
            //localPath = "file:///android_asset/www";
            localPath = "www";
            if (www_root.length() > 0) {
                localPath += "/";
                localPath += www_root;
            }
        }

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String errmsg = __startServer();
                if (errmsg.length() > 0) {
                    delayCallback.error(errmsg);
                } else {
                    if (localhost_only) {
                        url = "http://127.0.0.1:" + port;
                    } else {
                        url = "http://" + __getLocalIpAddress() + ":" + port;
                    }
                    delayCallback.success(url);
                }
            }
        });

        return null;
    }

    private String __startServer() {
        String errmsg = "";
        try {
            AndroidFile f = new AndroidFile(localPath);

            Context ctx = cordova.getActivity().getApplicationContext();
            AssetManager am = ctx.getResources().getAssets();
            f.setAssetManager(am);

            WebServer server = null;
            if (localhost_only) {
                InetSocketAddress localAddr = new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), port);
                server = new WebServer(localAddr, f);
                webServers.add(server);
            } else {
                server = new WebServer(port, f);
                webServers.add(server);
            }
        } catch (IOException e) {
            errmsg = String.format("IO Exception: %s", e.getMessage());
            Log.w(LOG_TAG, errmsg);
        }
        return errmsg;
    }

    private void __stopServer() {
        if (webServers.size() > 0) {
            for (WebServer webServer : webServers) {
                webServer.stop();
            }
            webServers.clear();
        }
    }

    private PluginResult getURL(JSONArray inputs, CallbackContext callbackContext) {
        Log.d(LOG_TAG, "getURL");

        callbackContext.success(this.url);
        return null;
    }

    private PluginResult getLocalPath(JSONArray inputs, CallbackContext callbackContext) {
        Log.d(LOG_TAG, "getLocalPath");

        callbackContext.success(this.localPath);
        return null;
    }

    private PluginResult stopServer(JSONArray inputs, CallbackContext callbackContext) {
        Log.d(LOG_TAG, "stopServer");

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                __stopServer();
                url = "";
                localPath = "";
                delayCallback.success();
            }
        });

        return null;
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app
     */
    public void onPause(boolean multitasking) {
        //if(! multitasking) __stopServer();
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app
     */
    public void onResume(boolean multitasking) {
        //if(! multitasking) __startServer();
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    public void onDestroy() {
        __stopServer();
    }
}
