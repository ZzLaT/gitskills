package com.example.personalaccounting.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.personalaccounting.R;
import com.example.personalaccounting.model.Bill;
import com.example.personalaccounting.model.BillRepository;


/**
 * 记账编辑页面Activity
 * 用于记录收入或支出账单
 */
public class BillEditActivity extends AppCompatActivity{

    // 控件
    private TextInputLayout tilAmount; // 金额输入布局
    private EditText etAmount; // 金额输入框
    private TextInputLayout tilType; // 类型选择布局
    private Spinner spType; // 类型选择器
    private TextInputLayout tilRemark; // 备注输入布局
    private EditText etRemark; // 备注输入框
    private TextView tvDate; // 日期显示
    private MaterialButton btnCancel; // 取消按钮
    private MaterialButton btnSave; // 保存按钮
    private ImageButton btnBack; // 返回按钮

    // 数据
    private int mBillType; // 0=支出，1=收入
    private BillRepository mBillRepository; // 账单仓库（MVC的Model层）
    private String mSelectedType; // 选中的类型
    private String mTodayDate; // 今日日期
    private String mOriginalDate; // 编辑模式下的原日期
    private int mBillId; // 账单ID，编辑模式下有效
    private boolean mIsEditMode; // 是否为编辑模式

    // 类型选项数据
    private List<String> mIncomeTypes; // 收入类型列表
    private List<String> mExpenseTypes; // 支出类型列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局
        setContentView(R.layout.activity_bill_edit);

        // 初始化控件
        initViews();

        // 初始化数据
        initData();

        // 设置监听器
        setListeners();

        // 初始化类型选择器
        initTypeSpinner();
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        tilAmount = findViewById(R.id.til_amount);
        etAmount = findViewById(R.id.et_amount);
        tilType = findViewById(R.id.til_type);
        spType = findViewById(R.id.sp_type);
        tilRemark = findViewById(R.id.til_remark);
        etRemark = findViewById(R.id.et_remark);
        tvDate = findViewById(R.id.tv_date);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 获取传递的账单类型标记
        Intent intent = getIntent();
        mBillType = intent.getIntExtra("bill_type", 0);
        mBillId = intent.getIntExtra("bill_id", -1);
        mIsEditMode = (mBillId != -1);

        // 初始化账单仓库（MVC的Model层）
        mBillRepository = BillRepository.getInstance(this);

        // 初始化类型选项数据
        mIncomeTypes = new ArrayList<>();
        mIncomeTypes.add("工资");
        mIncomeTypes.add("理财");
        mIncomeTypes.add("兼职");
        mIncomeTypes.add("其他");

        mExpenseTypes = new ArrayList<>();
        mExpenseTypes.add("餐饮");
        mExpenseTypes.add("水果");
        mExpenseTypes.add("零食");
        mExpenseTypes.add("美妆");
        mExpenseTypes.add("购物");
        mExpenseTypes.add("交通");
        mExpenseTypes.add("娱乐");
        mExpenseTypes.add("其他");

        // 获取今日日期，格式：yyyy-MM-dd
        mTodayDate = mBillRepository.getTodayDate();

        // 设置日期显示
        tvDate.setText(mTodayDate);

