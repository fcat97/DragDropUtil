# DragDropUtil [![](https://jitpack.io/v/fcat97/DragDropUtil.svg)](https://jitpack.io/#fcat97/DragDropUtil)

### A very simple way to Drag&Drop items in RecyclerVeiw

This is a simple library that removes all the boiler-plate code
that is needed to add Drag and Swap functionality in recyclerView

### How to use?

#### step 1: Adding dependency

Add it in your root build.gradle at the end of repositories:

```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

and then Add the dependency in `build.gradle(module:app)`

```gradle
dependencies {
        implementation 'com.github.fcat97:DragDropUtil:1.1.26'
}
```

now sync the project

#### Step 2: Using the library

Let assume,

`subject` is the item which is being moved

`target` is the item in which position the item will be moved finally


```java
new DragSwapUtil<>(
        recyclerView, 
        viewModel.listLiveData::getValue)
        .setPriorityListeners(new DragSwapUtil.PriorityListeners() {
                    @Override
                    public int priorityOf(int itemPos) {
                        // asking for the priority of the target item
                        // return the target's priority
                        // needed to persist data like in database...
                
                        // return 0 if you don't care about database
                        return adapter.getCurrentList().get(itemPos).tag.priority;
                    }

                    @Override
                    public void newPriorityOf(int itemPos, int priority) {
                        // the final position of subject and its priority after move is complete
                        // to persist the list i.e. save the list order...
                        // just change the priority of the list item@itemPosition with given priority
                
                        // leave empty if you don't care about persistance
                        adapter.getCurrentList().get(itemPos).tag.priority = priority;
                    }
        });
```

And there are listeners which will notify you...
each time items position are changed in list..
add them when needed...

**That's all...**
**Happy Coding...**
