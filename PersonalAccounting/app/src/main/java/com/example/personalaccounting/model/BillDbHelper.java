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
        Log.d("BillDbHelper", "账单表创建成功");
    }

    /**
     * 数据库版本更新时调用
     * @param db SQLiteDatabase对象
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果表存在，先删除表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILL);
        // 重新创建表
        onCreate(db);
        Log.d("BillDbHelper", "数据库版本更新成功");
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
                Log.d("BillDbHelper", "账单插入成功，ID: " + id);
            }
        } catch (Exception e) {
            Log.e("BillDbHelper", "插入账单失败: " + e.getMessage());
        } finally {
            // 关闭数据库连接
            if (db != null) {
                db.close();
            }
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
        } catch (Exception e) {
            Log.e("BillDbHelper", "查询所有账单失败: " + e.getMessage());
        } finally {
            // 关闭游标和数据库连接
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
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
            // 关闭游标和数据库连接
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
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
            // 关闭游标和数据库连接
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
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
            // 关闭游标和数据库连接
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return billList;
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
                Log.d("BillDbHelper", "账单更新成功，ID: " + bill.getId());
            }
        } catch (Exception e) {
            Log.e("BillDbHelper", "更新账单失败: " + e.getMessage());
        } finally {
            // 关闭数据库连接
            if (db != null) {
                db.close();
            }
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
            // 获取可写数据库
            db = this.getWritableDatabase();

            // 执行删除操作，返回受影响的行数
            int rows = db.delete(TABLE_BILL, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(billId)});

            // 如果受影响的行数大于0，表示删除成功
            if (rows > 0) {
                result = true;
                Log.d("BillDbHelper", "账单删除成功，ID: " + billId);
            }
        } catch (Exception e) {
            Log.e("BillDbHelper", "删除账单失败: " + e.getMessage());
        } finally {
            // 关闭数据库连接
            if (db != null) {
                db.close();
            }
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
            }
        } catch (Exception e) {
            Log.e("BillDbHelper", "根据ID查询账单失败: " + e.getMessage());
        } finally {
            // 关闭游标和数据库连接
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return bill;
    }
}
