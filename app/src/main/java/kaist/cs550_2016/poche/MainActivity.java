package kaist.cs550_2016.poche;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements BSUI.BSUIEventListener, MediaPlayer.OnCompletionListener {

    private Playlist playlist;
    private GestureDetector gestureDetector;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri playlistUri = getIntent().getData();
        Toast.makeText(this, "Loaded: " + playlistUri.toString(), Toast.LENGTH_LONG).show();

        try {
            playlist = Playlist.parse(this, playlistUri);
            PlayTrack();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BSUI bsui = new BSUI();
        bsui.setBSUIEventListener(this);
        gestureDetector = new GestureDetector(this, bsui);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
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
        Toast.makeText(this, "BSUI event: " + event, Toast.LENGTH_SHORT).show();
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
        Uri currentTrack = playlist.GetCurrentTrack();
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, currentTrack);
            mediaPlayer.setOnCompletionListener(this);
        }
        else {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(this, currentTrack);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mediaPlayer.start();
    }

    private void pauseResume() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            else {
                mediaPlayer.start();
            }
        }
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        NextTrack();
    }
}
