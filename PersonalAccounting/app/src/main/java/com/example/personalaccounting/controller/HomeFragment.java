package com.example.personalaccounting.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalaccounting.R;
import com.example.personalaccounting.model.Bill;
import com.example.personalaccounting.model.BillRepository;
import com.example.personalaccounting.view.RecentBillAdapter;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final String KEY_CURRENT_DAYS = "current_days";

    private TextView tvMonthIncome;
    private TextView tvMonthExpense;
    private TextView tvMonthBalance;
    private MaterialButton btnRecordIncome;
    private MaterialButton btnRecordExpense;
    private MaterialButton btnAllBills;
    private Spinner spDateFilter;
    private RecyclerView rvRecentBills;
    private TextView tvEmpty;

    private BillRepository mBillRepository;
    private RecentBillAdapter mAdapter;
    private DecimalFormat mDecimalFormat;
    private int mCurrentDays = 7;
    private String mStatisticsRequestId;
    private String mBillsRequestId;

    private ActivityResultLauncher<Intent> mEditBillLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: 开始创建首页视图");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        Log.d(TAG, "onCreateView: 首页视图创建完成");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: 视图创建完成，开始初始化数据");
        initData();
        setListeners();
        registerActivityResult();
        Log.d(TAG, "onViewCreated: 初始化完成");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Fragment 开始可见，开始加载数据");
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Fragment 获得焦点，用户可以交互");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Fragment 失去焦点，取消待处理的异步请求");
        cancelPendingRequests();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: 视图即将销毁，清理视图引用");
        cleanupViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Fragment 即将销毁，再次取消异步请求");
        cancelPendingRequests();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_DAYS, mCurrentDays);
        Log.d(TAG, "onSaveInstanceState: 保存状态，当前天数=" + mCurrentDays);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentDays = savedInstanceState.getInt(KEY_CURRENT_DAYS, 7);
            Log.d(TAG, "onViewStateRestored: 恢复状态，当前天数=" + mCurrentDays);
            if (spDateFilter != null) {
                spDateFilter.setSelection(getPositionForDays(mCurrentDays));
            }
        }
    }

    private void initViews(View view) {
        tvMonthIncome = view.findViewById(R.id.tv_month_income);
        tvMonthExpense = view.findViewById(R.id.tv_month_expense);
        tvMonthBalance = view.findViewById(R.id.tv_month_balance);
        btnRecordIncome = view.findViewById(R.id.btn_record_income);
        btnRecordExpense = view.findViewById(R.id.btn_record_expense);
        btnAllBills = view.findViewById(R.id.btn_all_bills);
        spDateFilter = view.findViewById(R.id.sp_date_filter);
        rvRecentBills = view.findViewById(R.id.rv_recent_bills);
        tvEmpty = view.findViewById(R.id.tv_empty);
    }

    private void initData() {
        mBillRepository = BillRepository.getInstance(requireContext());
        mDecimalFormat = new DecimalFormat("0.00");

        initDateFilter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvRecentBills.setLayoutManager(layoutManager);

        mAdapter = new RecentBillAdapter(requireContext(), new ArrayList<>());
        rvRecentBills.setAdapter(mAdapter);
    }

    private void initDateFilter() {
        List<String> dateOptions = new ArrayList<>();
        dateOptions.add("最近1天");
        dateOptions.add("最近3天");
        dateOptions.add("最近7天");
        dateOptions.add("最近30天");

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                dateOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDateFilter.setAdapter(adapter);

        spDateFilter.setSelection(getPositionForDays(mCurrentDays));

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

    private int getPositionForDays(int days) {
        switch (days) {
            case 1:
                return 0;
            case 3:
                return 1;
            case 7:
                return 2;
            case 30:
                return 3;
            default:
                return 2;
        }
    }

    private void setListeners() {
        btnRecordIncome.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BillEditActivity.class);
            intent.putExtra("bill_type", 1);
            mEditBillLauncher.launch(intent);
        });

        btnRecordExpense.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BillEditActivity.class);
            intent.putExtra("bill_type", 0);
            mEditBillLauncher.launch(intent);
        });

        btnAllBills.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BillListActivity.class);
            startActivity(intent);
        });
    }

    private void registerActivityResult() {
        mEditBillLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        loadData();
                    }
                }
        );
    }

    private void loadData() {
        Log.d(TAG, "loadData: 开始加载数据");
        loadTodayStatistics();
        loadRecentBills();
    }

    private void loadTodayStatistics() {
        Log.d(TAG, "loadTodayStatistics: 开始加载当日统计数据");
        if (mStatisticsRequestId != null) {
            Log.d(TAG, "loadTodayStatistics: 取消之前的统计请求，ID=" + mStatisticsRequestId);
            mBillRepository.cancelRequest(mStatisticsRequestId);
        }
        String todayDate = mBillRepository.getTodayDate();
        mStatisticsRequestId = mBillRepository.calculateTodayStatisticsAsync(todayDate, new BillRepository.Callback<BillRepository.TodayStatistics>() {
            @Override
            public void onSuccess(BillRepository.TodayStatistics statistics) {
                if (isAdded() && getView() != null) {
                    Log.d(TAG, "loadTodayStatistics: 统计数据加载成功，收入=" + statistics.getIncome() + "，支出=" + statistics.getExpense() + "，结余=" + statistics.getBalance());
                    requireActivity().runOnUiThread(() -> {
                        tvMonthIncome.setText("当日收入：" + mDecimalFormat.format(statistics.getIncome()) + "元");
                        tvMonthExpense.setText("当日支出：" + mDecimalFormat.format(statistics.getExpense()) + "元");

                        if (statistics.getBalance() >= 0) {
                            tvMonthBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            tvMonthBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                        tvMonthBalance.setText("当日结余：" + mDecimalFormat.format(statistics.getBalance()) + "元");
                    });
                } else {
                    Log.w(TAG, "loadTodayStatistics: Fragment已销毁或视图不可用，跳过UI更新");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadTodayStatistics: 加载当日统计数据失败: " + e.getMessage());
            }
        });
        Log.d(TAG, "loadTodayStatistics: 统计请求已提交，ID=" + mStatisticsRequestId);
    }

    private void loadRecentBills() {
        Log.d(TAG, "loadRecentBills: 开始加载最近" + mCurrentDays + "天的账单");
        if (mBillsRequestId != null) {
            Log.d(TAG, "loadRecentBills: 取消之前的账单请求，ID=" + mBillsRequestId);
            mBillRepository.cancelRequest(mBillsRequestId);
        }
        mBillsRequestId = mBillRepository.getBillsByDaysAsync(mCurrentDays, new BillRepository.Callback<List<Bill>>() {
            @Override
            public void onSuccess(List<Bill> recentBills) {
                if (isAdded() && getView() != null) {
                    Log.d(TAG, "loadRecentBills: 账单数据加载成功，数量=" + recentBills.size());
                    requireActivity().runOnUiThread(() -> {
                        mAdapter.updateData(recentBills);

                        if (recentBills.isEmpty()) {
                            rvRecentBills.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            rvRecentBills.setVisibility(View.VISIBLE);
                            tvEmpty.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Log.w(TAG, "loadRecentBills: Fragment已销毁或视图不可用，跳过UI更新");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadRecentBills: 加载近期账单失败: " + e.getMessage());
            }
        });
        Log.d(TAG, "loadRecentBills: 账单请求已提交，ID=" + mBillsRequestId);
    }

    private void cancelPendingRequests() {
        Log.d(TAG, "cancelPendingRequests: 取消待处理的异步请求");
        if (mStatisticsRequestId != null) {
            Log.d(TAG, "cancelPendingRequests: 取消统计请求，ID=" + mStatisticsRequestId);
            mBillRepository.cancelRequest(mStatisticsRequestId);
            mStatisticsRequestId = null;
        }
        if (mBillsRequestId != null) {
            Log.d(TAG, "cancelPendingRequests: 取消账单请求，ID=" + mBillsRequestId);
            mBillRepository.cancelRequest(mBillsRequestId);
            mBillsRequestId = null;
        }
    }

    private void cleanupViews() {
        Log.d(TAG, "cleanupViews: 清理视图引用");
        tvMonthIncome = null;
        tvMonthExpense = null;
        tvMonthBalance = null;
        btnRecordIncome = null;
        btnRecordExpense = null;
        btnAllBills = null;
        spDateFilter = null;
        rvRecentBills = null;
        tvEmpty = null;
    }

    public void refreshData() {
        if (isAdded() && getView() != null) {
            Log.d(TAG, "refreshData: 刷新数据");
            loadData();
        } else {
            Log.w(TAG, "refreshData: Fragment已销毁或视图不可用，跳过刷新");
        }
    }
}
