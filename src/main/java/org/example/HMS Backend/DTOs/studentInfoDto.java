package org.example.hms.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// we used this in monthly rent details page overview to get students basic details
@Data
@AllArgsConstructor
@NoArgsConstructor
public class studentInfoDto {
    private Integer studentId;
    private String studentName;
    private String contactNo;
    private String fatherName;
    private String fatherContact;
}
