package kaist.cs550_2016.poche;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PlaylistActivity extends AppCompatActivity {

    private ListView playlistListView;
    private ArrayAdapter<String> listViewAdapter;
    private String[] listViewValues = {"",""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        playlistListView = (ListView) findViewById(R.id.playlist_list);
        listViewAdapter = new ArrayAdapter<>(this, R.layout.activity_playlist_list_item, listViewValues);
        playlistListView.setAdapter(listViewAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("Playlist", "OnResume:");
        ConfigHelper configHelper = ConfigHelper.getInstance();
        listViewValues[0] = "Swipe mode: " + configHelper.getSwipeDirection();
        listViewValues[1] = "Wakelock enabled?: " + configHelper.isWakeLockEnabled();

        listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playlist, menu);
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
            startActivity(new Intent(this, ConfigActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
