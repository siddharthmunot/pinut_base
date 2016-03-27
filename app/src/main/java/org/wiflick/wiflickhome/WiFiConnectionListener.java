package org.wiflick.wiflickhome;

 import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by mdimran on 2/19/2016.
 */
public class WiFiConnectionListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "onReceive of WiFiConnectionListener", Toast.LENGTH_LONG);
        Log.d("PINUT", "onReceive of WifiClientListener2");

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null && info.isConnected()) {
            // Do your work.

            // e.g. To check the Network Name or other info:
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String SSID = wifiInfo.getSSID();
            if (SSID.startsWith("\"") && SSID.endsWith("\"")){
                SSID = SSID.substring(1, SSID.length()-1);
            }
            //int ret=SSID.compareToIgnoreCase("D-701");
            int ret=SSID.compareToIgnoreCase("pinut");
            if (ret==0){
            //Launch the main activity
                //Intent intent2 = new Intent(context, ServerDiscoveryActivity.class);
                //intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                //context.startActivity(intent2);
            }



        }

    }
}
