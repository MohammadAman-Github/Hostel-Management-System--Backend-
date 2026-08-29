package org.example.hmsBackend.DTOs;

import lombok.Data;

@Data
public class updateMRDdto
{
    public String paymentStatus;
//    public Integer lastReading;
//    public Integer currentReading;
    public Integer arrearBill;
}
