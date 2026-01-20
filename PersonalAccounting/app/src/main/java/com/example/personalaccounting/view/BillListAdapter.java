package com.example.personalaccounting.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

import com.example.personalaccounting.R;
import com.example.personalaccounting.model.Bill;


/**
 * 账单列表适配器类
 * <p>
 * 适配器是RecyclerView的核心组件之一，负责将数据列表转换为可视化的列表项
 * 本类继承自RecyclerView.Adapter，实现了数据与视图的绑定逻辑
 * </p>
 */
public class BillListAdapter extends RecyclerView.Adapter<BillListAdapter.BillViewHolder> {

    /**
     * 为什么需要Context？
     * BillListAdapter本身不继承自Activity/Fragment等Context相关类，
     * 所以无法直接访问系统资源和功能。通过外部传入Context并保存为成员变量，
     * 可以让Adapter获得完整的系统访问能力。
     */
    // 上下文对象，用于访问系统资源和组件
    private Context mContext;
    // 账单数据列表，存储要显示的所有账单信息
    private List<Bill> mBillList;
    // 金额格式化工具，用于将数字格式化为保留2位小数的字符串
    private DecimalFormat mDecimalFormat;

    /**
     * 构造方法，初始化适配器
     *
     * @param context  上下文对象，通常是Activity或Fragment
     * @param billList 要显示的账单数据列表
     */
    public BillListAdapter(Context context, List<Bill> billList) {
        this.mContext = context;
        this.mBillList = billList;
        // 初始化金额格式化器，"0.00"表示保留2位小数
        this.mDecimalFormat = new DecimalFormat("0.00");
    }

    /**
     * 创建ViewHolder实例
     * <p>
     * 当RecyclerView需要一个新的列表项视图时调用此方法
     * 负责加载列表项的布局文件并创建ViewHolder
     * </p>
     *
     * @param parent   RecyclerView的父布局容器
     * @param viewType 视图类型（多布局时使用，本例子只有一种布局）
     * @return 创建好的BillViewHolder实例
     */
    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 从上下文获取LayoutInflater，用于加载XML布局文件
        // inflate参数说明：
        // 1. R.layout.item_bill_list：列表项的布局文件ID
        // 2. parent：父布局容器
        // 3. false：是否将加载的视图立即添加到父布局（通常为false，由RecyclerView管理）
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_bill_list, parent, false);
        // 创建并返回ViewHolder实例
        return new BillViewHolder(view);
    }

    /**
     * 绑定数据到ViewHolder
     * <p>
     * 当RecyclerView需要显示或刷新某个位置的数据时调用此方法
     * 负责将对应位置的数据填充到ViewHolder的视图中
     * </p>
     *
     * @param holder   要绑定数据的ViewHolder实例
     * @param position 当前列表项在数据列表中的位置
     */
    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        // 获取当前位置的账单数据
        Bill bill = mBillList.get(position);

        // 设置账单类型（如"工资"、"餐饮"等）
        holder.tvBillType.setText(bill.getType());

        // 格式化金额为保留2位小数的字符串
        String amountStr = mDecimalFormat.format(bill.getAmount());

        // 根据账单类型设置不同颜色和符号：收入为绿色带+号，支出为红色带-号
        if (bill.getBillType() == 1) {
            // 收入类型（1表示收入）
            holder.tvBillAmount.setTextColor(mContext.getResources().getColor(android.R.color.holo_green_dark));
            holder.tvBillAmount.setText("+" + amountStr + "元");
        } else {
            // 支出类型（0表示支出）
            holder.tvBillAmount.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_dark));
            holder.tvBillAmount.setText("-" + amountStr + "元");
        }

        // 设置账单备注，如果备注为空则显示"无备注"
        String remark = bill.getRemark();
        if (remark != null && !remark.isEmpty()) {
            holder.tvBillRemark.setText(remark);
        } else {
            holder.tvBillRemark.setText("无备注");
        }

        // 设置账单日期
        holder.tvBillDate.setText(bill.getDate());

        // 为整个列表项设置点击事件 - 触发编辑功能
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBillActionListener != null) {
                    mOnBillActionListener.onEditBill(bill);
                }
            }
        });

        // 为整个列表项设置长按事件 - 触发删除功能
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 添加视觉反馈：震动
                v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);

                // 触发删除回调
                if (mOnBillActionListener != null) {
                    mOnBillActionListener.onDeleteBill(bill);
                }
                return true;
            }
        });
    }

    /**
     * 获取数据列表的长度
     * <p>
     * RecyclerView通过此方法了解列表的总数量，用于计算滚动范围等
     * </p>
     *
     * @return 数据列表的长度，如果列表为null则返回0
     */
    @Override
    public int getItemCount() {
        // 安全检查：如果列表为null则返回0，否则返回列表长度
        return mBillList != null ? mBillList.size() : 0;
    }

    /**
     * 更新适配器的数据
     * <p>
     * 当数据源发生变化时调用此方法，通知RecyclerView刷新视图
     * </p>
     *
     * @param billList 新的账单数据列表
     */
    public void updateData(List<Bill> billList) {
        // 更新数据源
        this.mBillList = billList;
        // 通知RecyclerView数据源已变化，需要刷新所有列表项
        notifyDataSetChanged();
    }

    /**
     * 账单操作监听器接口
     */
    public interface OnBillActionListener {
        /**
         * 编辑账单
         * @param bill 要编辑的账单
         */
        void onEditBill(Bill bill);

        /**
         * 删除账单
         * @param bill 要删除的账单
         */
        void onDeleteBill(Bill bill);
    }

    // 账单操作监听器
    //- new BillListAdapter.OnBillActionListener() {...} ：创建了一个 匿名内部类的实例
    //- 这个实例 实现了 OnBillActionListener 接口的所有方法（onEditBill和onDeleteBill）
    //- setOnBillActionListener 方法将这个 实例 赋值给 mOnBillActionListener 成员变量
    //- 赋值完成后， mOnBillActionListener 就指向了这个实现接口的实例
    private OnBillActionListener mOnBillActionListener;

    /**
     * 设置账单操作监听器
     * @param listener 监听器实例
     */
    public void setOnBillActionListener(OnBillActionListener listener) {
        this.mOnBillActionListener = listener;
    }

    /**
     * ViewHolder内部类
     * <p>
     * 用于缓存列表项中的视图组件，避免重复查找
     * RecyclerView通过ViewHolder机制提高性能
     * </p>
     */
    static class BillViewHolder extends RecyclerView.ViewHolder {
        // 账单类型文本视图
        TextView tvBillType;
        // 账单金额文本视图
        TextView tvBillAmount;
        // 账单备注文本视图
        TextView tvBillRemark;
        // 账单日期文本视图
        TextView tvBillDate;

        /**
         * ViewHolder构造方法
         *
         * @param itemView 列表项的根视图
         */
        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            // 通过findViewById查找列表项布局中的各个视图组件
            // R.id.tv_bill_type是布局文件中定义的视图ID
            tvBillType = itemView.findViewById(R.id.tv_bill_type);
            tvBillAmount = itemView.findViewById(R.id.tv_bill_amount);
            tvBillRemark = itemView.findViewById(R.id.tv_bill_remark);
            tvBillDate = itemView.findViewById(R.id.tv_bill_date);
        }
    }
}
