package com.example.personalaccounting.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalaccounting.R;
import com.example.personalaccounting.model.Bill;
import com.example.personalaccounting.model.BillRepository;
import com.example.personalaccounting.view.BottomNavigationItem;
import com.example.personalaccounting.view.RecentBillAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvTotalBalance;
    private RecyclerView rvBills;
    private TextView tvEmpty;
    private BottomNavigationItem navHome;
    private BottomNavigationItem navCalendar;
    private BillRepository mBillRepository;
    private RecentBillAdapter mAdapter;
    private SimpleDateFormat mDateFormat;
    private String mSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initViews();
        initData();
        setListeners();
        loadBillsForToday();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendar_view);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        rvBills = findViewById(R.id.rv_bills);
        tvEmpty = findViewById(R.id.tv_empty);
        navHome = findViewById(R.id.nav_home);
        navCalendar = findViewById(R.id.nav_calendar);

        // 设置日历导航项为激活状态
        navHome.setActive(false);
        navCalendar.setActive(true);
    }

    private void initData() {
        mBillRepository = new BillRepository(this);
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBills.setLayoutManager(layoutManager);

        // 初始化适配器，传入空列表
        mAdapter = new RecentBillAdapter(this, new ArrayList<>());
        rvBills.setAdapter(mAdapter);

        Calendar calendar = Calendar.getInstance();
        mSelectedDate = mDateFormat.format(calendar.getTime());
    }

    private void setListeners() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            mSelectedDate = mDateFormat.format(calendar.getTime());
            loadBillsForDate(mSelectedDate);
        });

        // 首页导航项点击事件
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 日历导航项点击事件
        navCalendar.setOnClickListener(v -> {
            // 当前已在日历页面，无需操作
        });
    }

    private void loadBillsForToday() {
        loadBillsForDate(mSelectedDate);
    }

    private void loadBillsForDate(String date) {
        mBillRepository.getBillsByDateAsync(date, new BillRepository.Callback<List<Bill>>() {
            @Override
            public void onSuccess(List<Bill> bills) {
                runOnUiThread(() -> {
                    // 直接更新适配器数据，AsyncListDiffer会自动计算差异
                    mAdapter.updateData(bills);

                    updateStatistics(bills);

                    if (bills.isEmpty()) {
                        rvBills.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvBills.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("CalendarActivity", "加载账单失败: " + e.getMessage());
            }
        });
    }

    private void updateStatistics(List<Bill> bills) {
        double income = 0.0;
        double expense = 0.0;

        for (Bill bill : bills) {
            if (bill.getBillType() == 1) {
                income += bill.getAmount();
            } else {
                expense += bill.getAmount();
            }
        }

        tvTotalIncome.setText(String.format("收入：%.2f元", income));
        tvTotalExpense.setText(String.format("支出：%.2f元", expense));
        tvTotalBalance.setText(String.format("结余：%.2f元", income - expense));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 使用单例获取BillRepository实例
        mBillRepository = BillRepository.getInstance(this);
        loadBillsForDate(mSelectedDate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
