package org.wiflick.wiflickhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.wiflick.wiflickhome.kore.Settings;
import org.wiflick.wiflickhome.kore.eventclient.EventServerConnection;
import org.wiflick.wiflickhome.kore.host.HostInfo;
import org.wiflick.wiflickhome.kore.host.HostManager;
import org.wiflick.wiflickhome.kore.jsonrpc.ApiCallback;
import org.wiflick.wiflickhome.kore.jsonrpc.ApiException;
import org.wiflick.wiflickhome.kore.jsonrpc.HostConnection;
import org.wiflick.wiflickhome.kore.jsonrpc.method.JSONRPC;
import org.wiflick.wiflickhome.kore.service.LibrarySyncService;
import org.wiflick.wiflickhome.kore.ui.BaseActivity;
import org.wiflick.wiflickhome.kore.ui.MoviesActivity;
import org.wiflick.wiflickhome.kore.ui.hosts.AddHostFragmentFinish;
import org.wiflick.wiflickhome.kore.ui.hosts.HostFragmentManualConfiguration;
import org.wiflick.wiflickhome.kore.utils.LogUtils;
import org.wiflick.wiflickhome.kore.utils.NetUtils;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import butterknife.InjectView;


public class ServerDiscoveryActivity extends BaseActivity {
    ProgressBar progressBar;
    TextView messageTextView;
    private static final String TAG = LogUtils.makeLogTag(ServerDiscoveryActivity.class);

