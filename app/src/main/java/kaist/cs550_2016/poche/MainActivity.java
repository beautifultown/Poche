package kaist.cs550_2016.poche;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.widget.Toast;

import junit.framework.Assert;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements BSUI.BSUIEventListener, MediaPlayer.OnCompletionListener {

    private TextView titleTextView;
    private TextView artistTextView;
    private TextView durationTextView;
    private TextView positionTextView;
    private TextView positionSlashTextView;
    private ImageView albumArtImageView;
    private ImageView nextAlbumArtImageView;
    private ImageView seekBarImageView;
    private ImageView playMode;
    private RelativeLayout controlLayout;

    private Playlist playlist;
    private GestureDetector gestureDetector;
    private MediaPlayerService.MediaPlayerServiceBinder mediaPlayerServiceBinder;
    private AsyncTask tick, albumArtTransition;
    private Bitmap nextAlbumArt;

    private int defaultAlbumArtAvarageColor;

    private boolean isFirstTrack;

    private BSUI bsui;

    private boolean directionLeft;
    private int uiUpdateFrameRate = 1000 / 30;
    private int albumArtTransitionFrameRate = 1000 / 60;

    /**
     * The total length of the track in ms
     */
    private int trackDuration;
    private int screenWidth, screenHeight;
    //    private float pxPerDip;
    private float pxPerWidthPercentage, pxPerHeightPercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_AppBarOverlay);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri playlistUri = getIntent().getData();
        Debug.log(this, "Playlist loaded: " + playlistUri.toString());

        try {
            Debug.stopwatchStart();
            playlist = Playlist.parse(this, playlistUri);
            Debug.toastStopwatch("Parse()");
        } catch (Exception e) {
            Toast.makeText(this,
                    R.string.toast_fail_parseplaylist, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }

        isFirstTrack = true;
        bindService(new Intent(getBaseContext(), MediaPlayerService.class),
                connection, BIND_AUTO_CREATE);
        // Start media playback in onServiceConnected

        bsui = new BSUI();
        bsui.setBSUIEventListener(this);
        gestureDetector = new GestureDetector(this, bsui);
        View bsuiRegion = findViewById(R.id.main_BSUIRegion);
        bsuiRegion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Debug.log("MotionEvent: " + event);
                Debug.stopwatchStart();
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        if (ConfigHelper.getInstance().isWakeLock()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
//        pxPerDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        pxPerHeightPercentage = ((float) screenHeight) / 100;
        pxPerWidthPercentage = ((float) screenWidth) / 100;

        // Android does not GC on strongly referenced variables!
        titleTextView = (TextView) findViewById(R.id.main_TextTitle);
        artistTextView = (TextView) findViewById(R.id.main_TextArtist);
        durationTextView = (TextView) findViewById(R.id.main_TextDuration);
        positionTextView = (TextView) findViewById(R.id.main_TextPosition);
        positionSlashTextView = (TextView) findViewById(R.id.main_TextPositionSlash);
        albumArtImageView = (ImageView) findViewById(R.id.main_ImageAlbumArt);
        nextAlbumArtImageView = (ImageView) findViewById(R.id.main_NextImageAlbumArt);
        seekBarImageView = (ImageView) findViewById(R.id.main_SeekBar);
        playMode = (ImageView) findViewById(R.id.main_Mode);
        controlLayout = (RelativeLayout) findViewById(R.id.main_Control_Layout);

        positionTextView.setText("0:00");
        seekBarImageView.setX(-100 * pxPerWidthPercentage);
        nextAlbumArtImageView.setX(100 * pxPerWidthPercentage);

        defaultAlbumArtAvarageColor =
                ContextCompat.getColor(this, R.color.colorDefaultAlbumArtAverage);

        if(ConfigHelper.getInstance().getPlayOrder() == ConfigHelper.PlayOrder.SHUFFLE) {
            playMode.setImageResource(R.drawable.shuffle);
        }

        tick = new Tick().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uiUpdateFrameRate);
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
        if (tick != null) tick.cancel(true);
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
        }
    }

    public void PlayTrack() {
        if (mediaPlayerServiceBinder == null) return;

        Uri uri = playlist.GetCurrentTrack();
        mediaPlayerServiceBinder.setTrack(uri);
        updateMetadata(uri);
        directionLeft = false;
        if(!mediaPlayerServiceBinder.isPlaying())
            mediaPlayerServiceBinder.pauseTrack();
    }

    private void pauseResume() {
        if (mediaPlayerServiceBinder == null) return;

        mediaPlayerServiceBinder.pauseTrack();
    }

    private void NextTrack() {
        directionLeft = false;
        playlist.NextTrack();
        PlayTrack();
    }

    private void PrevTrack() {
        directionLeft = true;
        playlist.PrevTrack();
        PlayTrack();
    }

    private void adjustVolume(int volumeAdjustCommand) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, volumeAdjustCommand, 0);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Toast.makeText(this, "Volume: " + volume, Toast.LENGTH_SHORT).show();
    }

    private void SetPlayMode(ConfigHelper.PlayOrder playOrder) {
        ConfigHelper.getInstance().setPlayOrder(playOrder);
    }

    // OnCompletionListener
    @Override
    public void onCompletion(MediaPlayer mp) {
        NextTrack();
    }

    /**
     * Retrieves metadata from the current track and displays them on the UI
     * Should be used only once per track
     *
     * @param uri
     */
    private void updateMetadata(Uri uri) {
        // Update text
        String trackTitle, trackArtist;
        Bitmap albumArt;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, uri);
        try {
            trackTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } catch (Exception e) {
            trackTitle = uri.getPath();
        }
        try {
            trackArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        } catch (Exception e) {
            trackArtist = getString(R.string.main_noartist);
        }

        // Android API returns the track length in milliseconds as a String
        trackDuration = Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        String trackLength = millisecondsToMinutesAndSeconds(trackDuration);

        Debug.log("Title: ", trackTitle);
        Debug.log("Artist: ", trackArtist);
        Debug.log("Length: ", trackLength);
        titleTextView.setText(trackTitle);
        artistTextView.setText(trackArtist);
        durationTextView.setText(trackLength);

        try {
            byte[] bytearr = retriever.getEmbeddedPicture();
            albumArt = BitmapFactory.decodeByteArray(bytearr, 0, bytearr.length);
        } catch (Exception e) {
            albumArt = null;
            //BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.random_album_art);
        }

        if (isFirstTrack) {
            if (albumArt != null) {
                albumArtImageView.setImageBitmap(albumArt);
            }
            else {
                albumArtImageView.setImageResource(R.drawable.random_album_art);
            }
            isFirstTrack = false;
        }
        else {
            if (albumArtTransition != null) {
                albumArtTransition.cancel(true);
                if (nextAlbumArt != null) {
                    albumArtImageView.setImageBitmap(nextAlbumArt);
                }
                else {
                    albumArtImageView.setImageResource(R.drawable.random_album_art);
                }
            }

            if (albumArt != null) {
                nextAlbumArtImageView.setImageBitmap(albumArt);
            }
            else {
                nextAlbumArtImageView.setImageResource(R.drawable.random_album_art);
            }
            int direction = directionLeft ? 0 : 1;
            albumArtTransition = new AlbumArtTransition().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR, 500, albumArtTransitionFrameRate, direction);
        }

        nextAlbumArt = albumArt;
        setColorForAlbumArt(albumArt);
    }

    private void setColorForAlbumArt(@Nullable Bitmap albumArt) {
        int color1, color2;
        int averageColor = albumArt != null ? getAverageColor(albumArt) : defaultAlbumArtAvarageColor;
        View rootLayout = findViewById(R.id.main_Root);

        // update colors
        rootLayout.setBackgroundColor(0xFF000000 + averageColor);

        int r = Color.red(averageColor);
        int g = Color.green(averageColor);
        int b = Color.blue(averageColor);
        int max = Math.max(r, Math.max(g, b));
        final float darkerRatio = 0.4f;
        final float lighterRatio = 0.6f;
        int darkerAvg = 0xFF000000 +
                getColorInt((int) (r * darkerRatio), (int) (g * darkerRatio), (int) (b * darkerRatio));
        int lightMod = (int)((255 - max) * lighterRatio);
        int lighterAvg = averageColor + lightMod * 0x10000 + lightMod * 0x100 + lightMod + 0xFF000000;
        double brightnessDelta = (r + g + b) * 0.6;

        // if too little difference, boost text color so it's brighter than the background
        // arbitrary threshold that felt good enough
        if (brightnessDelta < 300) {
            color1 = lighterAvg;
            float color2Ratio = 1 / darkerRatio;
            color2 = 0xFF000000 +
                    getColorInt((int) (r * color2Ratio), (int) (g * color2Ratio), (int) (b * color2Ratio));
            // and if the average color is not dark
        } else {
            color1 = darkerAvg;
            float color2Ratio = darkerRatio * 1.7f;
            color2 = 0xFF000000 +
                    getColorInt((int) (r * color2Ratio), (int) (g * color2Ratio), (int) (b * color2Ratio));
        }

        titleTextView.setTextColor(color1);
        artistTextView.setTextColor(color1);
        durationTextView.setTextColor(color1);
        positionSlashTextView.setTextColor(color1);
        positionTextView.setTextColor(color1);
        seekBarImageView.setBackgroundColor(color1);

        int color2ReducedOpacacity = color2 - 0xAA000000;
        for (int i = 0; i < controlLayout.getChildCount(); i++) {
            ImageView iv = (ImageView) controlLayout.getChildAt(i);
            if (iv.getTag() == null) {
                // not a bracket
                iv.setColorFilter(color2, PorterDuff.Mode.SRC_IN);
            } else {
                // bracket
                iv.setColorFilter(color2ReducedOpacacity, PorterDuff.Mode.SRC_IN);
            }
        }
        int somethingInTheMiddle = color2 - 0x77000000;
        playMode.setColorFilter(somethingInTheMiddle, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Updates the UI animation during track transition
     *
     * @param progress  assumed to be [0, 1]
     * @param direction false is left to right, true is right to left
     */
    private void updateAlbumArt(float progress, boolean direction) {
        // if next track
        // i.e. next album art coming in from the right
        if (direction) {
            nextAlbumArtImageView.setX(100 * (1 - progress) * pxPerWidthPercentage);
        } else {
            nextAlbumArtImageView.setX(-100 * (1 - progress) * pxPerWidthPercentage);
        }
        if (progress >= 1) {
            if (nextAlbumArt != null) {
                albumArtImageView.setImageBitmap(nextAlbumArt);
            }
            else {
                albumArtImageView.setImageResource(R.drawable.random_album_art);
            }
            nextAlbumArtImageView.setX(100 * pxPerWidthPercentage);
        }
    }

    /**
     * Updates the seek bar and the current time TextView
     */
    private void updateTrackTime() {
        if (mediaPlayerServiceBinder == null) return;

        int seconds = mediaPlayerServiceBinder.getCurrentPosition();
        positionTextView.setText(millisecondsToMinutesAndSeconds(seconds));
        float percentCurrentPosition = ((float) seconds) / trackDuration * 100;
        seekBarImageView.setX((percentCurrentPosition - 100) * pxPerWidthPercentage);
    }

    /**
     * Returns the input time as a String of minutes : seconds
     * Rounds up
     * e.g. 219921 -> 3:40
     *
     * @param ms Time in milliseconds
     * @return
     */
    private String millisecondsToMinutesAndSeconds(int ms) {
        float seconds = ((float) (ms % 60000)) / 1000;
        int secs = (int) seconds;
        if (seconds > secs)
            secs++;
        String suffix = secs < 10 ? ":0" + secs : ":" + secs;
        int mins = ms / 60000;
        return "" + mins + suffix;
    }

    /**
     * Uses getColorInt() for finalizing the return value
     * If too computationally expensive, just increase the stride
     *
     * @param bmp
     * @return
     */
    private int getAverageColor(Bitmap bmp) {
        int totalCount = bmp.getWidth() * bmp.getHeight();
        int[] pixels = new int[totalCount];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        // int causes overflow
        long totalR = 0;
        long totalG = 0;
        long totalB = 0;
        for (int px : pixels) {
            int r = Color.red(px);
            int g = Color.green(px);
            int b = Color.blue(px);
            totalR += r * r;
            totalG += g * g;
            totalB += b * b;
        }
        totalR = (int) Math.sqrt(totalR / totalCount);
        totalG = (int) Math.sqrt(totalG / totalCount);
        totalB = (int) Math.sqrt(totalB / totalCount);
        return getColorInt((int) totalR, (int) totalG, (int) totalB);
    }



    /**
     * Returns an int that contains RGB information as 0xRRGGBB
     * Might need to add 0xFF000000 if using for ARGB
     * Clips input to [0, 255]
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    private int getColorInt(int r, int g, int b) {
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        return Color.rgb(r,g,b);
    }

    /**
     * Kind of like an update function
     * Except it does not take dt as argument
     */
    private class Tick extends AsyncTask<Integer, Integer, Integer> {
        /**
         * args[0] == tick interval in ms
         * e.g. if input is [100] the task is run at most 10Hz
         * Just like update(), there is no guarantee of maintaining 10Hz
         *
         * @param args
         * @return
         */
        protected Integer doInBackground(Integer... args) {
            while (true) {
                try {
                    Thread.sleep(args[0]);
                    publishProgress(0);
                } catch (java.lang.InterruptedException e) {
                    Assert.assertNotNull(null);
                }
            }
        }

        /**
         * Does not use any inputs
         *
         * @param args
         */
        protected void onProgressUpdate(Integer... args) {
            updateTrackTime();
        }
    }

    /**
     * Measures the time since an album art transition was called, and notifies the UI updater
     * responsible for the transition on how much time has passed.
     */
    private class AlbumArtTransition extends AsyncTask<Integer, Float, Integer> {
        /**
         * args[0] == total time in ms
         * args[1] == tick interval in ms
         * args[2] == direction. 0 is Prev, 1 is Next
         * Does not guarantee constant tick rate.
         *
         * @param args
         * @return
         */
        protected Integer doInBackground(Integer... args) {
            long timeBeforeSleep = System.currentTimeMillis();
            int timeElapsed = 0;
            int timeTarget = args[0];
            int timeTick = args[1];
            int direction = args[2];
            Assert.assertTrue(direction == 0 || direction == 1);
            while (timeElapsed < timeTarget) {
                try {
                    Thread.sleep(timeTick);
                    timeElapsed += System.currentTimeMillis() - timeBeforeSleep;
                    publishProgress(((float) timeElapsed) / timeTarget, (float) direction);
                } catch (java.lang.InterruptedException e) {
                    Assert.assertNotNull(null);
                }
            }
            publishProgress(1.0f, (float) direction);
            return 0;
        }

        /**
         * args[0] == time elapsed as a ratio of time elapsed vs total time
         * args[1] == direction. 0 is Left, 1 is Right.
         * No error checking
         *
         * @param args
         */
        protected void onProgressUpdate(Float... args) {
            updateAlbumArt(args[0], args[1] > 0);
        }
    }
}
