package com.cncoderx.test.wheelview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.WheelView;

import java.util.Calendar;

/**
 * @author cncoderx
 */
public class DatePickerTestActivity extends Activity {
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
                "1980年", "1981年", "1982年", "1983年", "1984年", "1985年", "1986年", "1987年", "1988年", "1989年",
                "1990年", "1991年", "1992年", "1993年", "1994年", "1995年", "1996年", "1997年", "1998年", "1999年",
                "2000年", "2001年", "2002年", "2003年", "2004年", "2005年", "2006年", "2007年", "2008年", "2009年",
                "2010年", "2011年", "2012年", "2013年", "2014年", "2015年", "2016年", "2017年", "2018年", "2019年", "2020年"});

        wvMonth.setEntries(new String[] {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"});

        wvYear.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldIndex, int newIndex) {
                String text = (String) wvYear.getItem(newIndex);
                mYear = Integer.parseInt(text.substring(0, text.length() - 1));
                updateDayEntries();
                updateTextView();
            }
        });
        wvMonth.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldIndex, int newIndex) {
                String text = (String) wvMonth.getItem(newIndex);
                mMonth = Integer.parseInt(text.substring(0, text.length() - 1));
                updateDayEntries();
                updateTextView();
            }
        });
        wvDay.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldIndex, int newIndex) {
                String text = (String) wvDay.getItem(newIndex);
                mDay = Integer.parseInt(text.substring(0, text.length() - 1));
                updateTextView();
            }
        });

        mYear = 1980;
        mMonth = 1;
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
                        "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日",
                        "11日", "12日", "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日",
                        "21日", "22日", "23日", "24日", "25日", "26日", "27日", "28日"});
                break;
            case 29:
                wvDay.setEntries(new String[] {
                        "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日",
                        "11日", "12日", "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日",
                        "21日", "22日", "23日", "24日", "25日", "26日", "27日", "28日", "29日"});
                break;
            case 30:
                wvDay.setEntries(new String[] {
                        "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日",
                        "11日", "12日", "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日",
                        "21日", "22日", "23日", "24日", "25日", "26日", "27日", "28日", "29日", "30日"});
                break;
            case 31:
            default:
                wvDay.setEntries(new String[] {
                        "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日",
                        "11日", "12日", "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日",
                        "21日", "22日", "23日", "24日", "25日", "26日", "27日", "28日", "29日", "30日",
                        "31日"});
                break;
        }
    }

    private void updateTextView() {
        String text = String.format("%04d年%d月%d日", mYear, mMonth, mDay);
        mTextView.setText(text);
    }
}