    // See http://sourceforge.net/p/xbmc/mailman/message/28667703/
    // _xbmc-jsonrpc-http._tcp
    // _xbmc-jsonrpc-h._tcp
    // _xbmc-jsonrpc-tcp._tcp
    // _xbmc-jsonrpc._tcp
    private static final String MDNS_XBMC_SERVICENAME = "_xbmc-jsonrpc-h._tcp.local.";
    private static final int DISCOVERY_TIMEOUT = 5000;
    final Handler handler2 = new Handler();
    private boolean hostFound=false;
    private float error_msg_location=0;
    WifiReceiver receiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_discovery);


        progressBar=(ProgressBar)findViewById(R.id.progress_bar);
        messageTextView=(TextView)findViewById(R.id.text_message);
        error_msg_location=messageTextView.getY();
        receiver=new WifiReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SearchPinutServer();
     /*   //Launch the movie activity
        Intent intent = new Intent(this, MainActivityNew.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    */


    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
        @Override
    protected void onResume() {
        super.onResume();

            registerReceiver(receiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);
        //noNetworkConnection();
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server_discovery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Whether the user cancelled the search
    private boolean searchCancelled = false;
    private final Object lock = new Object();

    /**
     * Starts the service discovery, setting up the UI accordingly
     */
    public void startZeroConfSearching() {
        Log.d(TAG, "Starting service discovery...");
        searchCancelled = false;
        final Handler handler = new Handler();
        final Thread searchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

                WifiManager.MulticastLock multicastLock = null;
                try {
                    // Get wifi ip address
                    int wifiIpAddress = wifiManager.getConnectionInfo().getIpAddress();
                    InetAddress wifiInetAddress = NetUtils.intToInetAddress(wifiIpAddress);

                    // Acquire multicast lock
                    multicastLock = wifiManager.createMulticastLock("kore2.multicastlock");
                    multicastLock.setReferenceCounted(false);
                    multicastLock.acquire();

                    JmDNS jmDns = (wifiInetAddress != null)?
                            JmDNS.create(wifiInetAddress) :
                            JmDNS.create();

                    // Get the json rpc service list
                    final ServiceInfo[] serviceInfos =
                            jmDns.list(MDNS_XBMC_SERVICENAME, DISCOVERY_TIMEOUT);

                    synchronized (lock) {
                        // If the user didn't cancel the search, and we are sill in the activity
                        if (!searchCancelled ) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if ((serviceInfos == null) || (serviceInfos.length == 0)) {
                                        noHostFound();
                                    } else {
                                        foundHosts(serviceInfos);
                                    }
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Got an IO Exception", e);
                } finally {
                    if (multicastLock != null)
                        multicastLock.release();
                }
            }
        });

        //titleTextView.setText(R.string.searching);
        messageTextView.setText("Searching for Pinut server.");
        messageTextView.setMovementMethod(LinkMovementMethod.getInstance());

        progressBar.setVisibility(View.VISIBLE);

        searchThread.start();
    }

    /**
     * No host was found, present messages and buttons
     */
    public void noHostFound() {
        Log.d(TAG, "No zero conf host is found");
        //messageTextView.setText("Pinut server not found on this network");
        //messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
        //progressBar.setVisibility(View.GONE);

        hostFound=false;

        //if zero conf failed then try to configure using static conf
        configureStaticConnection();

    }

    public void configureStaticConnection() {
        String DEFAULT_HOSTNAME="Pinut Server";
        //String DEFAULT_HOSTADDRESS="192.168.43.1";
        String DEFAULT_HOSTADDRESS="192.168.1.4";
        int DEFAULT_HTTP_PORT=80;
        String DEFAULT_USERNAME="pinut";
        String DEFAULT_PASSWORD="welcome";

        /*HostInfo(String name, String address, int protocol, int httpPort,
        int tcpPort, String username, String password,
        boolean useEventServer, int eventServerPort)
        */

        HostInfo selectedHostInfo = new HostInfo(DEFAULT_HOSTNAME, DEFAULT_HOSTADDRESS, HostConnection.PROTOCOL_TCP,
                DEFAULT_HTTP_PORT, HostInfo.DEFAULT_TCP_PORT, DEFAULT_USERNAME, DEFAULT_PASSWORD, true, HostInfo.DEFAULT_EVENT_SERVER_PORT);
        testConnection(selectedHostInfo);
    }

    /**
     * Found hosts, present them
     * @param serviceInfos Service infos found
     */
    public void foundHosts(final ServiceInfo[] serviceInfos) {
        LogUtils.LOGD(TAG, "Found hosts: " + serviceInfos.length);
        progressBar.setVisibility(View.GONE);
        ServiceInfo selectedServiceInfo = serviceInfos[0];

        String[] addresses = selectedServiceInfo.getHostAddresses();
        if (addresses.length == 0) {
            // Couldn't get any address
            Toast.makeText(this, R.string.wizard_zeroconf_cant_connect_no_host_address, Toast.LENGTH_LONG)
                    .show();
            return;
        }
        String hostName = selectedServiceInfo.getName();
        String hostAddress = addresses[0];
        int hostHttpPort = selectedServiceInfo.getPort();
        HostInfo selectedHostInfo = new HostInfo(hostName, hostAddress, HostConnection.PROTOCOL_TCP,
                hostHttpPort, HostInfo.DEFAULT_TCP_PORT, null, null, true, HostInfo.DEFAULT_EVENT_SERVER_PORT);
        testConnection(selectedHostInfo);

    }

    /**
     * Tests a connection with the values set in the UI.
     * Checks whether the values are correctly set, and then tries to make
     * a ping call. First through HTTP, and if it succeeds, through TCP to
     * check availability. Finally adds the host and advances the wizard
     */
    private void testConnection(HostInfo hostInfo) {
        String xbmcName = hostInfo.getName();
        String xbmcAddress = hostInfo.getAddress();

        int xbmcHttpPort=hostInfo.getHttpPort();

        String xbmcUsername = hostInfo.getUsername();
        String xbmcPassword = hostInfo.getPassword();
        int xbmcTcpPort=hostInfo.getTcpPort();


        int xbmcProtocol = HostConnection.PROTOCOL_TCP ;

        //String macAddress = xbmcMacAddressEditText.getText().toString();
        String macAddress=null ;

        int xbmcWolPort = HostInfo.DEFAULT_WOL_PORT;


        boolean xbmcUseEventServer = hostInfo.getUseEventServer();
        //aux = xbmcEventServerPortEditText.getText().toString();
        int xbmcEventServerPort=hostInfo.getEventServerPort();


        // Ok, let's try to ping the host
        final HostInfo checkedHostInfo = new HostInfo(xbmcName, xbmcAddress, xbmcProtocol,
                xbmcHttpPort, xbmcTcpPort,
                xbmcUsername, xbmcPassword,
                xbmcUseEventServer, xbmcEventServerPort);
        checkedHostInfo.setMacAddress(macAddress);
        checkedHostInfo.setWolPort(xbmcWolPort);

        chainCallCheckHttpConnection(checkedHostInfo);

    }
    private void chainCallCheckHttpConnection(final HostInfo hostInfo) {
        // Let's ping the host through HTTP
        final HostConnection hostConnection = new HostConnection(hostInfo);
        hostConnection.setProtocol(HostConnection.PROTOCOL_HTTP);
        final JSONRPC.Ping httpPing = new JSONRPC.Ping();
        httpPing.execute(hostConnection, new ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtils.LOGD(TAG, "Successfully connected to new host through HTTP.");
                // Great, we managed to connect through HTTP, let's check through tcp
                if (hostInfo.getProtocol() == HostConnection.PROTOCOL_TCP) {
                    chainCallCheckTcpConnection(hostConnection, hostInfo);
                } else {
                    // No TCP, check EventServer
                    hostConnection.disconnect();
                    chainCallCheckEventServerConnection(hostInfo);
                }
            }

            @Override
            public void onError(int errorCode, String description) {
                // Couldn't connect through HTTP, abort, and initialize checkedHostInfo
                hostConnection.disconnect();
                hostConnectionError(errorCode, description);
            }
        }, handler2);
    }

    private void chainCallCheckTcpConnection(final HostConnection hostConnection, final HostInfo hostInfo) {
        final JSONRPC.Ping tcpPing = new JSONRPC.Ping();
        hostConnection.setProtocol(HostConnection.PROTOCOL_TCP);
        tcpPing.execute(hostConnection, new ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                // Great, we managed to connect through HTTP and TCP
                Log.d(TAG, "Successfully connected to new host through TCP.");
                hostConnection.disconnect();
                // Check EventServer
                chainCallCheckEventServerConnection(hostInfo);
            }

            @Override
            public void onError(int errorCode, String description) {
                // We only managed to connect through HTTP, revert checkedHostInfo to use HTTP
                Log.d(TAG, "Couldn't connect to host through TCP. Message: " + description);
                hostConnection.disconnect();
                hostInfo.setProtocol(HostConnection.PROTOCOL_HTTP);
                // Check EventServer
                chainCallCheckEventServerConnection(hostInfo);
            }
        }, handler2);
    }

    private void chainCallCheckEventServerConnection(final HostInfo hostInfo) {
        if (hostInfo.getUseEventServer()) {
            EventServerConnection.testEventServerConnection(
                    hostInfo,
                    new EventServerConnection.EventServerConnectionCallback() {
                        @Override
                        public void OnConnectResult(boolean success) {

                            LogUtils.LOGD(TAG, "Check ES connection: " + success);
                            if (success) {
                                hostConnectionChecked(hostInfo);
                            } else {
                                hostInfo.setUseEventServer(false);
                                hostConnectionChecked(hostInfo);
                            }
                        }
                    },
                    handler2);
        } else {
            hostConnectionChecked(hostInfo);
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Settings.KEY_PREF_CHECKED_EVENT_SERVER_CONNECTION, true)
                .apply();
    }

    /**
     * The connection was checked, and hostInfo has all the correct parameters to communicate
     * with it
     * @param hostInfo {@link HostInfo} to add
     */
    private void hostConnectionChecked(final HostInfo hostInfo) {
        // Let's get the MAC Address, if we don't have one
        if (TextUtils.isEmpty(hostInfo.getMacAddress())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String localMacAddress = NetUtils.getMacAddress(hostInfo.getAddress());
                    hostInfo.setMacAddress(localMacAddress);
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            /*if (isAdded()) {
                                progressDialog.dismiss();
                                listener.onHostManualConfigurationNext(hostInfo);
                            }
                            */
                            onHostManualConfigurationNext(hostInfo);
                        }
                    });
                }
            }).start();
        } else {
            // Mac address was supplied
            /*if (isAdded()) {
                progressDialog.dismiss();
                listener.onHostManualConfigurationNext(hostInfo);
            }*/
            onHostManualConfigurationNext(hostInfo);
        }
    }

    /**
     * Treats errors occurred during the connection check
     * @param errorCode Error code
     * @param description Description
     */
    private void hostConnectionError(int errorCode, String description) {
        Log.d(TAG, "An error occurred during connection testint. Message: " + description);
        messageTextView.setText("Pinut server not found on this network. Please try again later.");
        messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
        //messageTextView.setY(progressBar.getY()+100);
        progressBar.setVisibility(View.GONE);

        //messageTextView.setY(messageTextView.getY()+200);
       /* if (!isAdded()) return;

        progressDialog.dismiss();
        LogUtils.LOGD(TAG, "An error occurred during connection testint. Message: " + description);
        switch (errorCode) {
            case ApiException.HTTP_RESPONSE_CODE_UNAUTHORIZED:
                String username = xbmcUsernameEditText.getText().toString(),
                        password = xbmcPasswordEditText.getText().toString();
                int messageResourceId;
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    messageResourceId = R.string.wizard_empty_authentication;
                } else {
                    messageResourceId = R.string.wizard_incorrect_authentication;
                }
                Toast.makeText(getActivity(), messageResourceId, Toast.LENGTH_SHORT).show();
                xbmcUsernameEditText.requestFocus();
                break;
            default:
                Toast.makeText(getActivity(),
                        R.string.wizard_error_connecting,
                        Toast.LENGTH_SHORT).show();
                break;
        }*/
    }

    public void onHostManualConfigurationNext(HostInfo hostInfo) {
        HostManager hostManager = HostManager.getInstance(this);
        HostInfo newHostInfo = hostManager.addHost(hostInfo);
        hostManager.switchHost(newHostInfo);
        serverConfigurationFinished();
        //switchToFragment(new AddHostFragmentFinish());
    }

    private void serverConfigurationFinished(){
        // Start the syncing process
        Intent syncIntent = new Intent(this, LibrarySyncService.class);
        syncIntent.putExtra(LibrarySyncService.SYNC_ALL_MOVIES, true);
        //syncIntent.putExtra(LibrarySyncService.SYNC_ALL_TVSHOWS, true);
        //syncIntent.putExtra(LibrarySyncService.SYNC_ALL_MUSIC, true);
        //syncIntent.putExtra(LibrarySyncService.SYNC_ALL_MUSIC_VIDEOS, true);
        this.startService(syncIntent);

        //Launch the main activity
        Intent intent = new Intent(this, MainActivityNew.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
    private boolean isPinutNetworkConnected(){

         ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo.isConnected()) {
                final WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                    String SSID=connectionInfo.getSSID();
                    if (SSID.startsWith("\"") && SSID.endsWith("\"")){
                        SSID = SSID.substring(1, SSID.length()-1);
                    }
                    //int ret=SSID.compareToIgnoreCase("D-701");
                    int ret=SSID.compareToIgnoreCase("pinut");
                    if (ret==0)
                        return true;
                    //if (SSID.compareToIgnoreCase("pinut")==0)
                      //  return true;
                }
            }
            else
                return false;

        return false;

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm =(ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private void noNetworkConnection() {
        messageTextView.setText("Please connect to Pinut hotspot and try again.");
        messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
        //messageTextView.setY(progressBar.getY()+100);
        progressBar.setVisibility(View.GONE);
        //messageTextView.setY(messageTextView .getY()+200);
        //messageTextView.setY(messageTextView.getY()+200);
    }

    private class WifiReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(context, "onReceive of WiFiConnectionListener", Toast.LENGTH_LONG);
            Log.d("PINUT", "onReceive of WifiReceiver");
            SearchPinutServer();
        }
    }

    public void SearchPinutServer(){
        //check for network connectivity
        if (isPinutNetworkConnected()==false)
        {
            //show text for connectivity with pinut server
            noNetworkConnection();
        }

        else {
            // Launch discovery thread for zero conf server
            startZeroConfSearching();
        }

    }
}