        // 如果是编辑模式，加载账单信息
        if (mIsEditMode) {
            loadBillForEdit();
        }
    }

    /**
     * 设置监听器
     */
    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveBill());
        // 类型选择器监听器
        // 为 Spinner 组件设置选项选择监听器，当用户选择不同选项时触发相应的回调方法
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // parent.getItemAtPosition(position) 获取选中的类型字符串
                mSelectedType = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSelectedType = null;
            }
        });
    }

    /**
     * 初始化类型选择器
     */
    private void initTypeSpinner() {
        ArrayAdapter<String> adapter;

        if (mBillType == 1) {
            // 收入类型
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mIncomeTypes);
        } else {
            // 支出类型
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mExpenseTypes);
        }

        // 设置下拉样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 设置适配器
        spType.setAdapter(adapter);

        // 默认选中第一个
        if (adapter.getCount() > 0) {
            spType.setSelection(0);
            mSelectedType = adapter.getItem(0);
        }
    }

    /**
     * 验证表单数据
     * @return 是否验证通过
     */
    private boolean validateForm() {
        boolean isValid = true;

        // 验证金额
        String amountStr = etAmount.getText().toString().trim();
        //TextUtils.isEmpty(amountStr) 检查金额字符串是否为空或仅包含空格或null，null返回true
        if (TextUtils.isEmpty(amountStr)) {
            tilAmount.setError("请输入金额");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    tilAmount.setError("金额必须大于0");
                    isValid = false;
                } else {
                    tilAmount.setError(null);
                }
            } catch (NumberFormatException e) {
                tilAmount.setError("请输入有效的金额");
                isValid = false;
            }
        }

        // 验证类型
        if (TextUtils.isEmpty(mSelectedType)) {
            tilType.setError("请选择类型");
            isValid = false;
        } else {
            tilType.setError(null);
        }

        return isValid;
    }

    /**
     * 加载要编辑的账单信息
     */
    private void loadBillForEdit() {
        // 使用异步方法根据ID查询账单
        mBillRepository.getBillByIdAsync(mBillId, new BillRepository.Callback<Bill>() {
            @Override
            public void onSuccess(Bill bill) {
                // 在UI线程更新界面
                runOnUiThread(() -> {
                    if (bill != null) {
                        // 填充表单数据
                        etAmount.setText(String.valueOf(bill.getAmount()));
                        etRemark.setText(bill.getRemark());
                        tvDate.setText(bill.getDate());

                        // 保存原日期（编辑模式）
                        mOriginalDate = bill.getDate();

                        // 设置账单类型
                        mBillType = bill.getBillType();

                        // 重新初始化类型选择器并设置选中项
                        initTypeSpinner();

                        // 设置选中的类型
                        List<String> types = (mBillType == 1) ? mIncomeTypes : mExpenseTypes;
                        int index = types.indexOf(bill.getType());
                        if (index >= 0) {
                            spType.setSelection(index);
                            mSelectedType = bill.getType();
                        }
                    } else {
                        Toast.makeText(BillEditActivity.this, "账单不存在", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("BillEditActivity", "加载账单失败: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(BillEditActivity.this, "加载账单失败", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    /**
     * 保存账单数据（异步）
     */
    private void saveBill() {
        // 验证表单（Controller层的表单验证职责）
        if (!validateForm()) {
            return;
        }

        // 获取表单数据
        String amountStr = etAmount.getText().toString().trim();
        double amount = Double.parseDouble(amountStr);
        String remark = etRemark.getText().toString().trim();

        // 确定使用的日期：编辑模式使用原日期，新增模式使用当前日期
        String dateToSave;
        if (mIsEditMode) {
            // 编辑模式：使用原日期
            dateToSave = mOriginalDate;
        } else {
            // 新增模式：动态获取当前日期
            dateToSave = mBillRepository.getTodayDate();
        }

        // 创建Bill对象（数据封装）
        Bill bill = new Bill();
        bill.setType(mSelectedType);
        bill.setAmount(amount);
        bill.setBillType(mBillType);
        bill.setRemark(remark);
        bill.setDate(dateToSave);

        if (mIsEditMode) {
            // 编辑模式：设置ID和原创建时间
            bill.setId(mBillId);

            // 使用异步方法更新账单
            mBillRepository.updateBillAsync(bill, new BillRepository.Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean success) {
                    // 在UI线程更新界面
                    runOnUiThread(() -> {
                        if (success) {
                            // 更新成功，更新UI并返回
                            Toast.makeText(BillEditActivity.this, "更新成功", Toast.LENGTH_SHORT).show();

                            // 返回上一页，并通知刷新数据
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            // 更新失败
                            Toast.makeText(BillEditActivity.this, "更新失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e("BillEditActivity", "更新账单失败: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(BillEditActivity.this, "更新失败，请重试", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            // 新增模式
            bill.setCreateTime(System.currentTimeMillis());

            // 使用异步方法保存账单
            mBillRepository.addBillAsync(bill, new BillRepository.Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean success) {
                    // 在UI线程更新界面
                    runOnUiThread(() -> {
                        if (success) {
                            // 保存成功，更新UI并返回
                            Toast.makeText(BillEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                            // 返回上一页，并通知刷新数据
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            // 保存失败
                            Toast.makeText(BillEditActivity.this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e("BillEditActivity", "保存账单失败: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(BillEditActivity.this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 资源由BillRepository单例统一管理，无需在此关闭
    }
}