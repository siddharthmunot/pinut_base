package org.wiflick.wiflickhome;

import android.support.v4.app.Fragment;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import org.wiflick.wiflickhome.R;
import org.wiflick.wiflickhome.kore.host.HostInfo;
import org.wiflick.wiflickhome.kore.host.HostManager;
import org.wiflick.wiflickhome.kore.jsonrpc.ApiException;
import org.wiflick.wiflickhome.kore.jsonrpc.event.MediaSyncEvent;
import org.wiflick.wiflickhome.kore.service.LibrarySyncService;
import org.wiflick.wiflickhome.kore.service.SyncUtils;
import org.wiflick.wiflickhome.kore.ui.AlbumListFragment;
import org.wiflick.wiflickhome.kore.ui.ArtistListFragment;
import org.wiflick.wiflickhome.kore.ui.AudioGenresListFragment;
import org.wiflick.wiflickhome.kore.ui.MovieListFragment;
import org.wiflick.wiflickhome.kore.ui.MoviesActivity;
import org.wiflick.wiflickhome.kore.ui.MusicActivity;
import org.wiflick.wiflickhome.kore.ui.MusicVideoListFragment;
import org.wiflick.wiflickhome.kore.ui.TVShowsActivity;
import org.wiflick.wiflickhome.kore.utils.LogUtils;
import org.wiflick.wiflickhome.kore.utils.TabsAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;


/**
 * Created by mdimran on 12/26/2015.
 */
public class MainActivityFragments extends Fragment{
    //private TabsAdapter tabsAdapter;

    //@InjectView(R.id.pager_tab_strip) PagerSlidingTabStrip pagerTabStrip;
    //@InjectView(R.id.pager) ViewPager viewPager;
    @InjectView(R.id.entertainment_button_list)LinearLayout viewEntertainmentButtonList;
    @InjectView(R.id.imageButtonMovies)Button btnMovies;
    @InjectView(R.id.imageButtonMusic)Button btnMusic;
    @InjectView(R.id.imageButtonTVShows)Button btnTVShows;
    //@InjectView(R.id.imageEntertainmentBackground)ImageView backgroundImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.main_fragment, container, false);
        ButterKnife.inject(this, root);
        //final View viewEntertainmentButtonList=this.getView().findViewById(R.id.entertainment_button_list);
/*
        tabsAdapter = new TabsAdapter(getActivity(), getChildFragmentManager())
              .addTab(MovieFragment.class, getArguments(), R.string.tabEntertainment, 1)
            .addTab(FoodFragment.class, getArguments(), R.string.tabFood , 2)
          .addTab(FeedbackFragment.class, getArguments(), R.string.tabFeedback, 3);

        viewPager.setAdapter(tabsAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position != 0) {
                    viewEntertainmentButtonList.setVisibility(View.GONE);
                }
                else
                {
                    viewEntertainmentButtonList.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

*/
        //getFragmentManager().beginTransaction().replace(this.getId(), new MovieFragment()).commit();
        //getFragmentManager().executePendingTransactions();

        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, new MovieFragment()).commit();
        getChildFragmentManager().executePendingTransactions();


        btnMovies.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MoviesActivity.class);
                startActivity(intent);

            }
        });

        btnMusic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MusicActivity.class);
                startActivity(intent);

            }
        });

        btnTVShows.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),TVShowsActivity.class);
                startActivity(intent);

            }
        });

        //pagerTabStrip.setViewPager(viewPager);
        return root;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //backgroundImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart()
    {
        super.onStart ();
        //backgroundImageView.setVisibility(View.VISIBLE);
    }


}

