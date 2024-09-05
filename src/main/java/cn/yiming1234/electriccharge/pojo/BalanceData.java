package cn.yiming1234.electriccharge.pojo;

import lombok.Data;

@Data
public class BalanceData {
    private double amount;
    private long canBuy;
    private String displayRoomName;
    private long footerLink;
    private long isShowMoney;
    private long isShowSurplus;
    private String remind;
    private double surplus;
    private long system;
}
