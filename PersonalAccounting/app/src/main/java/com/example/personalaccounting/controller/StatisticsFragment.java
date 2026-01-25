package com.example.personalaccounting.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalaccounting.R;
import com.example.personalaccounting.model.BillRepository;
import com.example.personalaccounting.model.CategoryStatistics;
import com.example.personalaccounting.view.CategoryStatisticsAdapter;
import com.example.personalaccounting.view.DateSelectorView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {
    private static final String TAG = "StatisticsFragment";
    private static final String KEY_TYPE_FILTER = "type_filter";

    private DateSelectorView dateSelector;
    private Spinner spTypeFilter;
    private TextView tvIncome;
    private TextView tvExpense;
    private TextView tvBalance;
    private RecyclerView rvCategoryStatistics;
    private TextView tvEmpty;

    private BillRepository mBillRepository;
    private CategoryStatisticsAdapter mAdapter;
    private DecimalFormat mDecimalFormat;
    private SimpleDateFormat mDateFormat;

    private int mCurrentTypeFilter = 0;
    private String mCurrentDateValue = "";
    private int mCurrentDimension = 2;

    private String mStatisticsRequestId;
    private String mCategoryRequestId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: 开始创建统计视图");
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        initViews(view);
        Log.d(TAG, "onCreateView: 统计视图创建完成");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: 视图创建完成，开始初始化数据");
        initData();
        setListeners();
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
        outState.putInt(KEY_TYPE_FILTER, mCurrentTypeFilter);
        Log.d(TAG, "onSaveInstanceState: 保存状态，类型筛选=" + mCurrentTypeFilter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentTypeFilter = savedInstanceState.getInt(KEY_TYPE_FILTER, 0);
            Log.d(TAG, "onViewStateRestored: 恢复状态，类型筛选=" + mCurrentTypeFilter);
            if (spTypeFilter != null) {
                spTypeFilter.setSelection(mCurrentTypeFilter);
            }
        }
    }

    private void initViews(View view) {
        Log.d(TAG, "initViews: 初始化视图组件");
        dateSelector = view.findViewById(R.id.date_selector);
        spTypeFilter = view.findViewById(R.id.sp_type_filter);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        tvBalance = view.findViewById(R.id.tv_balance);
        rvCategoryStatistics = view.findViewById(R.id.rv_category_statistics);
        tvEmpty = view.findViewById(R.id.tv_empty);
    }

    private void initData() {
        Log.d(TAG, "initData: 初始化数据");
        mBillRepository = BillRepository.getInstance(requireContext());
        mDecimalFormat = new DecimalFormat("0.00");
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        initTypeFilter();
        initDateSelector();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvCategoryStatistics.setLayoutManager(layoutManager);

        mAdapter = new CategoryStatisticsAdapter(requireContext(), new ArrayList<>());
        rvCategoryStatistics.setAdapter(mAdapter);
    }

    private void initDateSelector() {
        dateSelector.setOnDateChangeListener(new DateSelectorView.OnDateChangeListener() {
            @Override
            public void onDateChanged(int dimension, String dateValue) {
                Log.d(TAG, "onDateChanged: 日期改变，维度=" + dimension + "，日期=" + dateValue);
                mCurrentDimension = dimension;
                mCurrentDateValue = dateValue;
                loadData();
            }
        });
        mCurrentDateValue = dateSelector.getCurrentDateValue();
    }

    private void initTypeFilter() {
        List<String> typeOptions = new ArrayList<>();
        typeOptions.add("全部");
        typeOptions.add("收入");
        typeOptions.add("支出");

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                typeOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTypeFilter.setAdapter(adapter);

        spTypeFilter.setSelection(mCurrentTypeFilter);
    }

    private void setListeners() {
        spTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentTypeFilter != position) {
                    Log.d(TAG, "setListeners: 类型筛选改变，位置=" + position);
                    mCurrentTypeFilter = position;
                    loadData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadData() {
        Log.d(TAG, "loadData: 开始加载数据，维度=" + mCurrentDimension + "，日期=" + mCurrentDateValue + "，类型筛选=" + mCurrentTypeFilter);
        loadStatistics();
        loadCategoryStatistics();
    }

    private void loadStatistics() {
        if (mStatisticsRequestId != null) {
            Log.d(TAG, "loadStatistics: 取消之前的统计请求，ID=" + mStatisticsRequestId);
            mBillRepository.cancelRequest(mStatisticsRequestId);
        }

        switch (mCurrentDimension) {
            case 1:
                loadWeekStatistics();
                break;
            case 2:
                loadMonthStatistics();
                break;
            case 3:
                loadYearStatistics();
                break;
        }
    }

    private void loadWeekStatistics() {
        String[] dates = mCurrentDateValue.split(",");
        if (dates.length != 2) {
            Log.e(TAG, "loadWeekStatistics: 日期格式错误，dateValue=" + mCurrentDateValue);
            return;
        }
        String startDate = dates[0];
        String endDate = dates[1];

        Log.d(TAG, "loadWeekStatistics: 加载周统计，开始=" + startDate + "，结束=" + endDate);
        mStatisticsRequestId = mBillRepository.calculateWeekStatisticsAsync(startDate, endDate, new BillRepository.Callback<BillRepository.WeekStatistics>() {
            @Override
            public void onSuccess(BillRepository.WeekStatistics statistics) {
                if (isAdded() && getView() != null) {
                    Log.d(TAG, "loadWeekStatistics: 统计数据加载成功，收入=" + statistics.getIncome() + "，支出=" + statistics.getExpense() + "，结余=" + statistics.getBalance());
                    requireActivity().runOnUiThread(() -> {
                        updateStatisticsUI(statistics.getIncome(), statistics.getExpense(), statistics.getBalance());
                    });
                } else {
                    Log.w(TAG, "loadWeekStatistics: Fragment已销毁或视图不可用，跳过UI更新");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadWeekStatistics: 加载周统计失败: " + e.getMessage());
            }
        });
    }

    private void loadMonthStatistics() {
        Log.d(TAG, "loadMonthStatistics: 加载月统计，月份=" + mCurrentDateValue);
        mStatisticsRequestId = mBillRepository.calculateMonthStatisticsAsync(mCurrentDateValue, new BillRepository.Callback<BillRepository.MonthStatistics>() {
            @Override
            public void onSuccess(BillRepository.MonthStatistics statistics) {
                if (isAdded() && getView() != null) {
                    Log.d(TAG, "loadMonthStatistics: 统计数据加载成功，收入=" + statistics.getIncome() + "，支出=" + statistics.getExpense() + "，结余=" + statistics.getBalance());
                    requireActivity().runOnUiThread(() -> {
                        updateStatisticsUI(statistics.getIncome(), statistics.getExpense(), statistics.getBalance());
                    });
                } else {
                    Log.w(TAG, "loadMonthStatistics: Fragment已销毁或视图不可用，跳过UI更新");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadMonthStatistics: 加载月统计失败: " + e.getMessage());
            }
        });
    }

    private void loadYearStatistics() {
        Log.d(TAG, "loadYearStatistics: 加载年统计，年份=" + mCurrentDateValue);
        mStatisticsRequestId = mBillRepository.calculateYearStatisticsAsync(mCurrentDateValue, new BillRepository.Callback<BillRepository.YearStatistics>() {
            @Override
            public void onSuccess(BillRepository.YearStatistics statistics) {
                if (isAdded() && getView() != null) {
                    Log.d(TAG, "loadYearStatistics: 统计数据加载成功，收入=" + statistics.getIncome() + "，支出=" + statistics.getExpense() + "，结余=" + statistics.getBalance());
                    requireActivity().runOnUiThread(() -> {
                        updateStatisticsUI(statistics.getIncome(), statistics.getExpense(), statistics.getBalance());
                    });
                } else {
                    Log.w(TAG, "loadYearStatistics: Fragment已销毁或视图不可用，跳过UI更新");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadYearStatistics: 加载年统计失败: " + e.getMessage());
            }
        });
    }

    private void updateStatisticsUI(double income, double expense, double balance) {
        tvIncome.setText(mDecimalFormat.format(income) + "元");
        tvExpense.setText(mDecimalFormat.format(expense) + "元");
        tvBalance.setText(mDecimalFormat.format(balance) + "元");

        if (balance >= 0) {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void loadCategoryStatistics() {
        if (mCategoryRequestId != null) {
            Log.d(TAG, "loadCategoryStatistics: 取消之前的分类统计请求，ID=" + mCategoryRequestId);
            mBillRepository.cancelRequest(mCategoryRequestId);
        }

        int billType = getBillTypeForFilter();
        if (billType == -1) {
            mAdapter.updateData(new ArrayList<>());
            tvEmpty.setVisibility(View.VISIBLE);
            rvCategoryStatistics.setVisibility(View.GONE);
            Log.d(TAG, "loadCategoryStatistics: 全部类型，不显示分类统计");
            return;
        }

        switch (mCurrentDimension) {
            case 1:
                loadWeekCategoryStatistics(billType);
                break;
            case 2:
                loadMonthCategoryStatistics(billType);
                break;
            case 3:
                loadYearCategoryStatistics(billType);
                break;
        }
    }

    private void loadWeekCategoryStatistics(int billType) {
        String[] dates = mCurrentDateValue.split(",");
        if (dates.length != 2) {
            Log.e(TAG, "loadWeekCategoryStatistics: 日期格式错误，dateValue=" + mCurrentDateValue);
            return;
        }
        String startDate = dates[0];
        String endDate = dates[1];

        Log.d(TAG, "loadWeekCategoryStatistics: 加载周分类统计，类型=" + billType);
        mCategoryRequestId = mBillRepository.getWeekCategoryStatisticsAsync(startDate, endDate, billType, new BillRepository.Callback<List<CategoryStatistics>>() {
            @Override
            public void onSuccess(List<CategoryStatistics> categoryList) {
                if (isAdded() && getView() != null) {
                    Log.d(TAG, "loadWeekCategoryStatistics: 分类统计加载成功，数量=" + categoryList.size());
                    requireActivity().runOnUiThread(() -> {
                        mAdapter.updateData(categoryList);
                        if (categoryList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvCategoryStatistics.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            rvCategoryStatistics.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    Log.w(TAG, "loadWeekCategoryStatistics: Fragment已销毁或视图不可用，跳过UI更新");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadWeekCategoryStatistics: 加载周分类统计失败: " + e.getMessage());
            }
        });
    }

    private void loadMonthCategoryStatistics(int billType) {
        Log.d(TAG, "loadMonthCategoryStatistics: 加载月分类统计，类型=" + billType);
        mCategoryRequestId = mBillRepository.getMonthCategoryStatisticsAsync(mCurrentDateValue, billType, new BillRepository.Callback<List<CategoryStatistics>>() {
            @Override
            public void onSuccess(List<CategoryStatistics> categoryList) {
                if (isAdded() && getView() != null) {
                    Log.d(TAG, "loadMonthCategoryStatistics: 分类统计加载成功，数量=" + categoryList.size());
                    requireActivity().runOnUiThread(() -> {
                        mAdapter.updateData(categoryList);
                        if (categoryList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvCategoryStatistics.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            rvCategoryStatistics.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    Log.w(TAG, "loadMonthCategoryStatistics: Fragment已销毁或视图不可用，跳过UI更新");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadMonthCategoryStatistics: 加载月分类统计失败: " + e.getMessage());
            }
        });
    }

    private void loadYearCategoryStatistics(int billType) {
        Log.d(TAG, "loadYearCategoryStatistics: 加载年分类统计，类型=" + billType);
        mCategoryRequestId = mBillRepository.getYearCategoryStatisticsAsync(mCurrentDateValue, billType, new BillRepository.Callback<List<CategoryStatistics>>() {
            @Override
            public void onSuccess(List<CategoryStatistics> categoryList) {
                if (isAdded() && getView() != null) {
                    Log.d(TAG, "loadYearCategoryStatistics: 分类统计加载成功，数量=" + categoryList.size());
                    requireActivity().runOnUiThread(() -> {
                        mAdapter.updateData(categoryList);
                        if (categoryList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvCategoryStatistics.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            rvCategoryStatistics.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    Log.w(TAG, "loadYearCategoryStatistics: Fragment已销毁或视图不可用，跳过UI更新");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadYearCategoryStatistics: 加载年分类统计失败: " + e.getMessage());
            }
        });
    }

    private int getBillTypeForFilter() {
        switch (mCurrentTypeFilter) {
            case 1:
                return 1;
            case 2:
                return 0;
            default:
                return -1;
        }
    }

    private void cancelPendingRequests() {
        Log.d(TAG, "cancelPendingRequests: 取消待处理的异步请求");
        if (mStatisticsRequestId != null) {
            Log.d(TAG, "cancelPendingRequests: 取消统计请求，ID=" + mStatisticsRequestId);
            mBillRepository.cancelRequest(mStatisticsRequestId);
            mStatisticsRequestId = null;
        }
        if (mCategoryRequestId != null) {
            Log.d(TAG, "cancelPendingRequests: 取消分类统计请求，ID=" + mCategoryRequestId);
            mBillRepository.cancelRequest(mCategoryRequestId);
            mCategoryRequestId = null;
        }
    }

    private void cleanupViews() {
        Log.d(TAG, "cleanupViews: 清理视图引用");
        dateSelector = null;
        spTypeFilter = null;
        tvIncome = null;
        tvExpense = null;
        tvBalance = null;
        rvCategoryStatistics = null;
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
