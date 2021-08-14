package media.uqab.dragswaputil;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

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
     * Constructor Method for {@link DragSwapUtil}
     * @param bindTo {@link RecyclerView} to which {@link DragSwapUtil} applies
     * @param getListCallback interface which fetch current list of items
     */
    public DragSwapUtil(RecyclerView bindTo,
                        GetListCallback<T> getListCallback) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
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

        // swap priorities
        int viewHolderPriorityAfterMove = 0;
        int targetPriorityAfterMove = 0;
        if (priorityListeners != null) {
            viewHolderPriorityAfterMove = priorityListeners.priorityOf(to);
            targetPriorityAfterMove = priorityListeners.priorityOf(from);

            if (viewHolderPriorityAfterMove == targetPriorityAfterMove) {
                Log.d(TAG, "onMove: viewHolderPriorityAfterMove==targetPriorityAfterMove = " + viewHolderPriorityAfterMove);
                if (from > to) { // moving upward
                    if (listOrder == ListOrder.DESCENDING) { // increase priority
                        viewHolderPriorityAfterMove += 1;
                    } else viewHolderPriorityAfterMove -= 1;
                } else { // moving downward
                    if (listOrder == ListOrder.DESCENDING) { // decrease priority
                        viewHolderPriorityAfterMove -= 1;
                    } else viewHolderPriorityAfterMove += 1;
                }
            }

            Log.d(TAG, "onMove: viewHolderPriorityAfterMove " + viewHolderPriorityAfterMove);
            Log.d(TAG, "onMove: targetPriorityAfterMove " + targetPriorityAfterMove);
        }


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

        // finally notify the adapter...
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemMoved(from, to);

        // notice that after swap...
        // the viewHolder is at the `to` location
        // and the target is at `from` location

        if (priorityListeners != null) {
            priorityListeners.newPriorityOf(to, viewHolderPriorityAfterMove); // send new priority by callback
            priorityListeners.newPriorityOf(from, targetPriorityAfterMove); // send new priority by callback
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

        // notify adapter that item has changed
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(fromPos);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(toPos);

        // do some work after shifting done...
        // eg. push modified list to repository for persistence...
        if (onMovedListener != null) onMovedListener.onMoved(fromPos, toPos);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                         int direction) {

        // do some work when item swiped done...
        // eg. push modified list to repository for persistence...
        if (onSwappedListener != null) onSwappedListener.onSwiped();
    }

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
     * Use this interface if persistence is needed like saving to database...
     * This provides two methods
     */
    public interface PriorityListeners{
        /**
         * Method to get the priority of target position object from the listOfItems
         * @param itemPos target position
         * @return priority of object at target position
         */
        int priorityOf(int itemPos);

        /**
         * Sends the priority of moving object to the class where {@link DragSwapUtil} is instantiated
         * This method is called after the shifting is made and before {@link OnMovedListener#onMoved)} called
         * @param itemPos the moving object object position after shifting complete
         * @param newPriority priority of the moving object after shifting made
         */
        void newPriorityOf(int itemPos, int newPriority);
    }
    public interface OnMovedListener { void onMoved(int from, int to);}
    public interface OnSwappedListener { void onSwiped();}

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

