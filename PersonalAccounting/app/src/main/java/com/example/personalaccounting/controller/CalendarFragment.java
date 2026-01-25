package com.example.personalaccounting.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalaccounting.R;
import com.example.personalaccounting.model.Bill;
import com.example.personalaccounting.model.BillRepository;
import com.example.personalaccounting.view.RecentBillAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private static final String TAG = "CalendarFragment";
    private static final String KEY_SELECTED_DATE = "selected_date";

    private CalendarView calendarView;
    private RecyclerView rvBills;
    private TextView tvEmpty;
    private BillRepository mBillRepository;
    private RecentBillAdapter mAdapter;
    private SimpleDateFormat mDateFormat;
    private String mSelectedDate;
    private String mDateRequestId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: 开始创建日历视图");
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        initViews(view);
        Log.d(TAG, "onCreateView: 日历视图创建完成");
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
        if (mSelectedDate != null) {
            outState.putString(KEY_SELECTED_DATE, mSelectedDate);
            Log.d(TAG, "onSaveInstanceState: 保存状态，选中日期=" + mSelectedDate);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedDate = savedInstanceState.getString(KEY_SELECTED_DATE);
            Log.d(TAG, "onViewStateRestored: 恢复状态，选中日期=" + mSelectedDate);
            if (mSelectedDate != null && calendarView != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    java.util.Date date = sdf.parse(mSelectedDate);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendarView.setDate(calendar.getTimeInMillis(), false, true);
                    Log.d(TAG, "onViewStateRestored: 日历视图日期已恢复");
                } catch (Exception e) {
                    Log.e(TAG, "onViewStateRestored: 恢复日期失败: " + e.getMessage());
                }
            }
        }
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendar_view);
        rvBills = view.findViewById(R.id.rv_bills);
        tvEmpty = view.findViewById(R.id.tv_empty);
    }

    private void initData() {
        mBillRepository = BillRepository.getInstance(requireContext());
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvBills.setLayoutManager(layoutManager);

        mAdapter = new RecentBillAdapter(requireContext(), new ArrayList<>());
        rvBills.setAdapter(mAdapter);

        if (mSelectedDate == null) {
            Calendar calendar = Calendar.getInstance();
            mSelectedDate = mDateFormat.format(calendar.getTime());
        }
    }

    private void setListeners() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            mSelectedDate = mDateFormat.format(calendar.getTime());
            Log.d(TAG, "setListeners: 用户选择日期=" + mSelectedDate);
            loadBillsForDate(mSelectedDate);
        });
    }

    private void loadData() {
        Log.d(TAG, "loadData: 开始加载数据，当前日期=" + mSelectedDate);
        loadBillsForDate(mSelectedDate);
    }

    private void loadBillsForDate(String date) {
        Log.d(TAG, "loadBillsForDate: 开始加载日期=" + date + "的账单");
        if (mDateRequestId != null) {
            Log.d(TAG, "loadBillsForDate: 取消之前的请求，ID=" + mDateRequestId);
            mBillRepository.cancelRequest(mDateRequestId);
        }
        mDateRequestId = mBillRepository.getBillsByDateAsync(date, new BillRepository.Callback<List<Bill>>() {
            @Override
            public void onSuccess(List<Bill> bills) {
                if (isAdded() && getView() != null) {
                    Log.d(TAG, "loadBillsForDate: 账单数据加载成功，数量=" + bills.size());
                    requireActivity().runOnUiThread(() -> {
                        mAdapter.updateData(bills);

                        if (bills.isEmpty()) {
                            rvBills.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            rvBills.setVisibility(View.VISIBLE);
                            tvEmpty.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Log.w(TAG, "loadBillsForDate: Fragment已销毁或视图不可用，跳过UI更新");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadBillsForDate: 加载账单失败: " + e.getMessage());
            }
        });
        Log.d(TAG, "loadBillsForDate: 请求已提交，ID=" + mDateRequestId);
    }

    private void cancelPendingRequests() {
        Log.d(TAG, "cancelPendingRequests: 取消待处理的异步请求");
        if (mDateRequestId != null) {
            Log.d(TAG, "cancelPendingRequests: 取消请求，ID=" + mDateRequestId);
            mBillRepository.cancelRequest(mDateRequestId);
            mDateRequestId = null;
        }
    }

    private void cleanupViews() {
        Log.d(TAG, "cleanupViews: 清理视图引用");
        calendarView = null;
        rvBills = null;
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
