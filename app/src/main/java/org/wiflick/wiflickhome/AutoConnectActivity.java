package org.wiflick.wiflickhome;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.wiflick.wiflickhome.kore.host.HostInfo;
import org.wiflick.wiflickhome.kore.ui.BaseActivity;
import org.wiflick.wiflickhome.kore.ui.hosts.AddHostFragmentZeroconf;
import org.wiflick.wiflickhome.kore.ui.hosts.HostFragmentManualConfiguration;


public class AutoConnectActivity extends BaseActivity implements AutoConncetServerFragment.AddHostZeroconfListener{

    private Fragment previousFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_connect);
        switchToFragment(new AutoConncetServerFragment());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_auto_connect, menu);
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

    /**
     * Search hosts fragment callbacks
     */
    public void onAddHostZeroconfNoHost() {
        HostFragmentManualConfiguration fragment = new HostFragmentManualConfiguration();
        Bundle args = new Bundle();
        args.putString(HostFragmentManualConfiguration.CANCEL_BUTTON_LABEL_ARG,
                getString(R.string.previous));
        fragment.setArguments(args);
        switchToFragment(fragment);
    }

    public void onAddHostZeroconfFoundHost(HostInfo hostInfo) {
        HostFragmentManualConfiguration fragment = new HostFragmentManualConfiguration();

        Bundle args = new Bundle();
        if (hostInfo != null) {
            args.putString(HostFragmentManualConfiguration.HOST_NAME, hostInfo.getName());
            args.putString(HostFragmentManualConfiguration.HOST_ADDRESS, hostInfo.getAddress());
            args.putInt(HostFragmentManualConfiguration.HOST_HTTP_PORT, hostInfo.getHttpPort());
            args.putInt(HostFragmentManualConfiguration.HOST_TCP_PORT, hostInfo.getTcpPort());
            args.putString(HostFragmentManualConfiguration.HOST_USERNAME, hostInfo.getUsername());
            args.putString(HostFragmentManualConfiguration.HOST_PASSWORD, hostInfo.getPassword());
            args.putInt(HostFragmentManualConfiguration.HOST_PROTOCOL, hostInfo.getProtocol());
            // Ignore Mac Address and Wol Port
            args.putBoolean(HostFragmentManualConfiguration.HOST_USE_EVENT_SERVER, hostInfo.getUseEventServer());
            args.putInt(HostFragmentManualConfiguration.HOST_EVENT_SERVER_PORT, hostInfo.getEventServerPort());

            // Send this fragment straight to test
            args.putBoolean(HostFragmentManualConfiguration.GO_STRAIGHT_TO_TEST, true);
            fragment.setArguments(args);
        }
        args.putString(HostFragmentManualConfiguration.CANCEL_BUTTON_LABEL_ARG,
                getString(R.string.previous));
        fragment.setArguments(args);
        switchToFragment(fragment);
    }

    private void switchToFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
