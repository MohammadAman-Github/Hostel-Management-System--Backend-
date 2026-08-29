package org.example.hms.DTOs;

import lombok.Data;

@Data
public class CreateRoomDetailsDto {
    public int roomNo;
    public String roomType;
    public int floor;
    public int beds;
    public int tables;
    public int chairs;
    public String coolers;
    public int monthlyRent;
    public String lightBill;
    public int securityAmount;
    public String securityAmountStatus;
    public String occupancyStatus;
}
