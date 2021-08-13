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
    private final PriorityListeners priorityListeners;

    public enum PriorityOrder { DESCENDING, ASCENDING }
    private PriorityOrder priorityOrder;

    /**
     * Constructor Method for {@link DragSwapUtil}
     * @param bindTo {@link RecyclerView} to which {@link DragSwapUtil} applies
     * @param priorityListeners interface which provide info about priority.
     *                          See {@link PriorityListeners} for details
     * @param getListCallback interface which fetch current list of items
     */
    public DragSwapUtil(RecyclerView bindTo,
                        PriorityListeners priorityListeners,
                        GetListCallback<T> getListCallback) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                0);
        this.getListCallback = getListCallback;
        this.priorityListeners = priorityListeners;
        this.priorityOrder = PriorityOrder.DESCENDING;
        new ItemTouchHelper(this).attachToRecyclerView(bindTo);
    }

    /**
     * Private method that calculates priority of moved Item
     * @param targetPriority priority of the object whose position will be taken by moving object
     * @param viewHolderPos position of moving object <b>before move complete</b>
     * @param targetPos position of target object <b>before move complete</b>
     * @return priority of moving object <b>when move is complete</b>
     */
    private int viewHolderPriority(int targetPriority,
                                   int viewHolderPos,
                                   int targetPos) {
        if (priorityOrder == PriorityOrder.DESCENDING) {
            // upward move -> increase priority
            // downward move -> decrease priority
            if (viewHolderPos > targetPos) { // moving upward
                return targetPriority + 1;
            } else return targetPriority - 1;
        } else { // ascending order
            // downward move -> increase priority
            // upward move -> decrease priority
            if (viewHolderPos > targetPos) { // moving upward
                return targetPriority - 1;
            } else return targetPriority + 1;
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        Log.d(TAG, "onMove: called");
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();
        int targetPriority = priorityListeners.priorityOf(to); // get priority of targetPos object by callback
        int viewHolderPriority = viewHolderPriority(targetPriority, from, to); // calculates priority when move is complete

        // insert item to target position and remove from previous pos
        List<T> list = getListCallback.getList(); // on which shifting will be made
        T itemFrom = list.get(from);
        try {
            list.remove(from);
            list.add(to, itemFrom);
        } catch (Exception e) {
            Log.d(TAG, "onMove: failed to perform action");
            e.printStackTrace();
        }

        priorityListeners.newPriorityOf(to, viewHolderPriority); // send new priority by callback

        // finally notify the adapter...
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemMoved(from, to);
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
        if (onMovedListener != null) onMovedListener.onMoved().run();
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                         int direction) {

        // do some work when item swiped done...
        // eg. push modified list to repository for persistence...
        if (onSwappedListener != null) onSwappedListener.onSwiped().run();
    }

    public DragSwapUtil<T> setPriorityOrder(PriorityOrder priorityOrder) {
        this.priorityOrder = priorityOrder;
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

    /**
     * Interface which communicates to mother class in which this class instantiated
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
         * This method is called after the shifting is made and before {@link OnMovedListener#onMoved()} called
         * @param itemPos them moving object object position after shifting complete
         * @param priority priority of the moving object after shifting made
         */
        void newPriorityOf(int itemPos, int priority);
    }
    public interface OnMovedListener { Runnable onMoved();}
    public interface OnSwappedListener { Runnable onSwiped();}

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

