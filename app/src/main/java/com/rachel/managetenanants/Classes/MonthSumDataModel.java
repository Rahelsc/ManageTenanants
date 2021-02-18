package com.rachel.managetenanants.Classes;

public class MonthSumDataModel {
    private String month;
    private String sum;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public MonthSumDataModel(String month, String sum) {
        this.month = month;
        this.sum = sum;
    }
}
