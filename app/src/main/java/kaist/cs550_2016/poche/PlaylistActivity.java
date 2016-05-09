package kaist.cs550_2016.poche;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<File>> {

    private ListView playlistListView;
    private ArrayAdapter<?> listViewAdapter;
    private ProgressDialog scanningProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        playlistListView = (ListView) findViewById(R.id.playlist_List);
        playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayTrack(view.getTag().toString());
            }
        });

        GetPlayList();
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


    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle args) {
        return new PlaylistScanAsyncTask(getApplicationContext());
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
        listViewAdapter = new PlaylistAdapter(this, R.layout.activity_playlist_list_item, data);
        playlistListView.setAdapter(listViewAdapter);
        scanningProgressDialog.dismiss();
    }

    @Override
    public void onLoaderReset(Loader<List<File>> loader) {

    }

    private class PlaylistAdapter extends ArrayAdapter<File> {

        public PlaylistAdapter(Context context, int resource, List<File> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.activity_playlist_list_item, null);
            }

            File playlistFile = getItem(position);
            String filename = playlistFile.getParentFile().getName() +
                    "/" + playlistFile.getName();
            TextView playlistTextView = ViewHolder.get(convertView, R.id.playlist_ListItem);

            playlistTextView.setText(filename);
            playlistTextView.setTag(playlistFile.getPath());

            return convertView;
        }
    }

    private void GetPlayList() {
        getLoaderManager().initLoader(0, null, this);
        scanningProgressDialog =
                ProgressDialog.show(this, "Please wait", "Scanning playlist files...");
    }

    private void PlayTrack(String path) {
        Intent intent = new Intent(PlaylistActivity.this, MainActivity.class);
        intent.setData(Uri.parse("file://" + path));
        startActivity(intent);
    }
}
