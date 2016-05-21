package kaist.cs550_2016.poche;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Leegeun Ha on 2016-05-09.
 */

public class BSUI extends GestureDetector.SimpleOnGestureListener {

    private static final long DELAY_BETWEEN_DOUBLE_STROKE_MS = 500;

    private BSUIEvent previousEvent;
    private BSUIEventListener bsuiEventListener;

    public void setBSUIEventListener(BSUIEventListener bsuiEventListener) {
        this.bsuiEventListener = bsuiEventListener;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        fireEvent(BSUIEvent.SINGLE_TAP);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float speedX = Math.abs(velocityX);
        float speedY = Math.abs(velocityY);

        if (speedX > speedY) {
            if (e1.getX() < e2.getX()) {
                fireEvent(BSUIEvent.STROKE_RIGHT);
            } else if (e1.getX() > e2.getX()) {
                fireEvent(BSUIEvent.STROKE_LEFT);
            }
        }

        if (speedX < speedY) {
            if (e1.getY() < e2.getY()) {
                fireEvent(BSUIEvent.STROKE_DOWN);
            } else if (e1.getY() > e2.getY()) {
                fireEvent(BSUIEvent.STROKE_UP);
            }
        }

        return true;
    }

    private void fireEvent(BSUIEvent event) {
        if (bsuiEventListener == null) return;

        BSUIEvent adjustedEvent = event;
        ConfigHelper.StrokeOrientation orientation =
                ConfigHelper.getInstance().getStrokeOrientation();

        adjustedEvent = applyConfiguration(event, adjustedEvent, orientation);
        adjustedEvent = handleDoubleStroke(event, adjustedEvent);

        if (adjustedEvent != null) {
            bsuiEventListener.onBSUIEvent(adjustedEvent);
        }
    }

    private BSUIEvent applyConfiguration(BSUIEvent event, BSUIEvent adjustedEvent,
                                         ConfigHelper.StrokeOrientation orientation) {
        if (orientation == ConfigHelper.StrokeOrientation.REVERSED) {
            switch (event) {
                case STROKE_LEFT:
                    adjustedEvent = BSUIEvent.STROKE_RIGHT;
                    break;
                case STROKE_RIGHT:
                    adjustedEvent = BSUIEvent.STROKE_LEFT;
                    break;
            }
        }
        return adjustedEvent;
    }

    @Nullable
    private BSUIEvent handleDoubleStroke(BSUIEvent event, BSUIEvent adjustedEvent) {
        switch (event) {
            case STROKE_UP:
            case STROKE_DOWN:
                if (previousEvent == event) {
                    adjustedEvent = BSUIEvent.getDoubledStroke(event);
                    previousEvent = null;
                } else {
                    previousEvent = adjustedEvent;
                    adjustedEvent = null;
                }
                break;
            default:
                previousEvent = null;
                break;
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (previousEvent == null) return;
                switch (previousEvent) {
                    case STROKE_UP:
                    case STROKE_DOWN:
                        bsuiEventListener.onBSUIEvent(previousEvent);
                        break;
                }
                previousEvent = null;
            }
        }, DELAY_BETWEEN_DOUBLE_STROKE_MS);
        return adjustedEvent;
    }

    public enum BSUIEvent {
        SINGLE_TAP,
        STROKE_UP,
        STROKE_DOWN,
        STROKE_LEFT,
        STROKE_RIGHT,
        STROKE_DOUBLEUP,
        STROKE_DOUBLEDOWN;

        private static BSUIEvent getDoubledStroke(BSUIEvent singleStroke) {
            switch (singleStroke) {
                case STROKE_UP:
                    return STROKE_DOUBLEUP;
                case STROKE_DOWN:
                    return STROKE_DOUBLEDOWN;
                case STROKE_LEFT:
                    break;
                case STROKE_RIGHT:
                    break;
            }
            return null;
        }
    }

    public interface BSUIEventListener {
        public void onBSUIEvent(BSUIEvent event);
    }
}
