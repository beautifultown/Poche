package kaist.cs550_2016.poche;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;

import junit.framework.Assert;

/**
 * Created by Leegeun Ha on 2016-05-09.
 */

public class BSUI extends GestureDetector.SimpleOnGestureListener {

    private static final double STROKE_DEADZONE_ANGLE_DEGREE = 10;
    private static final double STROKE_MINIMUM_DISTANCE_PX = 200;

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

        BSUIEvent adjustedEvent = event;
        ConfigHelper.StrokeOrientation orientation =
                ConfigHelper.getInstance().getStrokeOrientation();
        boolean reverse = ConfigHelper.getInstance().isStrokePull();

        adjustedEvent = applyConfiguration(event, adjustedEvent, orientation, reverse);

        if (adjustedEvent != null) {
            bsuiEventListener.onBSUIEvent(adjustedEvent);
            Debug.toastStopwatch("ParseTouchEvent()");
        }
    }

    private BSUIEvent applyConfiguration(BSUIEvent event, BSUIEvent adjustedEvent,
                                         ConfigHelper.StrokeOrientation orientation, boolean reverse) {
        if (orientation != ConfigHelper.StrokeOrientation.NORTH) {
            switch (orientation) {
                case EAST:
                    switch (event) {
                        case STROKE_UP:
                            adjustedEvent = BSUIEvent.STROKE_RIGHT;
                            break;
                        case STROKE_RIGHT:
                            adjustedEvent = BSUIEvent.STROKE_DOWN;
                            break;
                        case STROKE_DOWN:
                            adjustedEvent = BSUIEvent.STROKE_LEFT;
                            break;
                        case STROKE_LEFT:
                            adjustedEvent = BSUIEvent.STROKE_UP;
                            break;
                    }
                    break;
                case SOUTH:
                    switch (event) {
                        case STROKE_UP:
                            adjustedEvent = BSUIEvent.STROKE_DOWN;
                            break;
                        case STROKE_RIGHT:
                            adjustedEvent = BSUIEvent.STROKE_LEFT;
                            break;
                        case STROKE_DOWN:
                            adjustedEvent = BSUIEvent.STROKE_UP;
                            break;
                        case STROKE_LEFT:
                            adjustedEvent = BSUIEvent.STROKE_RIGHT;
                            break;
                    }
                    break;
                case WEST:
                    switch (event) {
                        case STROKE_UP:
                            adjustedEvent = BSUIEvent.STROKE_LEFT;
                            break;
                        case STROKE_RIGHT:
                            adjustedEvent = BSUIEvent.STROKE_UP;
                            break;
                        case STROKE_DOWN:
                            adjustedEvent = BSUIEvent.STROKE_RIGHT;
                            break;
                        case STROKE_LEFT:
                            adjustedEvent = BSUIEvent.STROKE_DOWN;
                            break;
                    }
                    break;
                default:
                    Assert.assertNotNull(null);
            }
        }
        if (reverse) {
            if (adjustedEvent == BSUIEvent.STROKE_LEFT)
                adjustedEvent = BSUIEvent.STROKE_RIGHT;
            else if (adjustedEvent == BSUIEvent.STROKE_RIGHT)
                adjustedEvent = BSUIEvent.STROKE_LEFT;
        }
        return adjustedEvent;
    }

    public enum BSUIEvent {
        SINGLE_TAP,
        STROKE_UP,
        STROKE_DOWN,
        STROKE_LEFT,
        STROKE_RIGHT
    }

    public interface BSUIEventListener {
        public void onBSUIEvent(BSUIEvent event);
    }

}
