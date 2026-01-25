package com.example.personalaccounting.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 账单数据库帮助类
 * 用于创建数据库、表结构和提供基本的数据库操作方法
 */
public class BillDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "BillDbHelper";
    // 数据库名称
    private static final String DATABASE_NAME = "bill.db";
    // 数据库版本
    private static final int DATABASE_VERSION = 1;
    // 账单表名
    private static final String TABLE_BILL = "bill_table";

    // 表字段
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_BILL_TYPE = "bill_type";
    private static final String COLUMN_REMARK = "remark";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_CREATE_TIME = "create_time";

    /**
     * 构造方法
     * @param context 上下文
     */
    public BillDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "BillDbHelper: 初始化数据库帮助类");
    }

    /**
     * 创建数据库表
     * @param db SQLiteDatabase对象
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建账单表的SQL语句
        String CREATE_BILL_TABLE = "CREATE TABLE " + TABLE_BILL + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TYPE + " TEXT NOT NULL, " +
                COLUMN_AMOUNT + " REAL NOT NULL, " +
                COLUMN_BILL_TYPE + " INTEGER NOT NULL, " +
                COLUMN_REMARK + " TEXT, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_CREATE_TIME + " INTEGER NOT NULL" +
                ");";

        // 执行创建表的SQL语句
        db.execSQL(CREATE_BILL_TABLE);
        Log.d(TAG, "onCreate: 账单表创建成功");
    }

    /**
     * 数据库版本更新时调用
     * @param db SQLiteDatabase对象
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: 数据库版本更新，旧版本=" + oldVersion + "，新版本=" + newVersion);
        // 如果表存在，先删除表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILL);
        // 重新创建表
        onCreate(db);
        Log.d(TAG, "onUpgrade: 数据库版本更新成功");
    }

    /**
     * 新增账单
     * @param bill 账单对象
     * @return 是否新增成功
     */
    public boolean insertBill(Bill bill) {
        boolean result = false;
        SQLiteDatabase db = null;
        try {
            Log.d(TAG, "insertBill: 开始插入账单，金额=" + bill.getAmount() + "，类型=" + bill.getBillType());
            // 获取可写数据库
            db = this.getWritableDatabase();

            // 创建ContentValues对象，用于存储要插入的数据
            ContentValues values = new ContentValues();
            values.put(COLUMN_TYPE, bill.getType());
            values.put(COLUMN_AMOUNT, bill.getAmount());
            values.put(COLUMN_BILL_TYPE, bill.getBillType());
            values.put(COLUMN_REMARK, bill.getRemark());
            values.put(COLUMN_DATE, bill.getDate());
            values.put(COLUMN_CREATE_TIME, bill.getCreateTime());

            // 执行插入操作，返回新插入行的ID
            long id = db.insert(TABLE_BILL, null, values);

            // 如果ID大于0，表示插入成功
            if (id > 0) {
                result = true;
                Log.d(TAG, "insertBill: 账单插入成功，ID: " + id);
            } else {
                Log.w(TAG, "insertBill: 账单插入失败，ID <= 0");
            }
        } catch (Exception e) {
            Log.e(TAG, "insertBill: 插入账单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 查询所有账单，按创建时间倒序排列
     * @return 账单列表
     */
    public List<Bill> queryAllBill() {
        List<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            Log.d(TAG, "queryAllBill: 开始查询所有账单");
            // 获取可读数据库
            db = this.getReadableDatabase();

            // 查询所有账单，按创建时间倒序排列
            String selectQuery = "SELECT * FROM " + TABLE_BILL + " ORDER BY " + COLUMN_CREATE_TIME + " DESC";
            cursor = db.rawQuery(selectQuery, null);

            // 遍历查询结果
            if (cursor.moveToFirst()) {
                do {
                    // 创建Bill对象
                    Bill bill = new Bill();
                    bill.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    bill.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                    bill.setAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)));
                    bill.setBillType(cursor.getInt(cursor.getColumnIndex(COLUMN_BILL_TYPE)));
                    bill.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
                    bill.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                    bill.setCreateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATE_TIME)));

                    // 添加到列表中
                    billList.add(bill);
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "queryAllBill: 查询完成，共" + billList.size() + "条记录");
        } catch (Exception e) {
            Log.e(TAG, "queryAllBill: 查询所有账单失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return billList;
    }

    /**
     * 按账单类型查询账单
     * @param billType 0=支出，1=收入
     * @return 账单列表
     */
    public List<Bill> queryBillByType(int billType) {
        List<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            // 获取可读数据库
            db = this.getReadableDatabase();

            // 查询指定类型的账单，按创建时间倒序排列
            String selectQuery = "SELECT * FROM " + TABLE_BILL + " WHERE " + COLUMN_BILL_TYPE + " = ? ORDER BY " + COLUMN_CREATE_TIME + " DESC";
            cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(billType)});

            // 遍历查询结果
            if (cursor.moveToFirst()) {
                do {
                    // 创建Bill对象
                    Bill bill = new Bill();
                    bill.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    bill.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                    bill.setAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)));
                    bill.setBillType(cursor.getInt(cursor.getColumnIndex(COLUMN_BILL_TYPE)));
                    bill.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
                    bill.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                    bill.setCreateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATE_TIME)));

                    // 添加到列表中
                    billList.add(bill);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("BillDbHelper", "按类型查询账单失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return billList;
    }

    /**
     * 查询今日账单
     * @param todayDate 今日日期，格式：yyyy-MM-dd
     * @return 账单列表
     */
    public List<Bill> queryTodayBill(String todayDate) {
        List<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            // 获取可读数据库
            db = this.getReadableDatabase();

            // 查询今日账单
            String selectQuery = "SELECT * FROM " + TABLE_BILL + " WHERE " + COLUMN_DATE + " = ?";
            cursor = db.rawQuery(selectQuery, new String[]{todayDate});

            // 遍历查询结果
            if (cursor.moveToFirst()) {
                do {
                    // 创建Bill对象
                    Bill bill = new Bill();
                    bill.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    bill.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                    bill.setAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)));
                    bill.setBillType(cursor.getInt(cursor.getColumnIndex(COLUMN_BILL_TYPE)));
                    bill.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
                    bill.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                    bill.setCreateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATE_TIME)));

                    // 添加到列表中
                    billList.add(bill);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("BillDbHelper", "查询今日账单失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return billList;
    }

    /**
     * 查询本月账单
     * @param month 月份，格式：yyyy-MM
     * @return 账单列表
     */
    public List<Bill> queryMonthBill(String month) {
        List<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            // 获取可读数据库
            db = this.getReadableDatabase();

            // 查询本月账单（使用 LIKE 查询匹配月份前缀）
            String selectQuery = "SELECT * FROM " + TABLE_BILL + " WHERE " + COLUMN_DATE + " LIKE ?";
            cursor = db.rawQuery(selectQuery, new String[]{month + "%"});

            // 遍历查询结果
            if (cursor.moveToFirst()) {
                do {
                    // 创建Bill对象
                    Bill bill = new Bill();
                    bill.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    bill.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                    bill.setAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)));
                    bill.setBillType(cursor.getInt(cursor.getColumnIndex(COLUMN_BILL_TYPE)));
                    bill.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
                    bill.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                    bill.setCreateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATE_TIME)));

                    // 添加到列表中
                    billList.add(bill);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("BillDbHelper", "查询本月账单失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return billList;
    }

    /**
     * 查询指定年份的账单
     * @param year 年份字符串（yyyy格式）
     * @return 账单列表
     */
    public List<Bill> queryYearBill(String year) {
        List<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            Log.d(TAG, "queryYearBill: 开始查询年份账单，年份=" + year);
            db = this.getReadableDatabase();

            String selectQuery = "SELECT * FROM " + TABLE_BILL + " WHERE " + COLUMN_DATE + " LIKE ?";
            cursor = db.rawQuery(selectQuery, new String[]{year + "%"});

            if (cursor.moveToFirst()) {
                do {
                    Bill bill = new Bill();
                    bill.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    bill.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                    bill.setAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)));
                    bill.setBillType(cursor.getInt(cursor.getColumnIndex(COLUMN_BILL_TYPE)));
                    bill.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
                    bill.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                    bill.setCreateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATE_TIME)));

                    billList.add(bill);
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "queryYearBill: 查询完成，共" + billList.size() + "条记录");
        } catch (Exception e) {
            Log.e(TAG, "queryYearBill: 查询年份账单失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return billList;
    }

    /**
     * 查询指定周范围的账单
     * @param startDate 开始日期（yyyy-MM-dd格式）
     * @param endDate 结束日期（yyyy-MM-dd格式）
     * @return 账单列表
     */
    public List<Bill> queryWeekBill(String startDate, String endDate) {
        List<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            Log.d(TAG, "queryWeekBill: 开始查询周账单，开始=" + startDate + "，结束=" + endDate);
            db = this.getReadableDatabase();

            String selectQuery = "SELECT * FROM " + TABLE_BILL + 
                    " WHERE " + COLUMN_DATE + " >= ? AND " + COLUMN_DATE + " <= ?" +
                    " ORDER BY " + COLUMN_DATE + " DESC";
            cursor = db.rawQuery(selectQuery, new String[]{startDate, endDate});

            if (cursor.moveToFirst()) {
                do {
                    Bill bill = new Bill();
                    bill.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    bill.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                    bill.setAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)));
                    bill.setBillType(cursor.getInt(cursor.getColumnIndex(COLUMN_BILL_TYPE)));
                    bill.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
                    bill.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                    bill.setCreateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATE_TIME)));

                    billList.add(bill);
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "queryWeekBill: 查询完成，共" + billList.size() + "条记录");
        } catch (Exception e) {
            Log.e(TAG, "queryWeekBill: 查询周账单失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return billList;
    }

    /**
     * 查询指定年份和账单类型的分类统计
     * @param year 年份字符串（yyyy格式）
     * @param billType 账单类型（1=收入，2=支出）
     * @return 分类统计列表
     */
    public List<CategoryStatistics> queryYearCategoryStatistics(String year, int billType) {
        List<CategoryStatistics> categoryList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            Log.d(TAG, "queryYearCategoryStatistics: 开始查询年份分类统计，年份=" + year + "，类型=" + billType);
            db = getReadableDatabase();

            String selectQuery = "SELECT " + COLUMN_TYPE + ", SUM(" + COLUMN_AMOUNT + ") as total_amount, COUNT(*) as bill_count " +
                    "FROM " + TABLE_BILL +
                    " WHERE " + COLUMN_DATE + " LIKE ? AND " + COLUMN_BILL_TYPE + " = ?" +
                    " GROUP BY " + COLUMN_TYPE +
                    " ORDER BY total_amount DESC";
            cursor = db.rawQuery(selectQuery, new String[]{year + "%", String.valueOf(billType)});

            if (cursor.moveToFirst()) {
                do {
                    String categoryName = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                    double amount = cursor.getDouble(cursor.getColumnIndex("total_amount"));
                    int count = cursor.getInt(cursor.getColumnIndex("bill_count"));
                    categoryList.add(new CategoryStatistics(categoryName, amount, count, 0));
                } while (cursor.moveToNext());
            }
            
            double totalAmount = 0;
            for (CategoryStatistics category : categoryList) {
                totalAmount += category.getAmount();
            }
            
            for (CategoryStatistics category : categoryList) {
                if (totalAmount > 0) {
                    double percentage = (category.getAmount() / totalAmount) * 100;
                    category.setPercentage(percentage);
                }
            }
            
            Log.d(TAG, "queryYearCategoryStatistics: 查询完成，共" + categoryList.size() + "个分类");
        } catch (Exception e) {
            Log.e(TAG, "queryYearCategoryStatistics: 查询年份分类统计失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return categoryList;
    }

    /**
     * 查询指定月份和账单类型的分类统计
     * @param month 月份字符串（yyyy-MM格式）
     * @param billType 账单类型（1=收入，2=支出）
     * @return 分类统计列表
     */
    public List<CategoryStatistics> queryMonthCategoryStatistics(String month, int billType) {
        List<CategoryStatistics> categoryList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            Log.d(TAG, "queryMonthCategoryStatistics: 开始查询月份分类统计，月份=" + month + "，类型=" + billType);
            db = getReadableDatabase();

            String selectQuery = "SELECT " + COLUMN_TYPE + ", SUM(" + COLUMN_AMOUNT + ") as total_amount, COUNT(*) as bill_count " +
                    "FROM " + TABLE_BILL +
                    " WHERE " + COLUMN_DATE + " LIKE ? AND " + COLUMN_BILL_TYPE + " = ?" +
                    " GROUP BY " + COLUMN_TYPE +
                    " ORDER BY total_amount DESC";
            cursor = db.rawQuery(selectQuery, new String[]{month + "%", String.valueOf(billType)});

            if (cursor.moveToFirst()) {
                do {
                    String categoryName = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                    double amount = cursor.getDouble(cursor.getColumnIndex("total_amount"));
                    int count = cursor.getInt(cursor.getColumnIndex("bill_count"));
                    categoryList.add(new CategoryStatistics(categoryName, amount, count, 0));
                } while (cursor.moveToNext());
            }
            
            double totalAmount = 0;
            for (CategoryStatistics category : categoryList) {
                totalAmount += category.getAmount();
            }
            
            for (CategoryStatistics category : categoryList) {
                if (totalAmount > 0) {
                    double percentage = (category.getAmount() / totalAmount) * 100;
                    category.setPercentage(percentage);
                }
            }
            
            Log.d(TAG, "queryMonthCategoryStatistics: 查询完成，共" + categoryList.size() + "个分类");
        } catch (Exception e) {
            Log.e(TAG, "queryMonthCategoryStatistics: 查询月份分类统计失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return categoryList;
    }

    /**
     * 查询指定周范围和账单类型的分类统计
     * @param startDate 开始日期（yyyy-MM-dd格式）
     * @param endDate 结束日期（yyyy-MM-dd格式）
     * @param billType 账单类型（1=收入，2=支出）
     * @return 分类统计列表
     */
    public List<CategoryStatistics> queryWeekCategoryStatistics(String startDate, String endDate, int billType) {
        List<CategoryStatistics> categoryList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            Log.d(TAG, "queryWeekCategoryStatistics: 开始查询周分类统计，开始=" + startDate + "，结束=" + endDate + "，类型=" + billType);
            db = getReadableDatabase();

            String selectQuery = "SELECT " + COLUMN_TYPE + ", SUM(" + COLUMN_AMOUNT + ") as total_amount, COUNT(*) as bill_count " +
                    "FROM " + TABLE_BILL +
                    " WHERE " + COLUMN_DATE + " >= ? AND " + COLUMN_DATE + " <= ? AND " + COLUMN_BILL_TYPE + " = ?" +
                    " GROUP BY " + COLUMN_TYPE +
                    " ORDER BY total_amount DESC";
            cursor = db.rawQuery(selectQuery, new String[]{startDate, endDate, String.valueOf(billType)});

            if (cursor.moveToFirst()) {
                do {
                    String categoryName = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                    double amount = cursor.getDouble(cursor.getColumnIndex("total_amount"));
                    int count = cursor.getInt(cursor.getColumnIndex("bill_count"));
                    categoryList.add(new CategoryStatistics(categoryName, amount, count, 0));
                } while (cursor.moveToNext());
            }
            
            double totalAmount = 0;
            for (CategoryStatistics category : categoryList) {
                totalAmount += category.getAmount();
            }
            
            for (CategoryStatistics category : categoryList) {
                if (totalAmount > 0) {
                    double percentage = (category.getAmount() / totalAmount) * 100;
                    category.setPercentage(percentage);
                }
            }
            
            Log.d(TAG, "queryWeekCategoryStatistics: 查询完成，共" + categoryList.size() + "个分类");
        } catch (Exception e) {
            Log.e(TAG, "queryWeekCategoryStatistics: 查询周分类统计失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return categoryList;
    }

    /**
     * 更新账单
     * @param bill 账单对象，必须包含有效的ID
     * @return 是否更新成功
     */
    public boolean updateBill(Bill bill) {
        boolean result = false;
        SQLiteDatabase db = null;
        try {
            Log.d(TAG, "updateBill: 开始更新账单，ID=" + bill.getId());
            // 获取可写数据库
            db = this.getWritableDatabase();

            // 创建ContentValues对象，用于存储要更新的数据
            ContentValues values = new ContentValues();
            values.put(COLUMN_TYPE, bill.getType());
            values.put(COLUMN_AMOUNT, bill.getAmount());
            values.put(COLUMN_BILL_TYPE, bill.getBillType());
            values.put(COLUMN_REMARK, bill.getRemark());
            values.put(COLUMN_DATE, bill.getDate());

            // 执行更新操作，返回受影响的行数
            int rows = db.update(TABLE_BILL, values, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(bill.getId())});

            // 如果受影响的行数大于0，表示更新成功
            if (rows > 0) {
                result = true;
                Log.d(TAG, "updateBill: 账单更新成功，影响行数=" + rows);
            } else {
                Log.w(TAG, "updateBill: 账单更新失败，影响行数=0");
            }
        } catch (Exception e) {
            Log.e(TAG, "updateBill: 更新账单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除账单
     * @param billId 账单ID
     * @return 是否删除成功
     */
    public boolean deleteBill(int billId) {
        boolean result = false;
        SQLiteDatabase db = null;
        try {
            Log.d(TAG, "deleteBill: 开始删除账单，ID=" + billId);
            // 获取可写数据库
            db = this.getWritableDatabase();

            // 执行删除操作，返回受影响的行数
            int rows = db.delete(TABLE_BILL, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(billId)});

            // 如果受影响的行数大于0，表示删除成功
            if (rows > 0) {
                result = true;
                Log.d(TAG, "deleteBill: 账单删除成功，删除行数=" + rows);
            } else {
                Log.w(TAG, "deleteBill: 账单删除失败，删除行数=0");
            }
        } catch (Exception e) {
            Log.e(TAG, "deleteBill: 删除账单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据ID查询账单
     * @param billId 账单ID
     * @return 账单对象，如果不存在则返回null
     */
    public Bill queryBillById(int billId) {
        Bill bill = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            Log.d(TAG, "queryBillById: 开始查询账单，ID=" + billId);
            // 获取可读数据库
            db = this.getReadableDatabase();

            // 查询指定ID的账单
            String selectQuery = "SELECT * FROM " + TABLE_BILL + " WHERE " + COLUMN_ID + " = ?";
            cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(billId)});

            // 如果查询到结果
            if (cursor.moveToFirst()) {
                // 创建Bill对象
                bill = new Bill();
                bill.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                bill.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                bill.setAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)));
                bill.setBillType(cursor.getInt(cursor.getColumnIndex(COLUMN_BILL_TYPE)));
                bill.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
                bill.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                bill.setCreateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATE_TIME)));
                Log.d(TAG, "queryBillById: 查询成功，ID=" + billId + "，金额=" + bill.getAmount());
            } else {
                Log.w(TAG, "queryBillById: 未找到账单，ID=" + billId);
            }
        } catch (Exception e) {
            Log.e(TAG, "queryBillById: 根据ID查询账单失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bill;
    }
}
