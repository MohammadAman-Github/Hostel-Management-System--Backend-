package org.example.hmsBackend.DTOs;

import lombok.Data;

@Data
public class Update_roomDetails_Dto {
    // we will not change the room no. foor
    public int room_no;
    public String roomType;
    public int floor;
    public int beds;
    public int tables;
    public int chairs;
    public String coolers;
    public int monthlyRent;
    public String lightBill;
    public Integer arrearBill;
    public Integer lastMeterReading;
    public int total_rent_paid;
    public int securityAmount;
    public String securityAmountStatus;

}
