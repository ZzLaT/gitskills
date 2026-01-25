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
    private static volatile BillRepository sInstance;
    private final BillDbHelper mDbHelper;
    private final ExecutorService mExecutorService;
    private final java.util.concurrent.atomic.AtomicInteger mRequestIdGenerator;
    private final java.util.concurrent.ConcurrentHashMap<String, Boolean> mPendingRequests;

    /**
     * 回调接口：处理异步操作结果
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    /**
     * 可取消的回调接口
     */
    public interface CancellableCallback<T> extends Callback<T> {
        void onCancel();
    }

    /**
     * 私有构造方法，防止外部实例化
     *
     * @param context 上下文对象
     */
    private BillRepository(Context context) {
        Log.d(TAG, "BillRepository: 初始化仓库");
        mDbHelper = new BillDbHelper(context.getApplicationContext());
        mExecutorService = Executors.newFixedThreadPool(2);
        mRequestIdGenerator = new java.util.concurrent.atomic.AtomicInteger(0);
        mPendingRequests = new java.util.concurrent.ConcurrentHashMap<>();
        Log.d(TAG, "BillRepository: 初始化完成");
    }

    /**
     * 获取单例实例
     * 使用双重检查锁定模式确保线程安全
     *
     * @param context 上下文对象
     * @return BillRepository单例实例
     */
    public static BillRepository getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BillRepository.class) {
                if (sInstance == null) {
                    Log.d(TAG, "getInstance: 创建新实例");
                    sInstance = new BillRepository(context);
                }
            }
        }
        return sInstance;
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
     * @return 请求ID，可用于取消请求
     */
    public String calculateTodayStatisticsAsync(String todayDate, Callback<TodayStatistics> callback) {
        String requestId = "today_stats_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        Log.d(TAG, "calculateTodayStatisticsAsync: 提交请求，ID=" + requestId + "，日期=" + todayDate);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "calculateTodayStatisticsAsync: 请求已取消，ID=" + requestId);
                    return;
                }
                TodayStatistics statistics = calculateTodayStatistics(todayDate);
                if (mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "calculateTodayStatisticsAsync: 请求成功，ID=" + requestId);
                    callback.onSuccess(statistics);
                    mPendingRequests.remove(requestId);
                } else {
                    Log.d(TAG, "calculateTodayStatisticsAsync: 请求已取消，ID=" + requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    Log.e(TAG, "calculateTodayStatisticsAsync: 请求失败，ID=" + requestId + "，错误=" + e.getMessage());
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
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
     * @return 请求ID，可用于取消请求
     */
    public String calculateMonthStatisticsAsync(String month, Callback<MonthStatistics> callback) {
        String requestId = "month_stats_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    return;
                }
                MonthStatistics statistics = calculateMonthStatistics(month);
                if (mPendingRequests.containsKey(requestId)) {
                    callback.onSuccess(statistics);
                    mPendingRequests.remove(requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
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
            List<Bill> monthBills = mDbHelper.queryMonthBill(month);

            double monthIncome = 0.0;
            double monthExpense = 0.0;

            for (Bill bill : monthBills) {
                if (bill.getBillType() == 1) {
                    monthIncome += bill.getAmount();
                } else {
                    monthExpense += bill.getAmount();
                }
            }

            statistics.setIncome(monthIncome);
            statistics.setExpense(monthExpense);
            statistics.setBalance(monthIncome - monthExpense);

        } catch (Exception e) {
            Log.e(TAG, "计算本月统计数据失败: " + e.getMessage());
        }

        return statistics;
    }

    /**
     * 计算年份统计数据（异步方法）
     *
     * @param year 年份字符串（yyyy格式）
     * @param callback 回调接口，用于返回统计结果
     * @return 请求ID，可用于取消请求
     */
    public String calculateYearStatisticsAsync(String year, Callback<YearStatistics> callback) {
        String requestId = "year_stats_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    return;
                }
                YearStatistics statistics = calculateYearStatistics(year);
                if (mPendingRequests.containsKey(requestId)) {
                    callback.onSuccess(statistics);
                    mPendingRequests.remove(requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
    }

    /**
     * 计算年份统计数据（同步方法）
     *
     * @param year 年份字符串（yyyy格式）
     * @return 年份统计结果
     */
    public YearStatistics calculateYearStatistics(String year) {
        YearStatistics statistics = new YearStatistics();

        try {
            List<Bill> yearBills = mDbHelper.queryYearBill(year);

            double yearIncome = 0.0;
            double yearExpense = 0.0;

            for (Bill bill : yearBills) {
                if (bill.getBillType() == 1) {
                    yearIncome += bill.getAmount();
                } else {
                    yearExpense += bill.getAmount();
                }
            }

            statistics.setIncome(yearIncome);
            statistics.setExpense(yearExpense);
            statistics.setBalance(yearIncome - yearExpense);

        } catch (Exception e) {
            Log.e(TAG, "计算年份统计数据失败: " + e.getMessage());
        }

        return statistics;
    }

    /**
     * 获取周分类统计 - 异步方法
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param billType 账单类型（1=收入，2=支出）
     * @param callback 回调接口，用于返回分类统计列表
     * @return 请求ID，可用于取消请求
     */
    public String getWeekCategoryStatisticsAsync(String startDate, String endDate, int billType, Callback<List<CategoryStatistics>> callback) {
        String requestId = "week_category_stats_" + startDate + "_" + endDate + "_" + billType + "_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        Log.d(TAG, "getWeekCategoryStatisticsAsync: 提交请求，ID=" + requestId + "，开始=" + startDate + "，结束=" + endDate + "，类型=" + billType);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "getWeekCategoryStatisticsAsync: 请求已取消，ID=" + requestId);
                    return;
                }
                List<CategoryStatistics> categoryList = mDbHelper.queryWeekCategoryStatistics(startDate, endDate, billType);
                if (mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "getWeekCategoryStatisticsAsync: 请求成功，ID=" + requestId + "，数量=" + categoryList.size());
                    callback.onSuccess(categoryList);
                    mPendingRequests.remove(requestId);
                } else {
                    Log.d(TAG, "getWeekCategoryStatisticsAsync: 请求已取消，ID=" + requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    Log.e(TAG, "getWeekCategoryStatisticsAsync: 请求失败，ID=" + requestId + "，错误=" + e.getMessage());
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
    }

    /**
     * 获取年份分类统计 - 异步方法
     *
     * @param year 年份字符串（yyyy格式）
     * @param billType 账单类型（1=收入，2=支出）
     * @param callback 回调接口，用于返回分类统计列表
     * @return 请求ID，可用于取消请求
     */
    public String getYearCategoryStatisticsAsync(String year, int billType, Callback<List<CategoryStatistics>> callback) {
        String requestId = "year_category_stats_" + year + "_" + billType + "_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        Log.d(TAG, "getYearCategoryStatisticsAsync: 提交请求，ID=" + requestId + "，年份=" + year + "，类型=" + billType);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "getYearCategoryStatisticsAsync: 请求已取消，ID=" + requestId);
                    return;
                }
                List<CategoryStatistics> categoryList = mDbHelper.queryYearCategoryStatistics(year, billType);
                if (mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "getYearCategoryStatisticsAsync: 请求成功，ID=" + requestId + "，数量=" + categoryList.size());
                    callback.onSuccess(categoryList);
                    mPendingRequests.remove(requestId);
                } else {
                    Log.d(TAG, "getYearCategoryStatisticsAsync: 请求已取消，ID=" + requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    Log.e(TAG, "getYearCategoryStatisticsAsync: 请求失败，ID=" + requestId + "，错误=" + e.getMessage());
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
    }

    /**
     * 获取月份分类统计 - 异步方法
     *
     * @param month 月份字符串（yyyy-MM格式）
     * @param billType 账单类型（1=收入，2=支出）
     * @param callback 回调接口，用于返回分类统计列表
     * @return 请求ID，可用于取消请求
     */
    public String getMonthCategoryStatisticsAsync(String month, int billType, Callback<List<CategoryStatistics>> callback) {
        String requestId = "month_category_stats_" + month + "_" + billType + "_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        Log.d(TAG, "getMonthCategoryStatisticsAsync: 提交请求，ID=" + requestId + "，月份=" + month + "，类型=" + billType);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "getMonthCategoryStatisticsAsync: 请求已取消，ID=" + requestId);
                    return;
                }
                List<CategoryStatistics> categoryList = mDbHelper.queryMonthCategoryStatistics(month, billType);
                if (mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "getMonthCategoryStatisticsAsync: 请求成功，ID=" + requestId + "，数量=" + categoryList.size());
                    callback.onSuccess(categoryList);
                    mPendingRequests.remove(requestId);
                } else {
                    Log.d(TAG, "getMonthCategoryStatisticsAsync: 请求已取消，ID=" + requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    Log.e(TAG, "getMonthCategoryStatisticsAsync: 请求失败，ID=" + requestId + "，错误=" + e.getMessage());
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
    }

    /**
     * 获取近期账单（近7条）- 异步方法
     *
     * @param callback 回调接口，用于返回账单列表
     * @return 请求ID，可用于取消请求
     */
    public String getRecentBillsAsync(Callback<List<Bill>> callback) {
        String requestId = "recent_bills_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    return;
                }
                List<Bill> bills = getRecentBills();
                if (mPendingRequests.containsKey(requestId)) {
                    callback.onSuccess(bills);
                    mPendingRequests.remove(requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
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
     * @return 请求ID，可用于取消请求
     */
    public String getBillsByDaysAsync(int days, Callback<List<Bill>> callback) {
        String requestId = "bills_by_days_" + days + "_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    return;
                }
                List<Bill> bills = getBillsByDays(days);
                if (mPendingRequests.containsKey(requestId)) {
                    callback.onSuccess(bills);
                    mPendingRequests.remove(requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
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
     * @return 请求ID，可用于取消请求
     */
    public String getBillsByDateAsync(String date, Callback<List<Bill>> callback) {
        String requestId = "bills_by_date_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        Log.d(TAG, "getBillsByDateAsync: 提交请求，ID=" + requestId + "，日期=" + date);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "getBillsByDateAsync: 请求已取消，ID=" + requestId);
                    return;
                }
                List<Bill> bills = getBillsByDate(date);
                if (mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "getBillsByDateAsync: 请求成功，ID=" + requestId + "，数量=" + bills.size());
                    callback.onSuccess(bills);
                    mPendingRequests.remove(requestId);
                } else {
                    Log.d(TAG, "getBillsByDateAsync: 请求已取消，ID=" + requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    Log.e(TAG, "getBillsByDateAsync: 请求失败，ID=" + requestId + "，错误=" + e.getMessage());
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
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
        Log.d(TAG, "addBillAsync: 提交添加账单请求，金额=" + bill.getAmount() + "，类型=" + bill.getBillType());
        mExecutorService.execute(() -> {
            try {
                boolean result = addBill(bill);
                Log.d(TAG, "addBillAsync: 添加账单" + (result ? "成功" : "失败"));
                callback.onSuccess(result);
            } catch (Exception e) {
                Log.e(TAG, "addBillAsync: 添加账单异常: " + e.getMessage());
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
            boolean result = mDbHelper.insertBill(bill);
            Log.d(TAG, "addBill: 数据库插入" + (result ? "成功" : "失败"));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "addBill: 添加账单失败: " + e.getMessage());
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
        Log.d(TAG, "updateBillAsync: 提交更新账单请求，ID=" + bill.getId());
        mExecutorService.execute(() -> {
            try {
                boolean result = updateBill(bill);
                Log.d(TAG, "updateBillAsync: 更新账单" + (result ? "成功" : "失败"));
                callback.onSuccess(result);
            } catch (Exception e) {
                Log.e(TAG, "updateBillAsync: 更新账单异常: " + e.getMessage());
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
            boolean result = mDbHelper.updateBill(bill);
            Log.d(TAG, "updateBill: 数据库更新" + (result ? "成功" : "失败"));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "updateBill: 更新账单失败: " + e.getMessage());
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
        Log.d(TAG, "deleteBillAsync: 提交删除账单请求，ID=" + billId);
        mExecutorService.execute(() -> {
            try {
                boolean result = deleteBill(billId);
                Log.d(TAG, "deleteBillAsync: 删除账单" + (result ? "成功" : "失败"));
                callback.onSuccess(result);
            } catch (Exception e) {
                Log.e(TAG, "deleteBillAsync: 删除账单异常: " + e.getMessage());
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
            boolean result = mDbHelper.deleteBill(billId);
            Log.d(TAG, "deleteBill: 数据库删除" + (result ? "成功" : "失败"));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "deleteBill: 删除账单失败: " + e.getMessage());
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
     * 静态方法，用于在应用退出时统一调用
     */
    public static void close() {
        if (sInstance != null) {
            sInstance.mDbHelper.close();
            sInstance.mExecutorService.shutdown();
            sInstance = null;
        }
    }

    /**
     * 计算本周统计数据（同步方法）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 本周统计结果
     */
    public WeekStatistics calculateWeekStatistics(String startDate, String endDate) {
        WeekStatistics statistics = new WeekStatistics();
        try {
            List<Bill> weekBills = mDbHelper.queryWeekBill(startDate, endDate);

            double weekIncome = 0.0;
            double weekExpense = 0.0;

            for (Bill bill : weekBills) {
                if (bill.getBillType() == 1) {
                    weekIncome += bill.getAmount();
                } else {
                    weekExpense += bill.getAmount();
                }
            }

            statistics.setIncome(weekIncome);
            statistics.setExpense(weekExpense);
            statistics.setBalance(weekIncome - weekExpense);

        } catch (Exception e) {
            Log.e(TAG, "计算本周统计数据失败: " + e.getMessage());
        }

        return statistics;
    }

    /**
     * 计算本周统计数据（异步方法）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param callback 回调接口，用于返回统计结果
     * @return 请求ID，可用于取消请求
     */
    public String calculateWeekStatisticsAsync(String startDate, String endDate, Callback<WeekStatistics> callback) {
        String requestId = "week_stats_" + mRequestIdGenerator.incrementAndGet();
        mPendingRequests.put(requestId, true);
        Log.d(TAG, "calculateWeekStatisticsAsync: 提交请求，ID=" + requestId + "，开始=" + startDate + "，结束=" + endDate);
        
        mExecutorService.execute(() -> {
            try {
                if (!mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "calculateWeekStatisticsAsync: 请求已取消，ID=" + requestId);
                    return;
                }
                WeekStatistics statistics = calculateWeekStatistics(startDate, endDate);
                if (mPendingRequests.containsKey(requestId)) {
                    Log.d(TAG, "calculateWeekStatisticsAsync: 请求成功，ID=" + requestId);
                    callback.onSuccess(statistics);
                    mPendingRequests.remove(requestId);
                } else {
                    Log.d(TAG, "calculateWeekStatisticsAsync: 请求已取消，ID=" + requestId);
                }
            } catch (Exception e) {
                if (mPendingRequests.containsKey(requestId)) {
                    Log.e(TAG, "calculateWeekStatisticsAsync: 请求失败，ID=" + requestId + "，错误=" + e.getMessage());
                    callback.onError(e);
                    mPendingRequests.remove(requestId);
                }
            }
        });
        return requestId;
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
     * 本周统计数据类
     * 用于封装本周收入、支出和结余信息
     */
    public static class WeekStatistics {
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

    /**
     * 年份统计数据类
     * 用于封装年份收入、支出和结余信息
     */
    public static class YearStatistics {
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
     * 取消指定的异步请求
     *
     * @param requestId 请求ID
     */
    public void cancelRequest(String requestId) {
        if (requestId != null) {
            Boolean removed = mPendingRequests.remove(requestId);
            Log.d(TAG, "cancelRequest: 取消请求，ID=" + requestId + "，" + (removed != null ? "成功" : "未找到"));
        }
    }

    /**
     * 取消所有待处理的请求
     */
    public void cancelAllRequests() {
        int count = mPendingRequests.size();
        mPendingRequests.clear();
        Log.d(TAG, "cancelAllRequests: 取消所有待处理的请求，数量=" + count);
    }
}

