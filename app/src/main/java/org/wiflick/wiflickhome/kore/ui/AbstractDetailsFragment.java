/*
 * Copyright 2015 Martijn Brekhof. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wiflick.wiflickhome.kore.ui;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.wiflick.wiflickhome.R;
import org.wiflick.wiflickhome.kore.host.HostInfo;
import org.wiflick.wiflickhome.kore.host.HostManager;
import org.wiflick.wiflickhome.kore.jsonrpc.ApiException;
import org.wiflick.wiflickhome.kore.jsonrpc.event.MediaSyncEvent;
import org.wiflick.wiflickhome.kore.service.LibrarySyncService;
import org.wiflick.wiflickhome.kore.service.SyncUtils;
import org.wiflick.wiflickhome.kore.utils.LogUtils;
import org.wiflick.wiflickhome.kore.utils.UIUtils;

import de.greenrobot.event.EventBus;

abstract public class AbstractDetailsFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        SyncUtils.OnServiceListener {
    private static final String TAG = LogUtils.makeLogTag(AbstractDetailsFragment.class);

    private HostManager hostManager;
    private HostInfo hostInfo;
    private EventBus bus;
    private String syncType;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ServiceConnection serviceConnection;

    abstract protected View createView(LayoutInflater inflater, ViewGroup container);

    /**
     * Should return {@link org.wiflick.wiflickhome.kore.service.LibrarySyncService} SyncType that
     * this fragment initiates
     * @return {@link org.wiflick.wiflickhome.kore.service.LibrarySyncService} SyncType
     */
    abstract protected String getSyncType();

    /**
     * Should return the {@link org.wiflick.wiflickhome.kore.service.LibrarySyncService} syncID if this fragment
     * synchronizes a single item. The itemId that should be synced must returned by {@link #getSyncItemID()}
     * @return {@link org.wiflick.wiflickhome.kore.service.LibrarySyncService} SyncID
     */
    abstract protected String getSyncID();

    /**
     * Should return the item ID for SyncID returned by {@link #getSyncID()}
     * @return -1 if not used.
     */
    abstract protected int getSyncItemID();

    /**
     * Should return the SwipeRefreshLayout if the fragment's view uses one.
     * Used to notify the user if a sync for synctype returned by {@link #getSyncType()}
     * is currently in progress
     * @return
     */
    abstract protected SwipeRefreshLayout getSwipeRefreshLayout();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus = EventBus.getDefault();
        hostManager = HostManager.getInstance(getActivity());
        hostInfo = hostManager.getHostInfo();
        syncType = getSyncType();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            // We're not being shown or there's nothing to show
            return null;
        }

        View view = createView(inflater, container);
        if( view != null ) {
            swipeRefreshLayout = getSwipeRefreshLayout();
            if( swipeRefreshLayout != null ) {
                swipeRefreshLayout.setOnRefreshListener(this);
            }
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        serviceConnection = SyncUtils.connectToLibrarySyncService(getActivity(), this);
    }

    @Override
    public void onResume() {
        bus.register(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        SyncUtils.disconnectFromLibrarySyncService(getActivity(), serviceConnection);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_item, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                onRefresh();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void startSync(boolean silentRefresh) {
        if (getHostInfo() != null) {
            if( ( swipeRefreshLayout != null ) && ( ! silentRefresh ) ){
                UIUtils.showRefreshAnimation(swipeRefreshLayout);
            }
            // Start the syncing process
            Intent syncIntent = new Intent(this.getActivity(), LibrarySyncService.class);

            if(syncType != null) {
                syncIntent.putExtra(syncType, true);
            }

            String syncID = getSyncID();
            int itemId = getSyncItemID();
            if( ( syncID != null ) && ( itemId != -1 ) ) {
                syncIntent.putExtra(syncID, itemId);
            }

            Bundle syncExtras = new Bundle();
            syncExtras.putBoolean(LibrarySyncService.SILENT_SYNC, silentRefresh);
            syncIntent.putExtra(LibrarySyncService.SYNC_EXTRAS, syncExtras);

            getActivity().startService(syncIntent);
        } else {
            if( swipeRefreshLayout != null ) {
                swipeRefreshLayout.setRefreshing(false);
            }
            Toast.makeText(getActivity(), R.string.no_xbmc_configured, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Swipe refresh layout callback
     */
    /** {@inheritDoc} */
    @Override
    public void onRefresh () {
        startSync(false);
    }

    /**
     * Event bus post. Called when the syncing process ended
     *
     * @param event Refreshes data
     */
    public void onEventMainThread(MediaSyncEvent event) {
        if ((syncType == null) || (! event.syncType.equals(syncType)))
            return;

        boolean silentSync = false;
        if (event.syncExtras != null) {
            silentSync = event.syncExtras.getBoolean(LibrarySyncService.SILENT_SYNC, false);
        }


        if( swipeRefreshLayout != null ) {
            swipeRefreshLayout.setRefreshing(false);
        }
        onSyncProcessEnded(event);
        if (event.status == MediaSyncEvent.STATUS_SUCCESS) {
            if (!silentSync) {
                Toast.makeText(getActivity(),
                        R.string.sync_successful, Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (!silentSync) {
            String msg = (event.errorCode == ApiException.API_ERROR) ?
                    String.format(getString(R.string.error_while_syncing), event.errorMessage) :
                    getString(R.string.unable_to_connect_to_xbmc);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onServiceConnected(LibrarySyncService librarySyncService) {
        if (syncType == null)
            return;

        if (SyncUtils.isLibrarySyncing(
                librarySyncService,
                HostManager.getInstance(getActivity()).getHostInfo(),
                syncType)) {
            if (swipeRefreshLayout != null) {
                UIUtils.showRefreshAnimation(swipeRefreshLayout);
            }
            return;
        }
    }

    /**
     * Called when sync process for type set through {@link #getSyncType()} ends
     * @param event
     */
    abstract protected void onSyncProcessEnded(MediaSyncEvent event);

    protected HostManager getHostManager() {
        return hostManager;
    }

    protected HostInfo getHostInfo() {
        return hostInfo;
    }
}