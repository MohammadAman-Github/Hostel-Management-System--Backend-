package org.example.hms.DTOs;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({
        "month",
        "year",
        "roomNo",
        "rent",
        "lastReading",
        "currentReading",
        "totalLightBill",
        "arrearBill",
        "totalRent",
        "paymentStatus"
})

@Data
public class CreateMrdDto {
    public String month;
    public String year;
    public Integer roomNo;
    public int rent;
    public int currentReading;
    public int lastReading;
    public int totalLightBill;
    public int arrearBill;
    public int totalRent;
    public String paymentStatus;
}
