package media.uqab.dragswaputil;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DragSwapUtil<T> extends ItemTouchHelper.SimpleCallback {
    private static final String TAG = DragSwapUtil.class.getName();
    private OnMovedListener onMovedListener;
    private OnSwappedListener onSwappedListener;
    private final GetListCallback<T> getListCallback;
    private PriorityListeners priorityListeners;

    public enum ListOrder { DESCENDING, ASCENDING }
    private ListOrder listOrder;

    /**
     * Swap the moving items of list
     */
    private boolean rearrangeList = false;

    /**
     * Constructor Method for {@link DragSwapUtil}
     * @param bindTo {@link RecyclerView} to which {@link DragSwapUtil} applies
     * @param getListCallback interface which fetch current list of items
     */
    public DragSwapUtil(RecyclerView bindTo,
                        GetListCallback<T> getListCallback) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT,
                0);
        this.getListCallback = getListCallback;
        this.listOrder = ListOrder.DESCENDING;
        new ItemTouchHelper(this).attachToRecyclerView(bindTo);
    }


    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        Log.d(TAG, "onMove: called");
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();

        // show movement animation
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemMoved(from, to);


        // a bug in adapter make the onMove() unstoppable
        // invoking the adapter.notifyItemChanged() makes this
        // onMove() to call twice. The first one is true call
        // but second time, the value of `to` & `from` is equal,
        // which is unnecessary. That's why this extra if(from != to) checked
        if (from != to) {
            // swap priorities
            if (priorityListeners != null) {
                int tarPrio = priorityListeners.priorityOf(from);
                int subPrio = priorityListeners.priorityOf(to);

                // if both the subject & target has same priority
                if (subPrio == tarPrio) {
                    Log.d(TAG, "onMove: subPrio==tarPrio = " + subPrio);
                    if (from > to) { // moving upward
                        if (listOrder == ListOrder.DESCENDING) { // increase priority
                            subPrio += 1;
                        } else subPrio -= 1;
                    } else { // moving downward
                        if (listOrder == ListOrder.DESCENDING) { // decrease priority
                            subPrio -= 1;
                        } else subPrio += 1;
                    }
                }

                Log.d(TAG, "onMove: subPrio " + subPrio);
                Log.d(TAG, "onMove: tarPrio " + tarPrio);

                priorityListeners.newPriorityOf(from, subPrio); // send new priority by callback
                priorityListeners.newPriorityOf(to, tarPrio); // send new priority by callback
            }

            if (rearrangeList) {
                // insert item to target position and remove from previous pos
                List<T> list = getListCallback.getList(); // on which shifting will be made
                T itemFrom = list.get(from);
                try {
                    list.remove(from);
                    list.add(to, itemFrom);
                    Log.d(TAG, "onMove: item moved successfully");
                } catch (Exception e) {
                    Log.d(TAG, "onMove: failed to perform action");
                    e.printStackTrace();
                }

                recyclerView.getAdapter().notifyItemChanged(from);
                recyclerView.getAdapter().notifyItemChanged(to);
            }
        }


        return true;
    }

    @Override
    public void onMoved(@NonNull RecyclerView recyclerView,
                        @NonNull RecyclerView.ViewHolder viewHolder,
                        int fromPos,
                        @NonNull RecyclerView.ViewHolder target,
                        int toPos,
                        int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);

        // do some work after shifting done...
        // eg. push modified list to repository for persistence...
        if (onMovedListener != null && fromPos != toPos) onMovedListener.onMoved(fromPos, toPos);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                         int direction) {

        // do some work when item swiped done...
        // eg. push modified list to repository for persistence...
        if (onSwappedListener != null) onSwappedListener.onSwiped(viewHolder.getAdapterPosition(), direction);
    }


    /**
     * Swap moving items and rearrange the list internally automatically
     * @param rearrangeList 'false' by default
     * @return {@link DragSwapUtil}
     */
    public DragSwapUtil<T> setRearrangeList(boolean rearrangeList) {
        this.rearrangeList = rearrangeList;
        return this;
    }

    /**
     * Set the applied list order.
     * List order is necessary to work correctly
     * Only applicable when {@linkplain PriorityListeners} is attached
     * @param listOrder {@link ListOrder}
     * @return {@linkplain DragSwapUtil}
     */
    public DragSwapUtil<T> setListOrder(ListOrder listOrder) {
        this.listOrder = listOrder;
        return this;
    }

    public DragSwapUtil<T> setOnMovedListener(OnMovedListener onMovedListener) {
        this.onMovedListener = onMovedListener;
        return this;
    }

    public DragSwapUtil<T> setOnSwappedListener(OnSwappedListener onSwappedListener) {
        this.onSwappedListener = onSwappedListener;
        return this;
    }

    public DragSwapUtil<T> setPriorityListeners(PriorityListeners priorityListeners) {
        this.priorityListeners = priorityListeners;
        return this;
    }

    /**
     * Interface which communicates to mother class in which this class instantiated
     * Set this listener to {@link DragSwapUtil} if persistence is needed like saving to database...
     */
    public interface PriorityListeners{
        /**
         * Method to get the priority of object from the listOfItems
         * @param itemPos position of which priority is returned
         * @return priority of object at this position
         */
        int priorityOf(int itemPos);

        /**
         * Sends the priority of moving object to the class where {@link DragSwapUtil} is instantiated
         * This method is called after the shifting is made and before {@link OnMovedListener#onMoved)} called
         * @param itemPos the object position **before** moving
         * @param newPriority priority of the object after moving
         */
        void newPriorityOf(int itemPos, int newPriority);
    }
    public interface OnMovedListener { void onMoved(int from, int to); }
    public interface OnSwappedListener { void onSwiped(int position, int direction); }

    /**
     * Listener which fetch the list on which shifting will be made
     * @param <T> TypedParameter of of ListItems
     */
    public interface GetListCallback<T> {
        /**
         * @return the <b>mutable List</b> on which shifting will be made
         */
        List<T> getList();
    }
}

