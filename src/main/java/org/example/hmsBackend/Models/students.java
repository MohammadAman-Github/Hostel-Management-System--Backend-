package org.example.hmsBackend.Models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "students")
@JsonPropertyOrder({
        "studentId",
        "studentName",
        "contactNo",
        "whatsappNo",
        "aadharNo",
        "fatherName",
        "fatherContact",
        "addressLine1",
        "addressLine2",
        "city",
        "state",
        "pincode",
        "roomNo",
        "joiningDate",
        "status",
        "leaving date",
        "pdfPath"
})
public class students {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    public int studentId;

    @Column(name = "student_name", nullable = false)
    public String studentName;

    @Column(name = "contact_no")
    public String contactNo;

    @Column(name = "whatsapp_no")
    public String whatsappNo;

    @Column(name = "aadhar_no")
    public String aadharNo;

    @Column(name = "father_name")
    public String fatherName;

    @Column(name = "father_contact")
    public String fatherContact;

    @Column(name = "address_line_1")
    public String addressLine1;

    @Column(name = "address_line_2")
    public String addressLine2;

    @Column(name = "city")
    public String city;

    @Column(name = "state")
    public String state;

    @Column(name = "pincode")
    public String pincode;

    @Column(name = "room_no")
    public Integer roomNo;

    @Column(name = "joining_date")
    public LocalDate joiningDate = LocalDate.now();

    @PrePersist
    public void setJoiningDate() {
        if (joiningDate == null) {
            joiningDate = LocalDate.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @Column(name = "status", nullable = false)
    public String status = "ACTIVE";

    @Column(name = "leaving_date")
    public LocalDate leavingDate;

    @Column(name = "pdf_path")
    public String pdfPath;
}


