package kaist.cs550_2016.poche;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Leegeun Ha.
 */

/**
 * Provides generic ViewHolder pattern utility when use Views<br>
 * that requires reuse child Views through Adapters such as {@link android.widget.ListView}.<br>
 * See <a href="http://developer.android.com/training/improving-layouts/smooth-scrolling.html">this page</a> for details.
 */
public class ViewHolder {

    /**
     * Get {@link View} from ViewHolder.
     * @param view {@link View} that can contain ViewHolder<br>
     *             In example of {@link android.widget.Adapter#getView(int, View, ViewGroup)}, use convertView parameter.
     * @param id Resource id of {@link View} to retrieve/store.
     * @return {@link View} that stored in ViewHolder.
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
