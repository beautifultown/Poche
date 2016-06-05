package kaist.cs550_2016.poche;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.WindowManager;

import java.io.IOException;

public class MediaPlayerService extends Service {

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = true;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (ConfigHelper.getInstance().isWakeLock()) {
            mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MediaPlayerServiceBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) mediaPlayer.release();
    }

    /**
     * Wraps the default MediaPlayer class as a Service
     */
    public class MediaPlayerServiceBinder extends Binder {

        public void initialize(MediaPlayer.OnCompletionListener listener) {
            mediaPlayer.setOnCompletionListener(listener);
        }

        public void setTrack(Uri trackUri) {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(MediaPlayerService.this, trackUri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (isPlaying) {
                mediaPlayer.start();
            }
        }

        public void pauseTrack() {
            if (mediaPlayer != null) {
                if (isPlaying) {
                    mediaPlayer.pause();
                }
                else {
                    mediaPlayer.start();
                }
                isPlaying = !isPlaying;
            }
        }

        public int getCurrentPosition() {
            if(mediaPlayer == null) return 0;
            return mediaPlayer.getCurrentPosition();
        }

        public boolean isPlaying() {
            if(mediaPlayer == null) return false;
            return mediaPlayer.isPlaying();
        }
    }
}
