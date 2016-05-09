package kaist.cs550_2016.poche;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Leegeun Ha on 2016-05-09.
 */

public class BSUI extends GestureDetector.SimpleOnGestureListener {

    private BSUIEventListener bsuiEventListener;

    public void setBSUIEventListener(BSUIEventListener bsuiEventListener) {
        this.bsuiEventListener = bsuiEventListener;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float speedX = Math.abs(velocityX);
        float speedY = Math.abs(velocityY);

        if (speedX > speedY) {
            if (e1.getX() < e2.getX()) {
                fireEvent(BSUIEvent.STROKE_RIGHT);
            }
            else if (e1.getX() > e2.getX()) {
                fireEvent(BSUIEvent.STROKE_LEFT);
            }
        }

        if (speedX < speedY) {
            if (e1.getY() < e2.getY()) {
                fireEvent(BSUIEvent.STROKE_DOWN);
            }
            else if (e1.getY() > e2.getY()) {
                fireEvent(BSUIEvent.STROKE_UP);
            }
        }

        return true;
    }

    private void fireEvent(BSUIEvent event) {
        if (bsuiEventListener != null) {
            BSUIEvent adjustedEvent = event;
            ConfigHelper.StrokeOrientation orientation =
                    ConfigHelper.getInstance().getStrokeOrientation();

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

            bsuiEventListener.onBSUIEvent(adjustedEvent);
        }
    }

    public enum BSUIEvent {
        STROKE_UP,
        STROKE_DOWN,
        STROKE_LEFT,
        STROKE_RIGHT
    }

    public interface BSUIEventListener {
        public void onBSUIEvent(BSUIEvent event);
    }
}
