package org.example.hmsBackend.Models;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "room_details")
public class roomDetails {

    @Id
    @Column(name = "room_no")
    public Integer roomNo;

    @Column (name = "room_type")
    public String roomType;

    @Column(name = "floor")
    public int floor;

    @Column(name = "beds")
    public int beds;

    @Column(name = "tables")
    public int tables;

    @Column(name = "chairs")
    public int chairs;

    @Column(name = "coolers")
    public String coolers;

    @Column(name = "monthly_rent")
    public int monthlyRent;

    @Column(name = "light_bill")
    public String lightBill;

    @Column(name = "lastMeterReading")
    public int lastMeterReading;

    @Column(name = "arrearBill")
    public int arrearBill;

    @Column(name = "security_amount")
    public int securityAmount;

    @Column(name = "security_amount_status")
    public String securityAmountStatus;

    @Column(name = "occupancy_status")
    public String occupancyStatus;

}
