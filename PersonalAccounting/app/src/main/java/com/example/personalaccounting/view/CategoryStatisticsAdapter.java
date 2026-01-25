package com.example.personalaccounting.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalaccounting.R;
import com.example.personalaccounting.model.CategoryStatistics;

import java.text.DecimalFormat;
import java.util.List;

public class CategoryStatisticsAdapter extends RecyclerView.Adapter<CategoryStatisticsAdapter.ViewHolder> {
    private static final String TAG = "CategoryStatisticsAdapter";
    
    private Context mContext;
    private List<CategoryStatistics> mCategoryList;
    private DecimalFormat mDecimalFormat;
    private double mTotalAmount;

    public CategoryStatisticsAdapter(Context context, List<CategoryStatistics> categoryList) {
        mContext = context;
        mCategoryList = categoryList;
        mDecimalFormat = new DecimalFormat("0.00");
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        mTotalAmount = 0;
        for (CategoryStatistics category : mCategoryList) {
            mTotalAmount += category.getAmount();
        }
    }

    public void updateData(List<CategoryStatistics> categoryList) {
        mCategoryList = categoryList;
        calculateTotalAmount();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_category_statistics, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStatistics category = mCategoryList.get(position);
        
        holder.tvCategoryName.setText(category.getCategoryName());
        holder.tvCategoryAmount.setText(mDecimalFormat.format(category.getAmount()) + "元");
        holder.tvBillCount.setText("共" + category.getCount() + "笔");
        
        if (mTotalAmount > 0) {
            double percentage = (category.getAmount() / mTotalAmount) * 100;
            holder.tvPercentage.setText(mDecimalFormat.format(percentage) + "%");
        } else {
            holder.tvPercentage.setText("0%");
        }
    }

    @Override
    public int getItemCount() {
        return mCategoryList != null ? mCategoryList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        TextView tvCategoryAmount;
        TextView tvBillCount;
        TextView tvPercentage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryAmount = itemView.findViewById(R.id.tv_category_amount);
            tvBillCount = itemView.findViewById(R.id.tv_bill_count);
            tvPercentage = itemView.findViewById(R.id.tv_percentage);
        }
    }
}