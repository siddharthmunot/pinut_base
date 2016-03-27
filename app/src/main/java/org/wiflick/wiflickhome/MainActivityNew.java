package org.wiflick.wiflickhome;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.transition.Visibility;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.wiflick.wiflickhome.R;
import org.wiflick.wiflickhome.kore.ui.BaseActivity;
import org.wiflick.wiflickhome.kore.ui.MovieDetailsFragment;
import org.wiflick.wiflickhome.kore.ui.MovieListFragment;
import org.wiflick.wiflickhome.kore.ui.MusicListFragment;
import org.wiflick.wiflickhome.kore.ui.NavigationDrawerFragment;
import org.wiflick.wiflickhome.kore.utils.LogUtils;
import org.wiflick.wiflickhome.kore.utils.Utils;

public class MainActivityNew extends BaseActivity  implements MovieFragment.OnMovieSelectedListener,FragmentManager.OnBackStackChangedListener {

    private int selectedMovieId = -1;
    private String selectedMovieTitle;
    private NavigationDrawerFragment navigationDrawerFragment;
    private ImageView backgroundImageView;

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        backgroundImageView = (ImageView) findViewById(R.id.imageEntertainmentBackground);
        // Set up the drawer.
        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        MainActivityFragments mainActivityFragment = new MainActivityFragments();
        //MovieFragment mainActivityFragment = new MovieFragment();

        // Setup animations
        if (Utils.isLollipopOrLater()) {
            mainActivityFragment.setExitTransition(null);
            mainActivityFragment.setReenterTransition(TransitionInflater
                    .from(this)
                    .inflateTransition(android.R.transition.fade));
        }
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, mainActivityFragment)
                .commit();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        setupActionBar();
        //backgroundImageView.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_new, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        backgroundImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        backgroundImageView.setVisibility(View.VISIBLE);
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

    @TargetApi(21)
    public void onMovieSelected(int movieId, String movieTitle) {
        selectedMovieId = movieId;
        selectedMovieTitle = movieTitle;

        backgroundImageView.setVisibility(View.GONE);
        MovieDetailsFragment movieDetailsFragment = MovieDetailsFragment.newInstance(movieId);
        FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();

        // Set up transitions
        if (Utils.isLollipopOrLater()) {
            movieDetailsFragment.setEnterTransition(TransitionInflater
                    .from(this)
                    .inflateTransition(R.transition.media_details));
            movieDetailsFragment.setReturnTransition(null);
        } else {
            fragTrans.setCustomAnimations(R.anim.fragment_details_enter, 0,
                    R.anim.fragment_list_popenter, 0);
        }

        fragTrans.replace(R.id.fragment_container, movieDetailsFragment)
                .addToBackStack(null)
                .commit();
        //setupActionBar(selectedMovieTitle);
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.default_toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationDrawerFragment.setDrawerIndicatorEnabled(true);
        actionBar.setTitle(R.string.app_name);

    }

    @Override
    public void onBackStackChanged() {
        Toast.makeText(this, "ON Cback stack change", Toast.LENGTH_LONG).show();
    }


}
