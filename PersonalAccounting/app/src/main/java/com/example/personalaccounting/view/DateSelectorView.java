package com.example.personalaccounting.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.personalaccounting.R;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateSelectorView extends LinearLayout {
    private static final String TAG = "DateSelectorView";

    public interface OnDateChangeListener {
        void onDateChanged(int dimension, String dateValue);
    }

    private TabLayout tabTimeDimension;
    private TextView tvCurrentDate;
    private ImageButton btnPrevious;
    private ImageButton btnNext;

    private OnDateChangeListener mListener;
    private int mCurrentDimension = 1;
    private int mCurrentWeekNumber = 1;

    private static final Calendar BASE_DATE = Calendar.getInstance();
    static {
        BASE_DATE.set(2026, Calendar.JANUARY, 1, 0, 0, 0);
    }

    public DateSelectorView(Context context) {
        super(context);
        init(context);
    }

    public DateSelectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateSelectorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_date_selector, this, true);
        tabTimeDimension = findViewById(R.id.tab_time_dimension);
        tvCurrentDate = findViewById(R.id.tv_current_date);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);

        setupTabs();
        setupButtons();
        calculateCurrentWeek();
        updateDateDisplay();
    }

    private void setupTabs() {
        TabLayout.Tab weekTab = tabTimeDimension.newTab();
        weekTab.setText("周");
        tabTimeDimension.addTab(weekTab);

        TabLayout.Tab monthTab = tabTimeDimension.newTab();
        monthTab.setText("月");
        tabTimeDimension.addTab(monthTab);

        TabLayout.Tab yearTab = tabTimeDimension.newTab();
        yearTab.setText("年");
        tabTimeDimension.addTab(yearTab);

        tabTimeDimension.selectTab(tabTimeDimension.getTabAt(1));
        mCurrentDimension = 2;

        tabTimeDimension.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mCurrentDimension = position + 1;
                if (mCurrentDimension == 1) {
                    calculateCurrentWeek();
                }
                updateDateDisplay();
                notifyDateChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupButtons() {
        btnPrevious.setOnClickListener(v -> {
            navigatePrevious();
        });

        btnNext.setOnClickListener(v -> {
            navigateNext();
        });
    }

    private void calculateCurrentWeek() {
        Calendar today = Calendar.getInstance();
        long diffMillis = today.getTimeInMillis() - BASE_DATE.getTimeInMillis();
        long diffDays = diffMillis / (24 * 60 * 60 * 1000);
        if (diffDays >= 0) {
            mCurrentWeekNumber = (int) (diffDays / 7) + 1;
        } else {
            mCurrentWeekNumber = 1;
        }
    }

    private Calendar getWeekStartDate(int weekNumber) {
        Calendar startDate = (Calendar) BASE_DATE.clone();
        startDate.add(Calendar.DAY_OF_MONTH, (weekNumber - 1) * 7);
        return startDate;
    }

    private Calendar getWeekEndDate(int weekNumber) {
        Calendar endDate = getWeekStartDate(weekNumber);
        endDate.add(Calendar.DAY_OF_MONTH, 6);
        return endDate;
    }

    private void navigatePrevious() {
        switch (mCurrentDimension) {
            case 1:
                if (mCurrentWeekNumber > 1) {
                    mCurrentWeekNumber--;
                }
                break;
            case 2:
                Calendar currentMonth = Calendar.getInstance();
                currentMonth.add(Calendar.MONTH, -1);
                break;
            case 3:
                Calendar currentYear = Calendar.getInstance();
                currentYear.add(Calendar.YEAR, -1);
                break;
        }
        updateDateDisplay();
        notifyDateChanged();
    }

    private void navigateNext() {
        switch (mCurrentDimension) {
            case 1:
                mCurrentWeekNumber++;
                break;
            case 2:
                Calendar currentMonth = Calendar.getInstance();
                currentMonth.add(Calendar.MONTH, 1);
                break;
            case 3:
                Calendar currentYear = Calendar.getInstance();
                currentYear.add(Calendar.YEAR, 1);
                break;
        }
        updateDateDisplay();
        notifyDateChanged();
    }

    private void updateDateDisplay() {
        String dateText = "";
        switch (mCurrentDimension) {
            case 1:
                Calendar startOfWeek = getWeekStartDate(mCurrentWeekNumber);
                Calendar endOfWeek = getWeekEndDate(mCurrentWeekNumber);
                
                SimpleDateFormat dayFormat = new SimpleDateFormat("M月d日", Locale.getDefault());
                String startDate = dayFormat.format(startOfWeek.getTime());
                String endDate = dayFormat.format(endOfWeek.getTime());
                dateText = startDate + " ~ " + endDate;
                break;
            case 2:
                Calendar currentMonth = Calendar.getInstance();
                int month = currentMonth.get(Calendar.MONTH) + 1;
                int yearMonth = currentMonth.get(Calendar.YEAR);
                dateText = yearMonth + "年" + month + "月";
                break;
            case 3:
                Calendar currentYear = Calendar.getInstance();
                int yearOnly = currentYear.get(Calendar.YEAR);
                dateText = yearOnly + "年";
                break;
        }
        tvCurrentDate.setText(dateText);
    }

    private void notifyDateChanged() {
        if (mListener != null) {
            String dateValue = getCurrentDateValue();
            mListener.onDateChanged(mCurrentDimension, dateValue);
        }
    }

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        mListener = listener;
    }

    public int getCurrentDimension() {
        return mCurrentDimension;
    }

    public String getCurrentDateValue() {
        SimpleDateFormat sdf;
        switch (mCurrentDimension) {
            case 1:
                Calendar startOfWeek = getWeekStartDate(mCurrentWeekNumber);
                Calendar endOfWeek = getWeekEndDate(mCurrentWeekNumber);
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return sdf.format(startOfWeek.getTime()) + "," + sdf.format(endOfWeek.getTime());
            case 2:
                sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                Calendar currentMonth = Calendar.getInstance();
                return sdf.format(currentMonth.getTime());
            case 3:
                sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
                Calendar currentYear = Calendar.getInstance();
                return sdf.format(currentYear.getTime());
            default:
                return "";
        }
    }

    public void resetToCurrentDate() {
        calculateCurrentWeek();
        updateDateDisplay();
        notifyDateChanged();
    }
}
