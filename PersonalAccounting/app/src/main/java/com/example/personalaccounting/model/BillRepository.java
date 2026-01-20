package com.example.personalaccounting.model;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 导入同包下的Bill类

/**
 * 账单仓库类
 * <p>
 * 这是MVC架构中的Model层核心类，封装了所有与账单相关的业务逻辑
 * 负责处理数据获取、业务计算和数据存储等操作
 * 隔离了View层（Activity）与底层数据库操作
 * </p>
 */
public class BillRepository {
    private static final String TAG = "BillRepository";
    private final BillDbHelper mDbHelper;
    private final ExecutorService mExecutorService;

    /**
     * 回调接口：处理异步操作结果
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    /**
     * 构造方法
     *
     * @param context 上下文对象
     */
    public BillRepository(Context context) {
        mDbHelper = new BillDbHelper(context);
        // 创建固定大小的线程池，用于处理数据库异步操作
        mExecutorService = Executors.newFixedThreadPool(2);
    }

    /**
     * 获取今日日期字符串（yyyy-MM-dd格式）
     *
     * @return 今日日期字符串
     */
    public String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * 获取当前月份字符串（yyyy-MM格式）
     *
     * @return 当前月份字符串
     */
    public String getCurrentMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * 计算今日统计数据（异步方法）
     *
     * @param todayDate 今日日期
     * @param callback 回调接口，用于返回统计结果
     */
    public void calculateTodayStatisticsAsync(String todayDate, Callback<TodayStatistics> callback) {
        mExecutorService.execute(() -> {
            try {
                TodayStatistics statistics = calculateTodayStatistics(todayDate);
                callback.onSuccess(statistics);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 计算今日统计数据（同步方法）
     *
     * @param todayDate 今日日期
     * @return 今日统计结果
     */
    public TodayStatistics calculateTodayStatistics(String todayDate) {
        TodayStatistics statistics = new TodayStatistics();
        try {
            // 查询今日账单
            List<Bill> todayBills = mDbHelper.queryTodayBill(todayDate);

            // 统计收入和支出
            double todayIncome = 0.0;
            double todayExpense = 0.0;

            for (Bill bill : todayBills) {
                if (bill.getBillType() == 1) {
                    todayIncome += bill.getAmount();
                } else {
                    todayExpense += bill.getAmount();
                }
            }

            // 设置统计结果
            statistics.setIncome(todayIncome);
            statistics.setExpense(todayExpense);
            statistics.setBalance(todayIncome - todayExpense);

        } catch (Exception e) {
            Log.e(TAG, "计算今日统计数据失败: " + e.getMessage());
        }

        return statistics;
    }

    /**
     * 计算本月统计数据（异步方法）
     *
     * @param month 月份字符串（yyyy-MM格式）
     * @param callback 回调接口，用于返回统计结果
     */
    public void calculateMonthStatisticsAsync(String month, Callback<MonthStatistics> callback) {
        mExecutorService.execute(() -> {
            try {
                MonthStatistics statistics = calculateMonthStatistics(month);
                callback.onSuccess(statistics);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 计算本月统计数据（同步方法）
     *
     * @param month 月份字符串（yyyy-MM格式）
     * @return 本月统计结果
     */
    public MonthStatistics calculateMonthStatistics(String month) {
        MonthStatistics statistics = new MonthStatistics();

        try {
            // 查询本月账单
            List<Bill> monthBills = mDbHelper.queryMonthBill(month);

            // 统计收入和支出
            double monthIncome = 0.0;
            double monthExpense = 0.0;

            for (Bill bill : monthBills) {
                if (bill.getBillType() == 1) {
                    monthIncome += bill.getAmount();
                } else {
                    monthExpense += bill.getAmount();
                }
            }

            // 设置统计结果
            statistics.setIncome(monthIncome);
            statistics.setExpense(monthExpense);
            statistics.setBalance(monthIncome - monthExpense);

        } catch (Exception e) {
            Log.e(TAG, "计算本月统计数据失败: " + e.getMessage());
        }

        return statistics;
    }

    /**
     * 获取近期账单（近7条）- 异步方法
     *
     * @param callback 回调接口，用于返回账单列表
     */
    public void getRecentBillsAsync(Callback<List<Bill>> callback) {
        mExecutorService.execute(() -> {
            try {
                List<Bill> bills = getRecentBills();
                callback.onSuccess(bills);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 获取近期账单（近7条）
     *
     * @return 近期账单列表
     */
    public List<Bill> getRecentBills() {
        return getBillsByDays(7);
    }

    /**
     * 根据天数获取账单 - 异步方法
     *
     * @param days 天数
     * @param callback 回调接口，用于返回账单列表
     */
    public void getBillsByDaysAsync(int days, Callback<List<Bill>> callback) {
        mExecutorService.execute(() -> {
            try {
                List<Bill> bills = getBillsByDays(days);
                callback.onSuccess(bills);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 根据天数获取账单
     *
     * @param days 天数
     * @return 指定天数内的账单列表
     */
    public List<Bill> getBillsByDays(int days) {
        try {
            List<Bill> allBills = mDbHelper.queryAllBill();
            long currentTime = System.currentTimeMillis();
            long daysInMillis = days * 24 * 60 * 60 * 1000L;
            
            List<Bill> filteredBills = new java.util.ArrayList<>();
            for (Bill bill : allBills) {
                long billTime = bill.getCreateTime();
                if (currentTime - billTime <= daysInMillis) {
                    filteredBills.add(bill);
                }
            }
            
            return filteredBills;
        } catch (Exception e) {
            Log.e(TAG, "获取指定天数账单失败: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * 根据日期获取账单 - 异步方法
     *
     * @param date 日期字符串（yyyy-MM-dd格式）
     * @param callback 回调接口，用于返回账单列表
     */
    public void getBillsByDateAsync(String date, Callback<List<Bill>> callback) {
        mExecutorService.execute(() -> {
            try {
                List<Bill> bills = getBillsByDate(date);
                callback.onSuccess(bills);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 根据日期获取账单
     *
     * @param date 日期字符串（yyyy-MM-dd格式）
     * @return 指定日期的账单列表
     */
    public List<Bill> getBillsByDate(String date) {
        try {
            List<Bill> allBills = mDbHelper.queryAllBill();
            List<Bill> filteredBills = new java.util.ArrayList<>();
            
            for (Bill bill : allBills) {
                if (bill.getDate().equals(date)) {
                    filteredBills.add(bill);
                }
            }
            
            return filteredBills;
        } catch (Exception e) {
            Log.e(TAG, "获取指定日期账单失败: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * 获取所有账单 - 异步方法
     *
     * @param callback 回调接口，用于返回账单列表
     */
    public void getAllBillsAsync(Callback<List<Bill>> callback) {
        mExecutorService.execute(() -> {
            try {
                List<Bill> bills = getAllBills();
                callback.onSuccess(bills);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 获取所有账单
     *
     * @return 所有账单列表
     */
    public List<Bill> getAllBills() {
        try {
            return mDbHelper.queryAllBill();
        } catch (Exception e) {
            Log.e(TAG, "获取所有账单失败: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * 根据账单类型获取账单列表 - 异步方法
     *
     * @param billType 账单类型：0=支出，1=收入
     * @param callback 回调接口，用于返回账单列表
     */
    public void getBillsByTypeAsync(int billType, Callback<List<Bill>> callback) {
        mExecutorService.execute(() -> {
            try {
                List<Bill> bills = getBillsByType(billType);
                callback.onSuccess(bills);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 根据账单类型获取账单列表
     *
     * @param billType 账单类型：0=支出，1=收入
     * @return 过滤后的账单列表
     */
    public List<Bill> getBillsByType(int billType) {
        try {
            return mDbHelper.queryBillByType(billType);
        } catch (Exception e) {
            Log.e(TAG, "根据类型获取账单失败: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * 添加新账单 - 异步方法
     *
     * @param bill 要添加的账单对象
     * @param callback 回调接口，用于返回添加结果
     */
    public void addBillAsync(Bill bill, Callback<Boolean> callback) {
        mExecutorService.execute(() -> {
            try {
                boolean result = addBill(bill);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 添加新账单
     *
     * @param bill 要添加的账单对象
     * @return 添加结果：true=成功，false=失败
     */
    public boolean addBill(Bill bill) {
        try {
            return mDbHelper.insertBill(bill);
        } catch (Exception e) {
            Log.e(TAG, "添加账单失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新账单 - 异步方法
     *
     * @param bill 要更新的账单对象（必须包含有效的ID）
     * @param callback 回调接口，用于返回更新结果
     */
    public void updateBillAsync(Bill bill, Callback<Boolean> callback) {
        mExecutorService.execute(() -> {
            try {
                boolean result = updateBill(bill);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 更新账单
     *
     * @param bill 要更新的账单对象（必须包含有效的ID）
     * @return 更新结果：true=成功，false=失败
     */
    public boolean updateBill(Bill bill) {
        try {
            return mDbHelper.updateBill(bill);
        } catch (Exception e) {
            Log.e(TAG, "更新账单失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除账单 - 异步方法
     *
     * @param billId 要删除的账单ID
     * @param callback 回调接口，用于返回删除结果
     */
    public void deleteBillAsync(int billId, Callback<Boolean> callback) {
        mExecutorService.execute(() -> {
            try {
                boolean result = deleteBill(billId);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 删除账单
     *
     * @param billId 要删除的账单ID
     * @return 删除结果：true=成功，false=失败
     */
    public boolean deleteBill(int billId) {
        try {
            return mDbHelper.deleteBill(billId);
        } catch (Exception e) {
            Log.e(TAG, "删除账单失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 根据ID查询账单 - 异步方法
     *
     * @param billId 要查询的账单ID
     * @param callback 回调接口，用于返回查询结果
     */
    public void getBillByIdAsync(int billId, Callback<Bill> callback) {
        mExecutorService.execute(() -> {
            try {
                Bill bill = getBillById(billId);
                callback.onSuccess(bill);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * 根据ID查询账单
     *
     * @param billId 要查询的账单ID
     * @return 查询到的账单对象，如果不存在则返回null
     */
    public Bill getBillById(int billId) {
        try {
            return mDbHelper.queryBillById(billId);
        } catch (Exception e) {
            Log.e(TAG, "根据ID查询账单失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 关闭数据库连接和线程池
     */
    public void close() {
        mDbHelper.close();
        mExecutorService.shutdown();
    }

    /**
     * 今日统计数据类
     * 用于封装今日收入、支出和结余信息
     */
    public static class TodayStatistics {
        private double income;
        private double expense;
        private double balance;

        public double getIncome() {
            return income;
        }

        public void setIncome(double income) {
            this.income = income;
        }

        public double getExpense() {
            return expense;
        }

        public void setExpense(double expense) {
            this.expense = expense;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }

    /**
     * 本月统计数据类
     * 用于封装本月收入、支出和结余信息
     */
    public static class MonthStatistics {
        private double income;
        private double expense;
        private double balance;

        public double getIncome() {
            return income;
        }

        public void setIncome(double income) {
            this.income = income;
        }

        public double getExpense() {
            return expense;
        }

        public void setExpense(double expense) {
            this.expense = expense;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }
}

