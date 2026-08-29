package org.example.hms.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonPropertyOrder({
        "month",
        "year",
        "roomNo",
        "students",
        "rent",
        "lastReading",
        "currentReading",
        "totalLightBill",
        "arrearBill",
        "totalRent",
        "totalRentPaid",
        "paymentStatus"
})

@JsonInclude(JsonInclude.Include.NON_NULL)
//Jackson will not include fields whose value is null when converting the Java object to JSON.

@Data
@NoArgsConstructor
@AllArgsConstructor
public class monthly_rent_details_dto {
    public String month;
    public String year;
    public Integer roomNo;
    public List<studentInfoDto> students;
    public int rent;
    public int lastReading;
    public int currentReading;
    public int totalLightBill;
    public int arrearBill;
    public int totalRent;
    public int totalRentPaid;
    public String paymentStatus;
}
