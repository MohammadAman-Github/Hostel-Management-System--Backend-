package org.example.hms.Models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "monthly_rent_details")
public class monthlyRentDetailsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    public int Id;

    @Column(name = "month")
    public String month;

    @Column(name = "year")
    public String year;

    @Column(name = "room_no")
    public Integer roomNo;

    @Column(name = "rent")
    public int rent;

    @Column(name = "current_reading")
    public int currentReading;

    @Column(name = "last_reading")
    public int lastReading;

    @Column(name = "total_light_bill")
    public int totalLightBill;

    @Column(name = "arrear_bill")
    public int arrearBill;

    @Column(name = "total_rent")
    public int totalRent;

    @Column(name = "total_rent_paid")
    public int totalRentPaid;

    @Column(name = "payment_status")
    public String paymentStatus;
}
