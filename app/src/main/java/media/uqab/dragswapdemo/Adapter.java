package media.uqab.dragswapdemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class Adapter extends ListAdapter<Item, Adapter.ViewHolder> {

    private static DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.name.equals(newItem.name) && oldItem.priority == newItem.priority;
        }
    };

    public Adapter() {
        super(DIFF_CALLBACK);
    }


    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = getItem(position);

        holder.nameTv.setText(item.name);
        String hash = "@" + item.hashCode();
        holder.hashTv.setText(hash);
        holder.priorityTv.setText(String.valueOf(item.priority));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTv;
        private TextView hashTv;
        private TextView priorityTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            hashTv = itemView.findViewById(R.id.hashTv);
            priorityTv = itemView.findViewById(R.id.priorityTv);
        }
    }
}
