package com.example.personalaccounting.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.personalaccounting.R;
import com.example.personalaccounting.model.Bill;
import com.example.personalaccounting.model.BillRepository;
import com.example.personalaccounting.view.BillListAdapter;


public class BillListActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    // 控件
    private ImageButton btnBack; // 返回按钮
    private RadioGroup rgFilter; // 筛选RadioGroup
    private RadioButton rbAll; // 全部账单
    private RadioButton rbIncome; // 收入账单
    private RadioButton rbExpense; // 支出账单
    private RecyclerView rvBillList; // 账单列表
    private TextView tvEmpty; // 空数据提示

    // 数据
    private BillRepository mBillRepository; // 账单仓库（MVC的Model层）
    private BillListAdapter mAdapter; // 账单列表适配器
    private int mCurrentFilter; // 当前筛选条件：0=全部，1=收入，2=支出

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局
        setContentView(R.layout.activity_bill_list);

        // 初始化控件
        initViews();

        // 初始化数据
        initData();

        // 设置监听器
        setListeners();

        // 加载账单数据
        loadBillData();
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rgFilter = findViewById(R.id.rg_filter);
        rbAll = findViewById(R.id.rb_all);
        rbIncome = findViewById(R.id.rb_income);
        rbExpense = findViewById(R.id.rb_expense);
        rvBillList = findViewById(R.id.rv_bill_list);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化账单仓库（MVC的Model层）
        mBillRepository = new BillRepository(this);

        // 初始化当前筛选条件为全部
        mCurrentFilter = 0;

        // 设置RecyclerView的布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBillList.setLayoutManager(layoutManager);

        // 初始化适配器，传入空列表
        mAdapter = new BillListAdapter(this, new ArrayList<>());
        rvBillList.setAdapter(mAdapter);

        // 设置账单操作监听器
        mAdapter.setOnBillActionListener(new BillListAdapter.OnBillActionListener() {
            @Override
            public void onEditBill(Bill bill) {
                // 编辑账单：跳转到BillEditActivity
                Intent intent = new Intent(BillListActivity.this, BillEditActivity.class);
                intent.putExtra("bill_type", bill.getBillType());
                intent.putExtra("bill_id", bill.getId());
                startActivityForResult(intent, 1);
            }

            @Override
            public void onDeleteBill(Bill bill) {
                // 删除账单：显示确认对话框
                new AlertDialog.Builder(BillListActivity.this)
                        .setTitle("删除确认")
                        .setMessage("确定要删除这条账单吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 使用异步方法删除账单
                            mBillRepository.deleteBillAsync(bill.getId(), new BillRepository.Callback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean success) {
                                    // 在UI线程更新界面
                                    runOnUiThread(() -> {
                                        if (success) {
                                            Toast.makeText(BillListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                            // 重新加载账单数据
                                            loadBillData();
                                        } else {
                                            Toast.makeText(BillListActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("BillListActivity", "删除账单失败: " + e.getMessage());
                                    runOnUiThread(() -> {
                                        Toast.makeText(BillListActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
    }

    /**
     * 设置监听器
     */
    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        // 监听单选按钮组中选中项的变化事件
        rgFilter.setOnCheckedChangeListener(this);
    }

    /**
     * 加载账单数据（异步）
     */
    private void loadBillData() {
        // 根据筛选条件调用对应的异步方法
        BillRepository.Callback<List<Bill>> callback = new BillRepository.Callback<List<Bill>>() {
            @Override
            public void onSuccess(List<Bill> bills) {
                // 在UI线程更新界面
                runOnUiThread(() -> {
                    // 直接更新适配器数据，AsyncListDiffer会自动计算差异
                    mAdapter.updateData(bills);

                    // 显示/隐藏空数据提示（Controller层只负责UI控制）
                    if (bills == null || bills.isEmpty()) {
                        rvBillList.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvBillList.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("BillListActivity", "加载账单数据失败: " + e.getMessage());
            }
        };

        switch (mCurrentFilter) {
            case 1:
                // 收入
                mBillRepository.getBillsByTypeAsync(1, callback);
                break;
            case 2:
                // 支出
                mBillRepository.getBillsByTypeAsync(0, callback);
                break;
            default:
                // 全部
                mBillRepository.getAllBillsAsync(callback);
                break;
        }
    }

    /**
     * RadioGroup选中状态变化监听器
     * @param group RadioGroup对象
     * @param checkedId 选中的RadioButton的ID
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // 更新当前筛选条件
        if (checkedId == R.id.rb_all) {
            mCurrentFilter = 0;
        } else if (checkedId == R.id.rb_income) {
            mCurrentFilter = 1;
        } else if (checkedId == R.id.rb_expense) {
            mCurrentFilter = 2;
        }

        // 根据新的筛选条件重新加载数据
        loadBillData();
    }

    /**
     * 处理从其他Activity返回的结果
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 如果是从编辑页面返回且操作成功，重新加载数据
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadBillData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭账单仓库资源
        if (mBillRepository != null) {
            mBillRepository.close();
        }
    }
}