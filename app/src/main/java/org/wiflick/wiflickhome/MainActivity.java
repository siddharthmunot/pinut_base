package org.wiflick.wiflickhome;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import org.wiflick.wiflickhome.kore.host.HostManager;
import org.wiflick.wiflickhome.kore.ui.BaseActivity;
import org.wiflick.wiflickhome.kore.ui.NavigationDrawerFragment;
import org.wiflick.wiflickhome.kore.ui.hosts.AddHostActivity;
import org.wiflick.wiflickhome.moviesView;
import android.widget.TextView;
//import android.R;


public class MainActivity extends BaseActivity {
    // public static final String MyPREFERENCES = "MyPrefs" ;
    //public static SharedPreferences pref;
    //private View SeatDialogView;
    private String TAG = "wiflick";
    //private SharedPreferences.Editor editor;
    private HostManager hostManager = null;
    private NavigationDrawerFragment navigationDrawerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the drawer.
        navigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        hostManager = HostManager.getInstance(this);
        // Check if we have any hosts setup
        if (hostManager.getHostInfo() == null) {
            final Intent intent = new Intent(this, AddHostActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        GridView grid;
        final String[] titles = {
                "Movies",
                "Music",
                "TV Shows",
        } ;
        int[] imageId = {
                //R.drawable.ic_movies,
                //R.drawable.ic_music_new ,
                //R.drawable.ic_tvshows,
        };

        MainViewAdapter adapter = new MainViewAdapter(MainActivity.this, titles, imageId);
        grid=(GridView)findViewById(R.id.grid);
        grid.setAdapter(adapter);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(MainActivity.this, "You Clicked at " +titles[+ position], Toast.LENGTH_SHORT).show();
                Intent intent;
                Log.i(TAG, "position#" + position);
                switch (position) {
                    case 0:
                        intent = new Intent(MainActivity.this, org.wiflick.wiflickhome.kore.ui.MoviesActivity.class);
                        intent.putExtra("flag", 0);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, org.wiflick.wiflickhome.kore.ui.MusicActivity.class);
                        intent.putExtra("flag", 1);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case 2:
                        Log.i(TAG, "coming inside tv shows");
                        intent = new Intent(MainActivity.this, org.wiflick.wiflickhome.kore.ui.TVShowsActivity.class);
                        intent.putExtra("flag", 2);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;

                }


            }
        });

        grid.setDrawSelectorOnTop(true);
        setupActionBar();

        /*
        ImageView movies = (ImageView)findViewById(R.id.movies);
        ImageView music = (ImageView)findViewById(R.id.music);
        ImageView food = (ImageView)findViewById(R.id.food);
        movies.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentMovies = new Intent(MainActivity.this, org.wiflick.wiflickhome.kore.ui.MoviesActivity.class);
                intentMovies.putExtra("flag", 0);
                intentMovies.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentMovies.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentMovies);
                //finish();
            }
        });
        music.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentMusic = new Intent(MainActivity.this, org.wiflick.wiflickhome.kore.ui.MusicActivity.class);
                intentMusic.putExtra("flag", 1);
                intentMusic.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentMusic.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentMusic);
                //finish();
            }
        });
        food.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentFood = new Intent(MainActivity.this, foodView.class);
                intentFood.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentFood.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentFood);
                //finish();
            }
        });
        */
    }

   /* private void confirmSeatNumber() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //final String[] seat = new String[1];

        SeatDialogView = inflater
                .inflate(R.layout.layout_dialog_confirm_seat_number, null);
        final EditText seatNumberText = (EditText) SeatDialogView.findViewById(R.id.seatnumber);

        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Confirm Number");
        alertDialog.setView(SeatDialogView);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Verify", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Log.i(TAG, "seat#" + seatNu);
                editor.putString("seatNumber", seatNumberText.getText().toString());
                editor.apply();
                //seatNumberText.getText().toString();
                }
        });
        alertDialog.show();
        //return seat[0];
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.default_toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationDrawerFragment.setDrawerIndicatorEnabled(true);
        actionBar.setTitle(R.string.app_name);

    }
}
