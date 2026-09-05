package org.example.hmsBackend.DTOs;

import lombok.Data;

import java.time.LocalDate;

@Data
public class updateStudentRequestDto {
    public int studentId;
    public String studentName;
    public Integer roomNo;
    public String contactNo;
    public String whatsappNo;
    public String aadharNo;
    public String fatherName;
    public String fatherContact;
    public String addressLine1;
    public String addressLine2;
    public String city;
    public String state;
    public String pincode;
    public LocalDate joiningDate;
}
