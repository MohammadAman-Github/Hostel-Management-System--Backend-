package org.example.hmsBackend.Controller;

import org.example.hmsBackend.DTOs.*;
import org.example.hmsBackend.Models.roomDetails;
import org.example.hmsBackend.Models.students;
import org.example.hmsBackend.Services.hms_service;
import org.example.hmsBackend.exception.DataNotFoundException;
import org.example.hmsBackend.exception.MRD_out_of_database;
import org.example.hmsBackend.exception.RoomNotFoundException;
import org.example.hmsBackend.exception.StudentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(
        origins = "http://localhost:5173",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.PATCH,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
@RequestMapping("/hms")
public class hms_Controller {

    @Autowired
    public hms_service hms_service;

    // Creating Students
    @PostMapping("/studentDetails")
    public ResponseEntity<students> createStudent(@RequestBody create_student_request_dto requestDto)
            throws StudentNotFoundException
    {
        students Student = hms_service.createStudent(requestDto);
        return new ResponseEntity<>(Student, HttpStatusCode.valueOf(200));
    }

// code for updoad pdf


    @PostMapping("/student_id/{studentId}/pdf")
    public ResponseEntity<String> uploadStudentPdf(@PathVariable int studentId,
                                                   @RequestParam("file") MultipartFile file) throws StudentNotFoundException{

        try {
            String pdfPath = hms_service.uploadStudentPdf(studentId, file);

            return ResponseEntity.ok(pdfPath);

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // code for get pdf

    @GetMapping("/student_id/{studentId}/pdf")
    public ResponseEntity<Resource> getStudentPdf(@PathVariable int studentId) throws StudentNotFoundException {

        return hms_service.getStudentPdf(studentId);
    }

    @GetMapping("/allStudents")
    public ResponseEntity<List<students>> getAllStudents(){
        List<students> students = hms_service.getallStudents();
        return new ResponseEntity<>(students, HttpStatusCode.valueOf(200));

    }

    // Fetch students details by id
    @GetMapping("/student_id/{id}")
    public ResponseEntity<students> getStudent(@PathVariable("id") long id)
            throws StudentNotFoundException
    {
        students student = hms_service.getStudentById((int) id);
        return new ResponseEntity<>(student, HttpStatusCode.valueOf(200));
    }

    // Fetch students details by name
    @GetMapping("/student_name/{studentName}")
    public ResponseEntity<students> getStudent(@PathVariable("studentName") String studentName)
            throws StudentNotFoundException
    {
        students student = hms_service.getStudentByName(studentName);
        return new ResponseEntity<>(student, HttpStatusCode.valueOf(200));
    }


    // update student details by student id
    @PatchMapping("student_id/{studentId}")
    public ResponseEntity<students> updateStudent(@PathVariable("studentId") int studentId,
                                                  @RequestBody updateStudentRequestDto requestDto)
    throws StudentNotFoundException
    {
        students student = hms_service.updateStudentById(studentId, requestDto);
        return new ResponseEntity<>(student, HttpStatusCode.valueOf(200));
    }

    // update student details when the left

    @PatchMapping("/left_student_id/{student_id}")
    public ResponseEntity<students> updateStudentLeft(@PathVariable("student_id") int student_id) throws StudentNotFoundException{
        students student = hms_service.updateStudentLeft(student_id);
        return new ResponseEntity<>(student, HttpStatusCode.valueOf(200));
    }


    // update pfd

    @PutMapping("/{studentId}/pdf")
    public ResponseEntity<String> updateStudentPdf(@PathVariable int studentId, @RequestParam("file") MultipartFile file)
            throws StudentNotFoundException {

        try
        {
            String pdfPath = hms_service.updateStudentPdf(studentId, file);
            return ResponseEntity.ok(pdfPath);
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Delete student
    @DeleteMapping("/student_id/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable ("id") int id) throws StudentNotFoundException {
        hms_service.deleteStudentById(id);
        return ResponseEntity.ok("Student deleted successfully");
    }


    @PostMapping("/roomDetails")
    public ResponseEntity<roomDetails> createRooms(@RequestBody CreateRoomDetailsDto CRD_DTO) throws RoomNotFoundException{
        roomDetails Room = hms_service.createRooms(CRD_DTO);
        return new ResponseEntity<>(Room, HttpStatusCode.valueOf(200));
    }


    @GetMapping("/allRooms")
    public ResponseEntity<List<roomDetails>> getAllRooms(){
        List<roomDetails> allRooms = hms_service.getallRooms();
        return new ResponseEntity<>(allRooms, HttpStatusCode.valueOf(200));

    }

    // Fetch room details by room no
    @GetMapping("/roomDetails/{roomNo}")
    public ResponseEntity<roomDetails> getroomDetails(@PathVariable("roomNo") int roomNo)
            throws RoomNotFoundException
    {
        roomDetails roomDetails = hms_service.getRoomDetailsByroomNo(roomNo);
        return new ResponseEntity<>(roomDetails, HttpStatusCode.valueOf(200));
    }

    // Update room details by room no
    @PatchMapping("/roomDetails/{roomNo}")
    public ResponseEntity<roomDetails> updateroomDetails(@PathVariable ("roomNo") int roomNo,
                                                         @RequestBody Update_roomDetails_Dto URD_dto)
            throws RoomNotFoundException
    {
        roomDetails roomDetails = hms_service.updateRoomDetailsByroomNo(roomNo, URD_dto);
        return new ResponseEntity<>(roomDetails, HttpStatusCode.valueOf(200));
    }


    @DeleteMapping("roomNo/{room_no}")
    public ResponseEntity<String> deleteRoom(@PathVariable("room_no") int room_no) throws RoomNotFoundException{
        hms_service.deleteRoomDetailsByroomNo(room_no);
        return ResponseEntity.ok("Room No. " + room_no + " deleted successfully");
    }


    @PostMapping("/monthly_rent_details")
    public ResponseEntity<CreateMrdDto> CreateMonthlyRentDetails(@RequestBody CreateMrdDto MRD_DTO)
            throws MRD_out_of_database
    {
        CreateMrdDto MRD = hms_service.CreateMonthlyRentDetails(MRD_DTO);
        return new ResponseEntity<>(MRD, HttpStatusCode.valueOf(200));
    }


    @PatchMapping("/mrd/month/{month}/year/{year}/room_no/{room_no}")
    public ResponseEntity<monthly_rent_details_dto> updateMRD(@PathVariable ("month") String month,
                                                              @PathVariable ("year") String year,
                                                              @PathVariable ("room_no") int room_no,
                                                              @RequestBody updateMRDdto updateMRDdto)
            throws MRD_out_of_database{
        monthly_rent_details_dto updateMrd = hms_service.updateMRD(month, year, room_no, updateMRDdto);
        return new ResponseEntity<>(updateMrd, HttpStatusCode.valueOf(200));
    }

    @GetMapping("/mrd/month/{month}/year/{year}/room_no/{room_no}")
    public ResponseEntity<monthly_rent_details_dto> getMrdByMonth_Year_RoomNo(@PathVariable ("month") String month, @PathVariable ("year") String year,
                                                                        @PathVariable("room_no") int room_no)
    throws DataNotFoundException {
        monthly_rent_details_dto result = hms_service.getMrdByMonth_Year_RoomNo(month, year, room_no);
        return new ResponseEntity<>(result, HttpStatusCode.valueOf(200));

    }


    @GetMapping("/mrd/month/{month}/year/{year}")
    public ResponseEntity<List<monthly_rent_details_dto>> getMrdByMonthAndYear(@PathVariable("month") String month, @PathVariable("year") String year)
        throws MRD_out_of_database {
        List<monthly_rent_details_dto> allRooms = hms_service.getMrdByMonthAndYear(month, year);
        return new ResponseEntity<>(allRooms, HttpStatusCode.valueOf(200));
    }

    @GetMapping("/mrd/room_no/{room_no}/year/{year}")
    public ResponseEntity<List<monthly_rent_details_dto>> getMrdByRoom_NoAndYear(@PathVariable("room_no") int room_no, @PathVariable("year") String year)
            throws MRD_out_of_database {
        List<monthly_rent_details_dto> allRooms = hms_service.getMrdByRoomNoAndYear(room_no, year);
        return new ResponseEntity<>(allRooms, HttpStatusCode.valueOf(200));
    }

    @DeleteMapping("/mrd/month/{month}/year/{year}/room_no/{room_no}")
    public ResponseEntity<String> deleteMRD(
            @PathVariable String month,
            @PathVariable String year,
            @PathVariable int room_no)
            throws MRD_out_of_database {

        hms_service.deleteMRD(month, year, room_no);

        return ResponseEntity.ok(
                "Monthly rent deleted successfully"
        );
    }


}
