package com.example.personalaccounting.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.personalaccounting.R;
import com.example.personalaccounting.model.Bill;

/**
 * 首页近期账单适配器
 * 用于RecyclerView展示近7条账单
 */
public class RecentBillAdapter extends RecyclerView.Adapter<RecentBillAdapter.BillViewHolder> {

    private Context mContext;
    private AsyncListDiffer<Bill> mDiffer;
    private DecimalFormat mDecimalFormat;

    /**
     * DiffUtil.Callback实现类
     * 用于计算新旧数据集的差异
     */
    private static final DiffUtil.ItemCallback<Bill> DIFF_CALLBACK = new DiffUtil.ItemCallback<Bill>() {
        @Override
        public boolean areItemsTheSame(@NonNull Bill oldItem, @NonNull Bill newItem) {
            // 判断是否是同一个item（通过ID判断）
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Bill oldItem, @NonNull Bill newItem) {
            // 判断item内容是否相同
            return oldItem.getType().equals(newItem.getType())
                    && oldItem.getAmount() == newItem.getAmount()
                    && oldItem.getBillType() == newItem.getBillType()
                    && oldItem.getDate().equals(newItem.getDate());
        }
    };

    /**
     * 构造方法
     * @param context 上下文
     * @param billList 账单列表数据
     */
    public RecentBillAdapter(Context context, List<Bill> billList) {
        this.mContext = context;
        // 初始化金额格式化器，保留2位小数
        this.mDecimalFormat = new DecimalFormat("0.00");
        // 初始化AsyncListDiffer
        this.mDiffer = new AsyncListDiffer<>(this, DIFF_CALLBACK);
        // 设置初始数据
        if (billList != null) {
            mDiffer.submitList(billList);
        }
    }

    /**
     * 创建ViewHolder
     * @param parent 父布局
     * @param viewType 视图类型
     * @return BillViewHolder实例
     */
    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载item布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recent_bill, parent, false);
        return new BillViewHolder(view);
    }

    /**
     * 绑定ViewHolder数据
     * @param holder ViewHolder实例
     * @param position 位置
     */
    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        // 获取当前位置的账单数据
        Bill bill = mDiffer.getCurrentList().get(position);

        // 设置账单类型
        holder.tvBillType.setText(bill.getType());

        // 根据账单类型名称设置图标
        int iconRes = getIconForBillType(bill.getType());
        holder.ivBillIcon.setImageResource(iconRes);

        // 设置金额，收入标绿色，支出标红色
        String amountStr = mDecimalFormat.format(bill.getAmount());
        if (bill.getBillType() == 1) {
            // 收入
            holder.tvBillAmount.setTextColor(mContext.getResources().getColor(android.R.color.holo_green_dark));
            holder.tvBillAmount.setText("+" + amountStr + "元");
        } else {
            // 支出
            holder.tvBillAmount.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_dark));
            holder.tvBillAmount.setText("-" + amountStr + "元");
        }

        // 设置账单日期（直接显示创建的原始日期）
        holder.tvBillDate.setText(bill.getDate());
    }

    /**
     * 根据账单类型名称获取对应的图标资源ID
     * @param billType 账单类型名称
     * @return 图标资源ID
     */
    private int getIconForBillType(String billType) {
        if (billType == null) {
            return R.drawable.ic_bill_type;
        }

        switch (billType) {
            // 收入类型
            case "工资":
                return R.drawable.ic_salary;
            case "理财":
                return R.drawable.ic_finance;
            case "兼职":
                return R.drawable.ic_part_time;
            case "其他":
                return R.drawable.ic_bill_type;
            // 支出类型
            case "餐饮":
                return R.drawable.ic_food;
            case "水果":
                return R.drawable.ic_fruit;
            case "零食":
                return R.drawable.ic_snack;
            case "美妆":
                return R.drawable.ic_cosmetics;
            case "购物":
                return R.drawable.ic_shopping;
            case "交通":
                return R.drawable.ic_transport;
            case "娱乐":
                return R.drawable.ic_entertainment;
            default:
                return R.drawable.ic_bill_type;
        }
    }

    /**
     * 根据账单日期动态生成显示文本（今天、昨天等）
     * @param dateStr 账单日期字符串（yyyy-MM-dd格式）
     * @return 格式化后的日期文本
     */
    private String formatBillDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date billDate = inputFormat.parse(dateStr);

            Calendar billCalendar = Calendar.getInstance();
            billCalendar.setTime(billDate);

            Calendar todayCalendar = Calendar.getInstance();

            int billYear = billCalendar.get(Calendar.YEAR);
            int billMonth = billCalendar.get(Calendar.MONTH);
            int billDay = billCalendar.get(Calendar.DAY_OF_MONTH);

            int todayYear = todayCalendar.get(Calendar.YEAR);
            int todayMonth = todayCalendar.get(Calendar.MONTH);
            int todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH);

            if (billYear == todayYear && billMonth == todayMonth && billDay == todayDay) {
                return "今天";
            }

            Calendar yesterdayCalendar = Calendar.getInstance();
            yesterdayCalendar.add(Calendar.DAY_OF_MONTH, -1);
            int yesterdayYear = yesterdayCalendar.get(Calendar.YEAR);
            int yesterdayMonth = yesterdayCalendar.get(Calendar.MONTH);
            int yesterdayDay = yesterdayCalendar.get(Calendar.DAY_OF_MONTH);

            if (billYear == yesterdayYear && billMonth == yesterdayMonth && billDay == yesterdayDay) {
                return "昨天";
            }

            Calendar tomorrowCalendar = Calendar.getInstance();
            tomorrowCalendar.add(Calendar.DAY_OF_MONTH, 1);
            int tomorrowYear = tomorrowCalendar.get(Calendar.YEAR);
            int tomorrowMonth = tomorrowCalendar.get(Calendar.MONTH);
            int tomorrowDay = tomorrowCalendar.get(Calendar.DAY_OF_MONTH);

            if (billYear == tomorrowYear && billMonth == tomorrowMonth && billDay == tomorrowDay) {
                return "明天";
            }

            if (billYear == todayYear && billMonth == todayMonth) {
                int dayDiff = billDay - todayDay;
                if (dayDiff > 0) {
                    return dayDiff + "天后";
                } else if (dayDiff < 0) {
                    return Math.abs(dayDiff) + "天前";
                }
            }

            SimpleDateFormat outputFormat;
            if (billYear == todayYear) {
                outputFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
            } else {
                outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }

            return outputFormat.format(billDate);

        } catch (ParseException e) {
            return dateStr;
        }
    }

    /**
     * 获取数据数量
     * @return 数据数量
     */
    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    /**
     * 更新数据
     * @param billList 新的账单列表数据
     */
    public void updateData(List<Bill> billList) {
        // 使用AsyncListDiffer提交新数据，自动计算差异并局部刷新
        mDiffer.submitList(billList);
    }

    /**
     * ViewHolder类
     * 用于缓存item中的视图
     */
    static class BillViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBillIcon; // 账单图标
        TextView tvBillType; // 账单类型
        TextView tvBillDate; // 账单日期
        TextView tvBillAmount; // 账单金额

        /**
         * 构造方法
         * @param itemView item视图
         */
        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            // 绑定视图
            ivBillIcon = itemView.findViewById(R.id.iv_bill_icon);
            tvBillType = itemView.findViewById(R.id.tv_bill_type);
            tvBillDate = itemView.findViewById(R.id.tv_bill_date);
            tvBillAmount = itemView.findViewById(R.id.tv_bill_amount);
        }
    }
}
