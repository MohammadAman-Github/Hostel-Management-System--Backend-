package org.example.hmsBackend.Services;

import org.example.hmsBackend.DTOs.*;
import org.example.hmsBackend.Models.roomDetails;
import org.example.hmsBackend.Models.students;
import org.example.hmsBackend.exception.DataNotFoundException;
import org.example.hmsBackend.exception.MRD_out_of_database;
import org.example.hmsBackend.exception.RoomNotFoundException;
import org.example.hmsBackend.exception.StudentNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface hms_service_interface {
    students createStudent(create_student_request_dto requestDto) throws StudentNotFoundException;
    students getStudentById(int id) throws StudentNotFoundException;
    students getStudentByName(String studentName) throws StudentNotFoundException;
    List<students> getallStudents();
    students updateStudentById(int studentId, updateStudentRequestDto requestDto) throws StudentNotFoundException;
    students updateStudentLeft(int student_id)throws StudentNotFoundException;
    void deleteStudentById(int studentId) throws StudentNotFoundException;

    roomDetails createRooms(CreateRoomDetailsDto CRD_DTO) throws RoomNotFoundException;
    List<roomDetails> getallRooms();
    roomDetails getRoomDetailsByroomNo(int roomNo)throws RoomNotFoundException;
    roomDetails updateRoomDetailsByroomNo(int roomNo, Update_roomDetails_Dto URD_Dto)throws RoomNotFoundException;
    void deleteRoomDetailsByroomNo(int roomNo)throws RoomNotFoundException;

    CreateMrdDto CreateMonthlyRentDetails(CreateMrdDto MRD_DTO)throws MRD_out_of_database;
    monthly_rent_details_dto getMrdByMonth_Year_RoomNo(String month, String year, int room_no) throws DataNotFoundException;

    void updateRoomOccupancy(Integer roomNo);
    void updateLastMeterReadingInRoomDetails(String month, String year, Integer roomNo);

    List<monthly_rent_details_dto> getMrdByMonthAndYear(String month, String year) throws MRD_out_of_database;
    List<monthly_rent_details_dto> getMrdByRoomNoAndYear(int room_no, String year) throws MRD_out_of_database;
    monthly_rent_details_dto updateMRD(String month, String year, int room_no, updateMRDdto updateMRDdto) throws  MRD_out_of_database;
    void deleteMRD(String month, String year, int room_no) throws MRD_out_of_database;

    String uploadStudentPdf(int studentId, MultipartFile file) throws StudentNotFoundException;

    String updateStudentPdf(int studentId, MultipartFile file) throws StudentNotFoundException;
    ResponseEntity<Resource> getStudentPdf(int studentId) throws StudentNotFoundException;

    // DATABASE BACKUP & RESTORE
    java.util.Map<String, Object> backupDatabase();

    void restoreDatabase(java.util.Map<String, Object> backupData);

}
