package kaist.cs550_2016.poche;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Leegeun Ha on 2016-05-09.
 */

public class BSUI extends GestureDetector.SimpleOnGestureListener {

    private static final long DELAY_BETWEEN_DOUBLE_STROKE_MS = 480;
    private static final double STROKE_DEADZONE_ANGLE_DEGREE = 10;
    private static final double STROKE_MINIMUM_DISTANCE_PX = 200;

    private BSUIEvent storedEvent, previousEvent;
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
        float distanceX = Math.abs(e1.getX() - e2.getX());
        float distanceY = Math.abs(e1.getY() - e2.getY());

        // ignore short drags
        float distanceXY = distanceX + distanceY;
        Debug.log("Manhattan Dist: " + distanceXY);
        if (distanceXY < STROKE_MINIMUM_DISTANCE_PX) return true;

        // ignore dead zone 45 deg +- STROKE_DEADZONE_ANGLE_DEGREE
        double angle = Math.toDegrees(Math.atan2(distanceY, distanceX));
        double deadZoneProximity = angle - 45.0;
        if (deadZoneProximity < STROKE_DEADZONE_ANGLE_DEGREE
                && deadZoneProximity > -STROKE_DEADZONE_ANGLE_DEGREE) {
            return true;
        }

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
        if (storedEvent == null) Debug.stopwatchStart();

        BSUIEvent adjustedEvent = event;
        ConfigHelper.StrokeOrientation orientation =
                ConfigHelper.getInstance().getStrokeOrientation();

        adjustedEvent = applyConfiguration(event, adjustedEvent, orientation);
        adjustedEvent = handleDoubleStroke(event, adjustedEvent);

        if (adjustedEvent != null) {
            previousEvent = adjustedEvent;
            bsuiEventListener.onBSUIEvent(adjustedEvent);
            Debug.toastStopwatch("ParseTouchEvent()");
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
                if (storedEvent == event) {
                    adjustedEvent = BSUIEvent.getDoubledStroke(event);
                    storedEvent = null;
                } else {
                    storedEvent = adjustedEvent;
                    adjustedEvent = null;
                }
                break;
            default:
                storedEvent = null;
                break;
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (storedEvent == null) return;
                switch (storedEvent) {
                    case STROKE_UP:
                    case STROKE_DOWN:
                        bsuiEventListener.onBSUIEvent(storedEvent);
                        Debug.toastStopwatch("ParseTouchEvent()");
                        break;
                }
                storedEvent = null;
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

    public BSUIEvent getPreviousEvent() {
        return previousEvent;
    }
}
