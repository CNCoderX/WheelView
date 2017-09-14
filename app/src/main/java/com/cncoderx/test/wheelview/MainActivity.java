package com.cncoderx.test.wheelview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.WheelView;

import java.util.Calendar;

public class MainActivity extends Activity {
    TextView mTextView;
    WheelView wvYear, wvMonth, wvDay;

    int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textView2);
        wvYear = (WheelView) findViewById(R.id.wv_year);
        wvMonth = (WheelView) findViewById(R.id.wv_month);
        wvDay = (WheelView) findViewById(R.id.wv_day);

        wvYear.setEntries(new String[]{
                "1980", "1981", "1982", "1983", "1984", "1985", "1986", "1987", "1988", "1989",
                "1990", "1991", "1992", "1993", "1994", "1995", "1996", "1997", "1998", "1999",
                "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009",
                "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020"});

        wvMonth.setEntries(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"});

        wvYear.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldIndex, int newIndex) {
                mYear = Integer.parseInt((String) wvYear.getItem(newIndex));
                updateDayEntries();
                updateTextView();
            }
        });
        wvMonth.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldIndex, int newIndex) {
                mMonth = Integer.parseInt((String) wvMonth.getItem(newIndex));
                updateDayEntries();
                updateTextView();
            }
        });
        wvDay.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldIndex, int newIndex) {
                mDay = Integer.parseInt((String) wvDay.getItem(newIndex));
                updateTextView();
            }
        });

        mYear = Integer.parseInt((String) wvYear.getCurrentItem());
        mMonth = Integer.parseInt((String) wvMonth.getCurrentItem());
        mDay = 1;
        updateDayEntries();
        updateTextView();
    }

    private void updateDayEntries() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.MONTH, mMonth - 1);

        int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        switch (days) {
            case 28:
                wvDay.setEntries(new String[] {
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                        "21", "22", "23", "24", "25", "26", "27", "28",});
                break;
            case 29:
                wvDay.setEntries(new String[] {
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                        "21", "22", "23", "24", "25", "26", "27", "28", "29"});
                break;
            case 30:
                wvDay.setEntries(new String[] {
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                        "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"});
                break;
            case 31:
            default:
                wvDay.setEntries(new String[] {
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                        "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"});
                break;
        }
    }

    private void updateTextView() {
        String text = String.format("%04d年%d月%d日", mYear, mMonth, mDay);
        mTextView.setText(text);
    }
}
