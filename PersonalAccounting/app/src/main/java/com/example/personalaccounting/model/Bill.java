package com.example.personalaccounting.model;

/**
 * 账单实体类
 * 用于存储账单的基本信息
 */
public class Bill {
    // 唯一标识，自增
    private int id;
    // 账单类型，如「餐饮」「工资」
    private String type;
    // 账单金额，保留2位小数
    private double amount;
    // 0=支出，1=收入
    private int billType;
    // 账单备注，可为空
    private String remark;
    // 账单日期，格式：yyyy-MM-dd
    private String date;
    // 创建时间戳，用于排序
    private long createTime;

    /**
     * 无参构造方法
     */
    public Bill() {
    }

    /**
     * 全参构造方法
     * @param id 唯一标识
     * @param type 账单类型
     * @param amount 账单金额
     * @param billType 0=支出，1=收入
     * @param remark 账单备注
     * @param date 账单日期
     * @param createTime 创建时间戳
     */
    public Bill(int id, String type, double amount, int billType, String remark, String date, long createTime) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.billType = billType;
        this.remark = remark;
        this.date = date;
        this.createTime = createTime;
    }

    // getter和setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getBillType() {
        return billType;
    }

    public void setBillType(int billType) {
        this.billType = billType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /**
     * toString方法，用于调试和日志输出
     * @return 账单信息的字符串表示
     */
    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", billType=" + billType +
                ", remark='" + remark + '\'' +
                ", date='" + date + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
