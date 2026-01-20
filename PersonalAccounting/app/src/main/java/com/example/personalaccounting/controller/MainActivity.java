package com.example.personalaccounting.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalaccounting.R;
import com.example.personalaccounting.model.Bill;
import com.example.personalaccounting.model.BillRepository;
import com.example.personalaccounting.view.BottomNavigationItem;
import com.example.personalaccounting.view.RecentBillAdapter;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 控件
    private TextView tvMonthIncome; // 本月收入
    private TextView tvMonthExpense; // 本月支出
    private TextView tvMonthBalance; // 本月结余
    private MaterialButton btnRecordIncome; // 记收入按钮
    private MaterialButton btnRecordExpense; // 记支出按钮
    private MaterialButton btnAllBills; // 全部账单按钮
    private Spinner spDateFilter; // 日期选择器
    private BottomNavigationItem navHome; // 首页导航项
    private BottomNavigationItem navCalendar; // 日历导航项
    private RecyclerView rvRecentBills; // 近期账单列表
    private TextView tvEmpty; // 空数据提示

    // 数据
    private BillRepository mBillRepository; // 账单仓库（MVC的Model层）
    private RecentBillAdapter mAdapter; // 近期账单适配器
    private List<Bill> mRecentBillList; // 近期账单列表数据
    private DecimalFormat mDecimalFormat; // 金额格式化器
    private String mCurrentMonth; // 当前月份
    private int mCurrentDays; // 当前选择的天数
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局
        setContentView(R.layout.activity_main);

        // 初始化控件
        initViews();

        // 初始化数据
        initData();

        // 设置监听器
        setListeners();

        // 加载本月统计数据
        loadMonthStatistics();

        // 加载近期账单
        loadRecentBills();
    }
    /**
     * 初始化控件
     */
    private void initViews() {
        tvMonthIncome = findViewById(R.id.tv_month_income);
        tvMonthExpense = findViewById(R.id.tv_month_expense);
        tvMonthBalance = findViewById(R.id.tv_month_balance);
        btnRecordIncome = findViewById(R.id.btn_record_income);
        btnRecordExpense = findViewById(R.id.btn_record_expense);
        btnAllBills = findViewById(R.id.btn_all_bills);
        spDateFilter = findViewById(R.id.sp_date_filter);
        navHome = findViewById(R.id.nav_home);
        navCalendar = findViewById(R.id.nav_calendar);
        rvRecentBills = findViewById(R.id.rv_recent_bills);
        tvEmpty = findViewById(R.id.tv_empty);

        // 设置首页导航项为激活状态
        navHome.setActive(true);
        navCalendar.setActive(false);
    }
    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化账单仓库（MVC的Model层）
        mBillRepository = new BillRepository(this);

        // 初始化近期账单列表
        mRecentBillList = new ArrayList<>();

        // 初始化金额格式化器，保留2位小数
        mDecimalFormat = new DecimalFormat("0.00");

        // 获取当前月份，格式：yyyy-MM
        mCurrentMonth = mBillRepository.getCurrentMonth();

        // 初始化日期选择器
        initDateFilter();

        // 设置RecyclerView的布局管理器
        // 还可以设置GridLayoutManager（网格布局）、StaggeredGridLayoutManager（瀑布流布局）
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvRecentBills.setLayoutManager(layoutManager);

        // 初始化适配器
        mAdapter = new RecentBillAdapter(this, mRecentBillList);
        rvRecentBills.setAdapter(mAdapter);
    }

    /**
     * 初始化日期选择器
     */
    private void initDateFilter() {
        // 创建日期选项
        List<String> dateOptions = new ArrayList<>();
        dateOptions.add("最近1天");
        dateOptions.add("最近3天");
        dateOptions.add("最近7天");
        dateOptions.add("最近30天");

        // 创建适配器
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                dateOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDateFilter.setAdapter(adapter);

        // 设置默认选中7天
        spDateFilter.setSelection(2);
        mCurrentDays = 7;

        // 设置选择监听器
        spDateFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mCurrentDays = 1;
                        break;
                    case 1:
                        mCurrentDays = 3;
                        break;
                    case 2:
                        mCurrentDays = 7;
                        break;
                    case 3:
                        mCurrentDays = 30;
                        break;
                }
                loadRecentBills();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 设置监听器
     */
    private void setListeners() {
        // 记收入按钮点击事件
        btnRecordIncome.setOnClickListener(v -> {
            Intent intent = new Intent(this, BillEditActivity.class);
            intent.putExtra("bill_type", 1);
            startActivityForResult(intent, 1);
        });

        // 记支出按钮点击事件
        btnRecordExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, BillEditActivity.class);
            intent.putExtra("bill_type", 0);
            startActivityForResult(intent, 1);
        });

        // 全部账单按钮点击事件
        btnAllBills.setOnClickListener(v -> {
            Intent intent = new Intent(this, BillListActivity.class);
            startActivity(intent);
        });

        // 首页导航项点击事件
        navHome.setOnClickListener(v -> {
            // 当前已在首页，无需操作
        });

        // 日历导航项点击事件
        navCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 加载本月统计数据（异步）
     */
    private void loadMonthStatistics() {
        // 使用异步方法获取本月统计数据
        mBillRepository.calculateMonthStatisticsAsync(mCurrentMonth, new BillRepository.Callback<BillRepository.MonthStatistics>() {
            @Override
            public void onSuccess(BillRepository.MonthStatistics statistics) {
                // 在UI线程更新界面
                runOnUiThread(() -> {
                    // 更新UI（Controller层只负责UI更新，不处理业务逻辑）
                    tvMonthIncome.setText("本月收入：" + mDecimalFormat.format(statistics.getIncome()) + "元");
                    tvMonthExpense.setText("本月支出：" + mDecimalFormat.format(statistics.getExpense()) + "元");

                    // 设置结余颜色：收入>支出标绿色，支出>收入标红色
                    if (statistics.getBalance() >= 0) {
                        /**
                         getResources() 方法返回一个 Resources 对象，用于访问应用的所有资源文件 ，包括：
                         - 字符串资源（ strings.xml ）
                         - 颜色资源（ colors.xml ）
                         - 尺寸资源（ dimens.xml ）
                         */
                        tvMonthBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        tvMonthBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                    tvMonthBalance.setText("本月结余：" + mDecimalFormat.format(statistics.getBalance()) + "元");
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "加载本月统计数据失败: " + e.getMessage());
            }
        });
    }

    /**
     * 加载近期账单（根据选择的天数）- 异步方法
     * 1. Activity调用BillRepository的异步方法（如getBillsByDaysAsync）
     * 2. 该方法在后台线程（由ExecutorService管理）执行数据库操作
     * 3. 数据库操作完成后，**回调方法onSuccess在同一个后台线程中被调用**
     * 4. 如果直接在onSuccess中更新UI，就会在后台线程操作UI，导致崩溃
     * 5. 因此需要使用runOnUiThread将UI更新代码切换到主线程执行
     */
    private void loadRecentBills() {
        // 使用异步方法获取指定天数的账单数据
        mBillRepository.getBillsByDaysAsync(mCurrentDays, new BillRepository.Callback<List<Bill>>() {
            @Override
            public void onSuccess(List<Bill> recentBills) {
                // 在UI线程更新界面
                // Android的UI操作必须在主线程（UI线程）执行 ，否则会抛出异常！
                // 将代码块切换到主线程执行 。它的作用是确保UI操作的线程安全。
                runOnUiThread(() -> {
                    // 清空当前列表并更新数据
                    mRecentBillList.clear();
                    mRecentBillList.addAll(recentBills);

                    // 更新适配器
                    mAdapter.updateData(mRecentBillList);

                    // 显示/隐藏空数据提示（Controller层只负责UI控制）
                    if (mRecentBillList.isEmpty()) {
                        rvRecentBills.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvRecentBills.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "加载近期账单失败: " + e.getMessage());
            }
        });
    }


//    startActivityForResult 启动 BillEditActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 当从记账编辑页面返回时，重新加载数据
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadMonthStatistics();
            loadRecentBills();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次Activity重新显示时刷新数据
        // 检查repository是否已关闭，如果已关闭则重新创建
        if (mBillRepository == null) {
            mBillRepository = new BillRepository(this);
        }
        loadMonthStatistics();
        loadRecentBills();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 不关闭账单仓库资源，因为Activity可能被重建
        // 如果需要关闭，应该在onPause中检查isFinishing()
    }
}