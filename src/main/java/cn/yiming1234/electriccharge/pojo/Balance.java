package cn.yiming1234.electriccharge.pojo;

@lombok.Data
public class Balance {
    private BalanceData data;
    private String message;
    private long statusCode;
    private boolean success;
}