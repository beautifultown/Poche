package kaist.cs550_2016.poche;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements BSUI.BSUIEventListener {

    private Playlist playlist;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri playlistUri = getIntent().getData();
        Toast.makeText(this, "Loaded: " + playlistUri.toString(), Toast.LENGTH_LONG).show();

        try {
            playlist = Playlist.parse(this, getIntent().getData());
            playlist.GetCurrentTrack();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BSUI bsui = new BSUI();
        bsui.setBSUIEventListener(this);
        gestureDetector = new GestureDetector(this, bsui);
    }

    // TouchListener()
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onBSUIEvent(BSUI.BSUIEvent event) {
        Toast.makeText(this, "BSUI event: " + event, Toast.LENGTH_LONG).show();
        switch (event) {
            case SINGLE_TAP:
                // TODO Pause/Play
                break;
            case STROKE_UP:
                // TODO Volume up
                break;
            case STROKE_DOWN:
                // TODO Volume down
                break;
            case STROKE_LEFT:
                playlist.PrevTrack();
                playlist.GetCurrentTrack();
                break;
            case STROKE_RIGHT:
                playlist.NextTrack();
                playlist.GetCurrentTrack();
                break;
            case STROKE_DOUBLEUP:
                ConfigHelper.getInstance().setPlayOrder(ConfigHelper.PlayOrder.ORDERED);
                break;
            case STROKE_DOUBLEDOWN:
                ConfigHelper.getInstance().setPlayOrder(ConfigHelper.PlayOrder.SHUFFLE);
                break;
        }
    }
}
