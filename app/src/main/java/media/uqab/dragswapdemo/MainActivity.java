package media.uqab.dragswapdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import media.uqab.dragswaputil.DragSwapUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private ArrayList<Item> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Adapter adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        adapter.submitList(getArrayList());

        new DragSwapUtil<>(recyclerView, this::getArrayList)
                .setPriorityListeners(new DragSwapUtil.PriorityListeners() {
                            @Override
                            public int priorityOf(int itemPos) {
                                Log.d(TAG, "priorityOf: --------------------------------");
                                Item i = getArrayList().get(itemPos);
                                Log.d(TAG, "priorityOf: " + i.name + "@" + i.hashCode() + " ->" + i.priority);
//                              for (Item i: getArrayList()) Log.d(TAG, "priorityOf: " + i.name + "@" + i.hashCode() + " ->" + i.priority);
                                return getArrayList().get(itemPos).priority;
                            }

                            @Override
                            public void newPriorityOf(int itemPos, int priority) {
                                getArrayList().get(itemPos).priority = priority;
                                Log.d(TAG, "newPriorityOf: -----------------------------------");
                                Item i = getArrayList().get(itemPos);
                                Log.d(TAG, "newPriorityOf: " + i.name + "@" + i.hashCode() + " ->" + i.priority);
//                              for (Item i: getArrayList()) Log.d(TAG, "newPriorityOf: " + i.name + "@" + i.hashCode() + " ->" + i.priority);
                            }
                        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // do the persistence functionality here...
        // i.e. pushing to database or network etc...
        Log.d(TAG, "onDestroy: --------------------------------------");
        for (Item i: getArrayList()) Log.d(TAG, "onDestroy: " + i.name + "@" + i.hashCode() + " ->" + i.priority);
    }

    private ArrayList<Item> getArrayList() {
        if (arrayList == null) {
            arrayList = new ArrayList<>();
            arrayList.add(new Item("Apple", 1));
            arrayList.add(new Item("Orange", 2));
            arrayList.add(new Item("Guava", 3));
            arrayList.add(new Item("Banana", 4));
            arrayList.add(new Item("PineApple", 5));
            arrayList.add(new Item("Mango", 6));
            arrayList.add(new Item("Watermelon", 7));
            arrayList.add(new Item("Coconut", 8));

            Collections.reverse(arrayList);
        }
        return arrayList;
    }
}