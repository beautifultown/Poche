package kaist.cs550_2016.poche;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.widget.TextView;

import junit.framework.Assert;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements BSUI.BSUIEventListener, MediaPlayer.OnCompletionListener {

    private TextView titleTextView;
    private TextView artistTextView;
    private TextView durationTextView;
    private TextView positionTextView;
    private ImageView albumArtImageView;

    private Playlist playlist;
    private GestureDetector gestureDetector;
    private MediaPlayerService.MediaPlayerServiceBinder mediaPlayerServiceBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri playlistUri = getIntent().getData();
        Debug.log(this, "Playlist loaded: " + playlistUri.toString());

        try {
            Debug.stopwatchStart();
            playlist = Playlist.parse(this, playlistUri);
            Debug.toastStopwatch("Parse()");

        } catch (IOException e) {
            Toast.makeText(this, R.string.toast_fail_parseplaylist, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }

        bindService(new Intent(getBaseContext(), MediaPlayerService.class),
                connection, BIND_AUTO_CREATE);
        // Start media playback in onServiceConnected

        BSUI bsui = new BSUI();
        bsui.setBSUIEventListener(this);
        gestureDetector = new GestureDetector(this, bsui);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        if (ConfigHelper.getInstance().isWakeLock()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        refreshUIElements();
        positionTextView.setText("0:00");
    }

    private void refreshUIElements() {
        titleTextView = (TextView) findViewById(R.id.main_TextTitle);
        artistTextView = (TextView) findViewById(R.id.main_TextArtist);
        durationTextView = (TextView) findViewById(R.id.main_TextDuration);
        positionTextView = (TextView) findViewById(R.id.main_TextPosition);
        albumArtImageView = (ImageView) findViewById(R.id.main_ImageAlbumart);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaPlayerServiceBinder = ((MediaPlayerService.MediaPlayerServiceBinder) service);
            mediaPlayerServiceBinder.initialize(MainActivity.this);
            PlayTrack();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Nothing to do
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayerServiceBinder != null) {
            unbindService(connection);
            mediaPlayerServiceBinder = null;
        }
    }

    // TouchListener()
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onBSUIEvent(BSUI.BSUIEvent event) {
        Debug.log(this, "BSUI event: " + event);
        switch (event) {
            case SINGLE_TAP:
                pauseResume();
                break;
            case STROKE_UP:
                adjustVolume(AudioManager.ADJUST_RAISE);
                break;
            case STROKE_DOWN:
                adjustVolume(AudioManager.ADJUST_LOWER);
                break;
            case STROKE_LEFT:
                PrevTrack();
                break;
            case STROKE_RIGHT:
                NextTrack();
                break;
            case STROKE_DOUBLEUP:
                SetPlayMode(ConfigHelper.PlayOrder.ORDERED);
                break;
            case STROKE_DOUBLEDOWN:
                SetPlayMode(ConfigHelper.PlayOrder.SHUFFLE);
                break;
        }
    }

    public void PlayTrack() {
        if (mediaPlayerServiceBinder == null) return;

        Uri fileURI = playlist.GetCurrentTrack();
        mediaPlayerServiceBinder.setTrack(fileURI);
        updateMetadata(fileURI);
    }

    private void pauseResume() {
        if (mediaPlayerServiceBinder == null) return;

        mediaPlayerServiceBinder.pauseTrack();
    }

    private void NextTrack() {
        playlist.NextTrack();
        PlayTrack();
    }

    private void PrevTrack() {
        playlist.PrevTrack();
        PlayTrack();
    }

    private void adjustVolume(int volumeAdjustCommand) {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                volumeAdjustCommand, AudioManager.FLAG_SHOW_UI);
    }

    private void SetPlayMode(ConfigHelper.PlayOrder playOrder) {
        ConfigHelper.getInstance().setPlayOrder(playOrder);
    }

    // OnCompletionListener
    @Override
    public void onCompletion(MediaPlayer mp) {
        NextTrack();
    }

    private void updateMetadata(Uri uri) {
        String trackTitle, trackArtist;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, uri);
        refreshUIElements();
        try {
            trackTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } catch (Exception e) {
            trackTitle = uri.getPath();
        }
        try {
            trackArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        } catch (Exception e) {
            trackArtist = "No Data";
        }
        // Android API returns the track length in milliseconds as a String
        String trackLength = millisecondsToMinuetesAndSeconds(
                Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
        try {
            byte[] bytearr = retriever.getEmbeddedPicture();
            Bitmap albumArt = BitmapFactory.decodeByteArray(bytearr, 0, bytearr.length);
            albumArtImageView.setImageBitmap(albumArt);
        } catch (Exception e)
        {
            //TODO: get a placeholder instead of being a magenta square
            albumArtImageView.setImageResource(0);
            albumArtImageView.setBackgroundColor(Color.MAGENTA);
        }
        Debug.log("Title: ", trackTitle);
        Debug.log("Artist: ", trackArtist);
        Debug.log("Length: ", trackLength);
        titleTextView.setText(trackTitle);
        artistTextView.setText(trackArtist);
        durationTextView.setText(trackLength);
    }

    /**
     * Returns the input time as a String of minuetes : seconds
     * Rounds up
     * e.g. 219921 -> 3:40
     * @param ms in milliseconds
     * @return
     */
    private String millisecondsToMinuetesAndSeconds(int ms)
    {
        float seconds = ((float)(ms % 60000)) / 1000;
        int secs = (int) seconds;
        if (seconds > secs)
            secs++;
        int mins = ms / 60000;
        return "" + mins + ':' + secs;
    }
}
