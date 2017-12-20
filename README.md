## 使用说明
这个库包含两种类型的滚轮：`普通滚轮`和`立体滚轮`，普通滚轮调用WheelView，立体滚轮调用Wheel3DView。两种滚轮实现原理相同，但显示效果不同。立体滚轮类似IOS时间选择控件，效果如下。

## 演示
![img](https://github.com/CNCoderX/WheelView/blob/master/sample.gif)    ![img](https://github.com/CNCoderX/WheelView/blob/master/sample2.gif)

## 添加依赖
```compile
compile 'com.cncoderx.wheelview:library:1.2.1'
```
## 使用方法
#### 在xml文件中添加
```xml
<com.cncoderx.wheelview.Wheel3DView
    android:id="@+id/wheel"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:cyclic="true"
    app:entries="@array/array2"
    app:visibleItems="9"
    app:toward="right"/>
```
#### 在java文件中添加
```java
WheelView wheelView = (WheelView) findViewById(R.id.wheel);
wheelView.setOnWheelChangedListener(new OnWheelChangedListener() {
    @Override
    public void onChanged(WheelView view, int oldIndex, int newIndex) {
        CharSequence text = view.getItem(newIndex);
        Log.i("WheelView", String.format("index: %d, text: %s", newIndex, text));
    }
});
```

