package com.cncoderx.test.wheelview;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1,
                new String[]{"Simple Test", "Date Picker Test"}));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, SimpleTestActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, DatePickerTestActivity.class));
                break;
        }
    }
}
