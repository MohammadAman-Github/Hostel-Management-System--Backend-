package org.example.hmsBackend.Services;

import jakarta.transaction.Transactional;
import org.example.hmsBackend.DTOs.*;
import org.example.hmsBackend.Models.monthlyRentDetailsModel;
import org.example.hmsBackend.Models.roomDetails;
import org.example.hmsBackend.Models.students;
import org.example.hmsBackend.Repository.MRD_repository;
import org.example.hmsBackend.Repository.hms_repository;
import lombok.Data;
import org.example.hmsBackend.Repository.roomDetailsRepository;
import org.example.hmsBackend.exception.DataNotFoundException;
import org.example.hmsBackend.exception.MRD_out_of_database;
import org.example.hmsBackend.exception.RoomNotFoundException;
import org.example.hmsBackend.exception.StudentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.time.Month;
import java.time.YearMonth;

@Data
@Service
public class hms_service implements hms_service_interface {
    @Autowired
    hms_repository hms_repository;
    @Autowired
    roomDetailsRepository roomDetailsRepository;
    @Autowired
    MRD_repository mrd_repository;
    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public students createStudent(create_student_request_dto requestDto)
            throws StudentNotFoundException {
        Optional<roomDetails> room = roomDetailsRepository.findByRoomNo(requestDto.getRoomNo());
        if (room.isEmpty()) {
            throw new StudentNotFoundException("Room no. --> " + requestDto.getRoomNo() + " not found");
        }

        students s = hms_repository.findByStudentNameAndRoomNo(requestDto.getStudentName()
                , requestDto.getRoomNo());
        if (s != null) {
            throw new StudentNotFoundException("Student already exists");
        }
        students student = new students();
        student.setStudentName(requestDto.getStudentName());
        student.setRoomNo(requestDto.getRoomNo());
        student.setContactNo(requestDto.getContactNo());
        student.setAadharNo(requestDto.getAadharNo());
        student.setFatherName(requestDto.getFatherName());
        student.setFatherContact(requestDto.getFatherContact());
        student.setAddressLine1(requestDto.getAddressLine1());
        student.setAddressLine2(requestDto.getAddressLine2());
        student.setCity(requestDto.getCity());
        student.setState(requestDto.getState());
        student.setPincode(requestDto.getPincode());
        student.setJoiningDate(requestDto.getJoiningDate());
        student.setStatus("ACTIVE");
        student.setLeavingDate(null);
        student.setPdfPath(requestDto.getPdfPath());
        student = hms_repository.save(student);

        // Recalculate room occupancy
        updateRoomOccupancy(student.getRoomNo());

        return student;
    }

    @Override
    public students getStudentById(int id)
            throws StudentNotFoundException {
        Optional<students> optionalStudents = hms_repository.findById(id);
        if (optionalStudents.isPresent()) {
            students student = optionalStudents.get();
            return student;
        } else {
            throw new StudentNotFoundException("Student with studentId: " + id + " Not Found");
        }
    }

    @Override
    public students getStudentByName(String studentName)
            throws StudentNotFoundException {
        Optional<students> optionalStudents = hms_repository.findByStudentName(studentName);
        if (optionalStudents.isPresent()) {
            students student = optionalStudents.get();
            return student;
        } else {
            throw new StudentNotFoundException("Student with name --> " + studentName + " Not Found");
        }
    }


    @Override
    public List<students> getallStudents() {
        List<students> student = hms_repository.findAllByOrderByRoomNoAscStudentIdAsc();
        return student;
    }

    @Override
    public students updateStudentById(int studentId, updateStudentRequestDto requestDto) throws StudentNotFoundException {
        Optional<students> student = hms_repository.findByStudentIdIs(studentId);
        if (student.isEmpty()) {
            throw new StudentNotFoundException("Student with studentId: " + studentId + " Not Found");
        }

        students s = student.get();

        // Store old room number before changing it
        Integer oldRoomNo = s.getRoomNo();

        if (requestDto.getStudentName() != null) {
            s.setStudentName(requestDto.getStudentName());
        }

        if (requestDto.getRoomNo() != null) {
            s.setRoomNo(requestDto.getRoomNo());
        }

        if (requestDto.getContactNo() != null) {
            s.setContactNo(requestDto.getContactNo());
        }

        if (requestDto.getAadharNo() != null) {
            s.setAadharNo(requestDto.getAadharNo());
        }

        if (requestDto.getFatherName() != null) {
            s.setFatherName(requestDto.getFatherName());
        }

        if (requestDto.getFatherContact() != null) {
            s.setFatherContact(requestDto.getFatherContact());
        }

        if (requestDto.getAddressLine1() != null) {
            s.setAddressLine1(requestDto.getAddressLine1());
        }

        if (requestDto.getAddressLine2() != null) {
            s.setAddressLine2(requestDto.getAddressLine2());
        }

        if (requestDto.getCity() != null) {
            s.setCity(requestDto.getCity());
        }

        if (requestDto.getState() != null) {
            s.setState(requestDto.getState());
        }

        if (requestDto.getStudentName() != null) {
            s.setStudentName(requestDto.getStudentName());
        }

        if (requestDto.getJoiningDate() != null) {
            s.setJoiningDate(requestDto.getJoiningDate());
        }

        s = hms_repository.save(s);

        // New room number after update
        Integer newRoomNo = s.getRoomNo();

//        // Recalculate old room occupancy
//        if (oldRoomNo != null) {
//            updateRoomOccupancy(oldRoomNo);
//        }
//
//        // Recalculate new room occupancy
//        if (newRoomNo != null) {
//            updateRoomOccupancy(newRoomNo);
//        }

        if (oldRoomNo != null && !oldRoomNo.equals(newRoomNo))
        {
            updateRoomOccupancy(oldRoomNo);
            updateRoomOccupancy(newRoomNo);
        }
        return s;

    }

    @Override
    public students updateStudentLeft(int student_id) throws StudentNotFoundException {
        Optional<students> student = hms_repository.findById(student_id);
        if (student.isEmpty()) {
            throw new StudentNotFoundException("Student with studentId: " + student_id + " Not Found");
        }
        students s = student.get();
        if ("Left".equals(s.getStatus())) {
            throw new StudentNotFoundException("Student with studentId: " + student_id + " has already left on  " + s.getLeavingDate());
        }

        s.setStatus("Left");
        s.setLeavingDate(LocalDate.now());

        // store this student room no. for setting occupancy status calculations

        Integer old_Room_No = s.getRoomNo();

        // Now Mark room_no -->> null when student has left the room
        s.setRoomNo(null);
        s = hms_repository.save(s);

        // Recalculate room occupancy
        updateRoomOccupancy(old_Room_No);

        return s;
    }

    @Override
    public void deleteStudentById(int studentId) throws StudentNotFoundException {
        Optional<students> optionalStudent = hms_repository.findById(studentId);

        if (hms_repository.findById(studentId).isEmpty()) {
            throw new StudentNotFoundException("Student with studentId: " + studentId + " Not Found");
        }

        // get student details
        students student = optionalStudent.get();

        // Save room number before deleting student
        Integer oldRoomNo = student.getRoomNo();

        // Get PDF path
        String pdfPath = student.getPdfPath();

        // Delete PDF if it exists
        if (pdfPath != null && !pdfPath.isEmpty()) {

            try
            {
                Path path = Paths.get(pdfPath);
                if (Files.exists(path))
                {
                    Files.delete(path);
                    System.out.println("Student PDF deleted: " + pdfPath);
                }

            }
            catch (IOException e)
            {
                System.err.println("Unable to delete student PDF: " + pdfPath);
            }
        }

        // Delete student
        hms_repository.deleteById(studentId);

        // Update room occupancy
        if (oldRoomNo != null) {
            updateRoomOccupancy(oldRoomNo);
        }

    }

    @Override
    public roomDetails createRooms(CreateRoomDetailsDto CRD_DTO) throws RoomNotFoundException {
        Optional<roomDetails> OptionalRoomDetails = roomDetailsRepository.findByRoomNo(CRD_DTO.roomNo);
        if (OptionalRoomDetails.isPresent()) {
            throw new RoomNotFoundException("Room No --> " + CRD_DTO.roomNo + " allready exists");
        }
        if (!"Single".equals(CRD_DTO.getRoomType()) && !"Double".equals(CRD_DTO.getRoomType()) && !"Tripple".equals(CRD_DTO.getRoomType())) {
            throw new RoomNotFoundException("Room type should be Single, Double or Tripple.\n" + "Spelling should be exactly same");
        }

        roomDetails room = new roomDetails();
        room.setRoomNo(CRD_DTO.getRoomNo());
        room.setRoomType(CRD_DTO.getRoomType());
        room.setFloor(CRD_DTO.getFloor());
        room.setBeds(CRD_DTO.getBeds());
        room.setTables(CRD_DTO.getTables());
        room.setChairs(CRD_DTO.getChairs());
        room.setCoolers(CRD_DTO.getCoolers());
        room.setMonthlyRent(CRD_DTO.getMonthlyRent());
        room.setLightBill(CRD_DTO.getLightBill());
        room.setSecurityAmount(CRD_DTO.getSecurityAmount());
        room.setSecurityAmountStatus(CRD_DTO.getSecurityAmountStatus());
        room.setOccupancyStatus(CRD_DTO.getOccupancyStatus());

        room = roomDetailsRepository.save(room);


        return room;
    }


    @Override
    public List<roomDetails> getallRooms() {
        List<roomDetails> allRooms = roomDetailsRepository.findAllByOrderByRoomNoAsc();
        return allRooms;
    }


    @Override
    public roomDetails getRoomDetailsByroomNo(int roomNo)
            throws RoomNotFoundException {
        Optional<roomDetails> OptionalroomDetails = roomDetailsRepository.findByRoomNo(roomNo);
        if (OptionalroomDetails.isPresent()) {
            roomDetails roomDetails = OptionalroomDetails.get();
            return roomDetails;
        } else {
            throw new RoomNotFoundException("Room No --> " + roomNo + " Not Found");
        }
    }

    @Override
    public roomDetails updateRoomDetailsByroomNo(int roomNo, Update_roomDetails_Dto URD_Dto)
            throws RoomNotFoundException {
        Optional<roomDetails> optionalRoomDetails = roomDetailsRepository.findByRoomNo(roomNo);
        if (optionalRoomDetails.isEmpty()) {
            throw new RoomNotFoundException("Room No --> " + roomNo + " Not Found");
        } else {
            // we will not change the room no. foor
            roomDetails room = optionalRoomDetails.get();
            if (URD_Dto.getRoomType() != null) {
                room.setRoomType(URD_Dto.getRoomType());
            }
            if (URD_Dto.getFloor() != 0) {
                room.setFloor(URD_Dto.getFloor());
            }
            if (URD_Dto.getBeds() != 0) {
                room.setBeds(URD_Dto.getBeds());
            }
            if (URD_Dto.getTables() != 0) {
                room.setTables(URD_Dto.getTables());
            }
            if (URD_Dto.getChairs() != 0) {
                room.setChairs(URD_Dto.getChairs());
            }
            if (URD_Dto.getCoolers() != null) {
                room.setCoolers(URD_Dto.getCoolers());
            }
            if (URD_Dto.getMonthlyRent() != 0) {
                room.setMonthlyRent(URD_Dto.getMonthlyRent());
            }
            if (URD_Dto.getLightBill() != null) {
                room.setLightBill(URD_Dto.getLightBill());
            }
            if (URD_Dto.getLastMeterReading() != null) {
                room.setLastMeterReading(URD_Dto.getLastMeterReading());
            }
            if (URD_Dto.getSecurityAmount() != 0) {
                room.setSecurityAmount(URD_Dto.getSecurityAmount());
            }

            if (URD_Dto.getSecurityAmountStatus() != null) {
                room.setSecurityAmountStatus(URD_Dto.getSecurityAmountStatus());
            }
            if (URD_Dto.getArrearBill() != null) {
                room.setArrearBill(URD_Dto.getArrearBill());
            }
            room = roomDetailsRepository.save(room);

            // Recalculate occupancy after room details are updated
            updateRoomOccupancy(roomNo);
            return room;
        }
    }

    @Override
    public void deleteRoomDetailsByroomNo(int roomNo) throws RoomNotFoundException {

        Optional<roomDetails> optionalRoomDetails = roomDetailsRepository.findByRoomNo(roomNo);

        if (optionalRoomDetails.isEmpty())
        {
            throw new RoomNotFoundException("Room No --> " + roomNo + " Not Found");
        }


        // =========================
        // CHECK ACTIVE STUDENTS
        // =========================

        long activeStudents = hms_repository.countByRoomNoAndStatus(roomNo, "ACTIVE");

        if (activeStudents > 0)
        {
            throw new IllegalStateException("Room " + roomNo
                            + " cannot be deleted because "
                            + activeStudents
                            + " active student"
                            + (activeStudents > 1 ? "s are" : " is")
                            + " assigned to this room.");
        }


        // =========================
        // CHECK MONTHLY RENT HISTORY
        // =========================

        long rentRecords = mrd_repository.countByRoomNo(roomNo);

        if (rentRecords > 0) {

            throw new IllegalStateException("Room " + roomNo
                            + " cannot be deleted because monthly rent history exists for this room."
            );
        }


        // =========================
        // DELETE ROOM
        // =========================

        roomDetails room = optionalRoomDetails.get();

        roomDetailsRepository.delete(room);
    }


    @Override
    public void updateRoomOccupancy(Integer roomNo) {

        // count active students in the room
        int active_Students = hms_repository.countByRoomNoAndStatus(roomNo, "ACTIVE");

        // get room details
        Optional<roomDetails> roomDetails = roomDetailsRepository.findByRoomNo(roomNo);

        if (roomDetails.isEmpty()) {
            return;
        }

        roomDetails room = roomDetails.get();

        // get total bed in the room
        int total_bed = room.getBeds();

        // occupancy status rules

        if (active_Students == 0) {
            room.setOccupancyStatus("VACANT");

        } else if (active_Students == 1) {
            room.setOccupancyStatus("SINGLE OCCUPANCY");

        } else if (active_Students == 2) {
            room.setOccupancyStatus("DOUBLE OCCUPANCY");

        } else if (active_Students == 3) {
            room.setOccupancyStatus("TRIPLE OCCUPANCY");

        } else {
            // Optional: for 4 or more students
            room.setOccupancyStatus(active_Students + " OCCUPANCY");
        }

        roomDetailsRepository.save(room);


    }


    @Override
    public void updateLastMeterReadingInRoomDetails(String month, String year, Integer roomNo) {
        Optional<roomDetails> roomDetails = roomDetailsRepository.findByRoomNo(roomNo);
        if (roomDetails.isEmpty()) {
            return;
        }

        Optional<monthlyRentDetailsModel> MRD = mrd_repository.findMRDByMonthAndYearAndRoomNo(month, year, roomNo);

        if (MRD.isEmpty()) {
            return;
        }
        monthlyRentDetailsModel mrd = MRD.get();
        roomDetails room = roomDetails.get();
        room.setLastMeterReading(mrd.getCurrentReading());
        room.setArrearBill(mrd.getArrearBill());

        roomDetailsRepository.save(room);

    }


    @Override
    public CreateMrdDto CreateMonthlyRentDetails(CreateMrdDto MRD_DTO)
            throws MRD_out_of_database {

        // Check whether details for this room and ( month + year) already exist
        Optional<monthlyRentDetailsModel> MRD_Detail = mrd_repository.findMRDByMonthAndYearAndRoomNo
                (MRD_DTO.getMonth(), MRD_DTO.getYear(), MRD_DTO.getRoomNo());
        if (MRD_Detail.isPresent()) {
            throw new MRD_out_of_database("Details for Room No --> " + MRD_DTO.getRoomNo() +
                    " for Month " + MRD_DTO.getMonth() + " - " + MRD_DTO.getYear() + " are already filled");
        }

        // Check that MRD cannot be created for a month
        // earlier than an already-created month for this room

        YearMonth requestedMonth;

        try {
            Month month = Month.valueOf(MRD_DTO.getMonth().toUpperCase(Locale.ENGLISH));

            requestedMonth = YearMonth.of(Integer.parseInt(MRD_DTO.getYear()), month);

        } catch (Exception e) {
            throw new MRD_out_of_database("Invalid month or year. Rent can only be created for the current or upcoming month.");
        }


        List<monthlyRentDetailsModel> existingMRDs = mrd_repository.findByRoomNoOrderByIdAsc(MRD_DTO.getRoomNo());


        for (monthlyRentDetailsModel existingMRD : existingMRDs) {

            YearMonth existingMonth;

            try {
                Month month = Month.valueOf(existingMRD.getMonth().toUpperCase(Locale.ENGLISH));

                existingMonth = YearMonth.of(Integer.parseInt(existingMRD.getYear()), month);

            } catch (Exception e) {

                continue;
            }

            if (existingMonth.isAfter(requestedMonth)) {
                throw new MRD_out_of_database("Rent for " + existingMRD.getMonth() + " " + existingMRD.getYear() +
                        " already exists for Room No --> " + MRD_DTO.getRoomNo() +
                        ". You cannot create rent for " + MRD_DTO.getMonth() + " " + MRD_DTO.getYear() + " now.");
            }
        }

        Optional<roomDetails> optionalRoomDetails = roomDetailsRepository.findByRoomNo(MRD_DTO.getRoomNo());

        // Check whether the room is exist in the table or not
        if (optionalRoomDetails.isEmpty()) {
            throw new RoomNotFoundException("Room No --> " + MRD_DTO.getRoomNo() + " Not Found");
        }

        List<students> students = hms_repository.findByRoomNo(MRD_DTO.getRoomNo());

        if (students == null || students.isEmpty()) {
            throw new MRD_out_of_database("Room " + MRD_DTO.getRoomNo() + " is empty. Please assign a " +
                    "student to this room before creating monthly rent.");
        }

//        if (MRD_DTO.currentReading < 0 || MRD_DTO.currentReading < room.getLastMeterReading())
//        {
//            throw new MRD_out_of_database("Current Meter Reading Can't be less than 0");
//        }
        if (MRD_DTO.getCurrentReading() < 0) {
            throw new MRD_out_of_database(
                    "Current Meter Reading Can't be less than 0"
            );
        }


        // creating object for roomDetails so that we can fetch
        // the room rent of the room form room details table

        roomDetails room = optionalRoomDetails.get();

        if (MRD_DTO.getCurrentReading() < room.getLastMeterReading()) {
            throw new MRD_out_of_database(
                    "Current Meter Reading Can't be less than Last Meter Reading"
            );
        }

        if (MRD_DTO.rent < 0) {
            throw new MRD_out_of_database("Rent Can't be less than 0");
        }
        if (!"Pending".equals(MRD_DTO.getPaymentStatus())) {
            throw new MRD_out_of_database("Set Payment Status to Pending");
        }

        // fetching room rent
        int roomRent;
        if (MRD_DTO.getRent() != 0 && MRD_DTO.getRent() != room.getMonthlyRent()) {
            roomRent = MRD_DTO.getRent(); // if we want to change the rent for a perticular month

        } else {
            roomRent = room.getMonthlyRent(); // fixed room rent form room details table
        }


        // calculating total electricity units

        int lastMeterReading = room.getLastMeterReading();  //50

        int ArrearBill = room.getArrearBill();

        int currentMeterReading = MRD_DTO.getCurrentReading();

        int totalUnits = currentMeterReading - lastMeterReading;  // 10

        // calculating total light bill

        int totalLightBill = totalUnits * 10; // 100

        // calculating total rent

        int totalRent = totalLightBill + ArrearBill + roomRent;

        //                   creating mrd object

        monthlyRentDetailsModel mrd = new monthlyRentDetailsModel();

        //------------------ filling details in mrd --------------------//

        mrd.setMonth(MRD_DTO.getMonth());
        mrd.setYear(MRD_DTO.getYear());
        mrd.setRoomNo(MRD_DTO.getRoomNo());
        mrd.setRent(roomRent);
        mrd.setLastReading(lastMeterReading);
        mrd.setCurrentReading(currentMeterReading);
        mrd.setTotalLightBill(totalLightBill);
        mrd.setArrearBill(MRD_DTO.getArrearBill());
        mrd.setTotalRent(totalRent);
        mrd.setPaymentStatus(MRD_DTO.getPaymentStatus());

// -------------- saving the details ( Now it will show in database ) ----------------//

        mrd = mrd_repository.save(mrd);

// ------------ calling function so that it will update the lastMeterReading in roomDetails table -------//

        updateLastMeterReadingInRoomDetails(MRD_DTO.getMonth(), MRD_DTO.getYear(), MRD_DTO.getRoomNo());

        // ---------------   we will show this dto as output ---------------------//

        CreateMrdDto MrdDto = new CreateMrdDto();
        MrdDto.setMonth(mrd.getMonth());
        MrdDto.setYear(mrd.getYear());
        MrdDto.setRoomNo(mrd.getRoomNo());
        MrdDto.setRent(mrd.getRent());
        MrdDto.setLastReading(mrd.getLastReading());
        MrdDto.setCurrentReading(mrd.getCurrentReading());
        MrdDto.setTotalLightBill(mrd.getTotalLightBill());
        MrdDto.setArrearBill(room.getArrearBill());
        MrdDto.setTotalRent(mrd.getTotalRent());
        MrdDto.setPaymentStatus(mrd.getPaymentStatus());

        return MrdDto;
    }


    @Override
    public monthly_rent_details_dto getMrdByMonth_Year_RoomNo(String month, String year, int room_no) throws DataNotFoundException {
        Optional<monthlyRentDetailsModel> MRD = mrd_repository.findMRDByMonthAndYearAndRoomNo(month, year, room_no);
        if (MRD.isEmpty()) {
            throw new DataNotFoundException("Data for Room No --> " + room_no + " , " + month + " - " + year + " Not Found");
        }

        // get the monthly rent object

        monthlyRentDetailsModel mrd = MRD.get();

        // get students staying in room
        List<students> studentList = hms_repository.findByRoomNo(room_no);

        // create Dto

        monthly_rent_details_dto dto = new monthly_rent_details_dto();
        dto.setRoomNo(mrd.getRoomNo());
        dto.setRent(mrd.getRent());
        dto.setMonth(mrd.getMonth());
        dto.setYear(mrd.getYear());
        dto.setArrearBill(mrd.getArrearBill());
        dto.setLastReading(mrd.getLastReading());
        dto.setCurrentReading(mrd.getCurrentReading());
        dto.setTotalLightBill(mrd.getTotalLightBill());
        dto.setTotalRent(mrd.getTotalRent());
        dto.setTotalRentPaid(mrd.getTotalRentPaid());
        dto.setPaymentStatus(mrd.getPaymentStatus());


//      Extract student ID and student name
        List<studentInfoDto> studentDetails = new ArrayList<>();

        for (students student : studentList) {

            studentInfoDto studentDto = new studentInfoDto();

            studentDto.setStudentId(student.getStudentId());
            studentDto.setStudentName(student.getStudentName());
            studentDto.setContactNo(student.getContactNo());
            studentDto.setFatherName(student.getFatherName());
            studentDto.setFatherContact(student.getFatherContact());

            studentDetails.add(studentDto);
        }

        dto.setStudents(studentDetails);

        return dto;
    }


    @Override
    public List<monthly_rent_details_dto> getMrdByMonthAndYear(String month, String year) throws MRD_out_of_database {
        List<monthlyRentDetailsModel> MRD = mrd_repository.findByMonthAndYearOrderByRoomNoAsc(month, year);
        if ((MRD.isEmpty())) {
            throw new MRD_out_of_database("Data for --> " + month + " - " + year + " Not Found");
        }

        List<monthly_rent_details_dto> MRDdtoList = new ArrayList<>();

        for (monthlyRentDetailsModel mrd : MRD) {

            monthly_rent_details_dto dto =
                    new monthly_rent_details_dto();

            dto.setMonth(mrd.getMonth());
            dto.setYear(mrd.getYear());
            dto.setRoomNo(mrd.getRoomNo());
            dto.setRent(mrd.getRent());
            dto.setLastReading(mrd.getLastReading());
            dto.setCurrentReading(mrd.getCurrentReading());
            dto.setTotalLightBill(mrd.getTotalLightBill());
            dto.setArrearBill(mrd.getArrearBill());
            dto.setTotalRent(mrd.getTotalRent());
            dto.setTotalRentPaid(mrd.getTotalRentPaid());
            dto.setPaymentStatus(mrd.getPaymentStatus());

            MRDdtoList.add(dto);
        }

        return MRDdtoList;
    }

    @Override
    public List<monthly_rent_details_dto> getMrdByRoomNoAndYear(int room_no, String year) throws MRD_out_of_database {
        List<monthlyRentDetailsModel> MRD = mrd_repository.findByRoomNoAndYearOrderByIdAsc(room_no, year);;
        if ((MRD.isEmpty())) {
            throw new MRD_out_of_database("Data for Room No --> " + room_no + " , Year --> " + year + "  Not Found");
        }

        List<monthly_rent_details_dto> MRDdtoList = new ArrayList<>();

        for (monthlyRentDetailsModel mrd : MRD) {

            monthly_rent_details_dto dto = new monthly_rent_details_dto();

            dto.setMonth(mrd.getMonth());
            dto.setYear(mrd.getYear());
            dto.setRoomNo(mrd.getRoomNo());
            dto.setRent(mrd.getRent());
            dto.setLastReading(mrd.getLastReading());
            dto.setCurrentReading(mrd.getCurrentReading());
            dto.setTotalLightBill(mrd.getTotalLightBill());
            dto.setArrearBill(mrd.getArrearBill());
            dto.setTotalRent(mrd.getTotalRent());
            dto.setTotalRentPaid(mrd.getTotalRentPaid());
            dto.setPaymentStatus(mrd.getPaymentStatus());

            MRDdtoList.add(dto);
        }

        return MRDdtoList;
    }

    @Override
    public monthly_rent_details_dto updateMRD(String month, String year, int room_no, updateMRDdto updateMRDdto) throws MRD_out_of_database {
        Optional<monthlyRentDetailsModel> MRD = mrd_repository.findMRDByMonthAndYearAndRoomNo(month, year, room_no);
        if (MRD.isEmpty()) {
            throw new MRD_out_of_database("Data for Month " + month + " - " + year + " Not Found");
        }
        if (updateMRDdto.getArrearBill() < 0) {
            throw new MRD_out_of_database("Arrear Bill Can't be less than 0");
        }


        monthlyRentDetailsModel mrd = MRD.get();

        int TempTotalRent = mrd.getTotalRent();

        if (updateMRDdto.getPaymentStatus() != null) {
            mrd.setPaymentStatus(updateMRDdto.getPaymentStatus());
        }

        if (updateMRDdto.getArrearBill() != null) {
            mrd.setArrearBill(updateMRDdto.getArrearBill());
        }


        // =========================
        // RECALCULATE BILL
        // =========================

        int totalUnits = mrd.getCurrentReading() - mrd.getLastReading();

        int totalLightBill = totalUnits * 10;

        int totalRent = TempTotalRent;

        mrd.setTotalRent(totalRent);
        int totalRentPaid = totalRent - updateMRDdto.getArrearBill();
        mrd.setTotalRentPaid(totalRentPaid);

        mrd.setTotalLightBill(totalLightBill);

        mrd = mrd_repository.save(mrd);

        updateLastMeterReadingInRoomDetails(month, year, room_no);


        // convert monthly_rent_details Entity ( mrd ) into a new Dto  ( because we are returning a dto )

        monthly_rent_details_dto dto = new monthly_rent_details_dto();

        dto.setMonth(mrd.getMonth());
        dto.setYear(mrd.getYear());
        dto.setRoomNo(mrd.getRoomNo());
        dto.setRent(mrd.getRent());
        dto.setLastReading(mrd.getLastReading());
        dto.setCurrentReading(mrd.getCurrentReading());
        dto.setTotalLightBill(mrd.getTotalLightBill());
        dto.setArrearBill(mrd.getArrearBill());
        dto.setTotalRent(mrd.getTotalRent());
        dto.setTotalRentPaid(mrd.getTotalRentPaid());
        dto.setPaymentStatus(mrd.getPaymentStatus());

        return dto;
    }

    @Override
    @Transactional
    public void deleteMRD(String month, String year, int room_no)
            throws MRD_out_of_database {

        // ==========================================
        // FIND MRD TO DELETE
        // ==========================================

        Optional<monthlyRentDetailsModel> mrdOptional = mrd_repository.findMRDByMonthAndYearAndRoomNo(month, year, room_no);

        if (mrdOptional.isEmpty()) {
            throw new MRD_out_of_database("Data for Room No --> " + room_no +
                    " , " + month + " - " + year + " Not Found");
        }


        // ==========================================
        // DELETE THE SELECTED MRD
        // ==========================================

        mrd_repository.delete(mrdOptional.get());


        // ==========================================
        // FIND ROOM
        // ==========================================

        Optional<roomDetails> roomOptional = roomDetailsRepository.findByRoomNo(room_no);

        if (roomOptional.isEmpty()) {
            return;
        }

        roomDetails room = roomOptional.get();


        // ==========================================
        // FIND REMAINING MRDs FOR THIS ROOM
        // ==========================================

        List<monthlyRentDetailsModel> remainingMRDs = mrd_repository.findByRoomNoOrderByIdAsc(room_no);


        // ==========================================
        // IF NO MRD REMAINS
        // ==========================================

        if (remainingMRDs.isEmpty()) {
            return;
        }


        // ==========================================
        // FIND LATEST REMAINING MRD
        // ==========================================

        monthlyRentDetailsModel latestMRD = null;

        YearMonth latestMonth = null;


        for (monthlyRentDetailsModel mrd : remainingMRDs) {

            try {

                Month monthEnum = Month.valueOf(mrd.getMonth().toUpperCase(Locale.ENGLISH));

                YearMonth mrdYearMonth = YearMonth.of(Integer.parseInt(mrd.getYear()), monthEnum);


                if (latestMonth == null || mrdYearMonth.isAfter(latestMonth)) {
                    latestMonth = mrdYearMonth;
                    latestMRD = mrd;
                }

            } catch (Exception e) {

                // Ignore invalid month/year records
            }
        }


        // ==========================================
        // RESTORE ROOM DETAILS
        // ==========================================

        if (latestMRD != null) {
            // Restore last meter reading
            room.setLastMeterReading(latestMRD.getCurrentReading());

            // Restore arrear bill
            room.setArrearBill(latestMRD.getArrearBill());

        } else {
            // No valid previous MRD found
            room.setLastMeterReading(0);
            room.setArrearBill(0);
        }

        // ==========================================
        // SAVE ROOM DETAILS
        // ==========================================

        roomDetailsRepository.save(room);
    }

    @Override
    public String uploadStudentPdf(int studentId, MultipartFile file) throws StudentNotFoundException {
        Optional<students> optionalStudent = hms_repository.findById(studentId);

        if (optionalStudent.isEmpty()) {
            throw new StudentNotFoundException("Student not found");
        }

        students student = optionalStudent.get();

        if (file == null || file.isEmpty()) {
            throw new StudentNotFoundException("PDF file is empty");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new StudentNotFoundException("Only PDF files are allowed");
        }

        // =========================
        // CREATE STUDENT FOLDER
        // =========================

        String uploadDir =
                "uploads/students/";

        Path studentFolder = Paths.get(uploadDir + studentId);

        try {

            Files.createDirectories(studentFolder);


            // =========================
            // PDF FILE NAME
            // =========================

            String fileName = "student_" + studentId + ".pdf";

            // =========================
            // COMPLETE FILE PATH
            // =========================

            Path filePath = studentFolder.resolve(fileName);

            // =========================
            // SAVE PDF
            // =========================

            Files.write(filePath, file.getBytes());


            // =========================
            // SAVE PATH IN DATABASE
            // =========================

            String pdfPath = filePath.toString();

            student.setPdfPath(pdfPath);

            student.setPdfPath(pdfPath);

            hms_repository.save(student);

            return pdfPath;
        }
        catch (IOException e) {throw new RuntimeException("Failed to save PDF file", e);
        }
    }

    @Override
    public String updateStudentPdf(int studentId, MultipartFile file) throws StudentNotFoundException {
        // Find student
        Optional<students> optionalStudent = hms_repository.findById(studentId);

        if (optionalStudent.isEmpty())
        {
            throw new StudentNotFoundException("Student with studentId: " + studentId + " Not Found");
        }

        students student = optionalStudent.get();


        // Check file
        if (file == null || file.isEmpty())
        {
            throw new StudentNotFoundException("PDF file is empty");
        }


        // Check PDF
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()))
        {
            throw new StudentNotFoundException("Only PDF files are allowed");
        }


        // Delete old PDF
        String oldPdfPath = student.getPdfPath();

        if (oldPdfPath != null && !oldPdfPath.isEmpty()) {

            try
            {
                Path oldPath = Paths.get(oldPdfPath);

                if (Files.exists(oldPath))
                {
                    Files.delete(oldPath);
                    System.out.println("Old PDF deleted: " + oldPdfPath);
                }

            }
            catch (IOException e)
            {
                throw new RuntimeException("Unable to delete old PDF", e);
            }
        }


        // Create folder
        Path uploadDirectory = Paths.get("uploads", "students", String.valueOf(studentId));

        try
        {
            Files.createDirectories(uploadDirectory);

            // New PDF name
            String fileName = "student_" + studentId + ".pdf";

            // Complete new PDF path
            Path newPdfPath = uploadDirectory.resolve(fileName);


            // Save new PDF
            Files.copy(file.getInputStream(), newPdfPath, StandardCopyOption.REPLACE_EXISTING);

            // Save path in database
            String pdfPath = newPdfPath.toString();

            student.setPdfPath(pdfPath);

            hms_repository.save(student);


            return pdfPath;

        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to save new PDF", e);
        }
    }

    @Override
    public ResponseEntity<Resource> getStudentPdf(int studentId) throws StudentNotFoundException {
        Optional<students> optionalStudent = hms_repository.findById(studentId);

        if (optionalStudent.isEmpty())
        {
            throw new StudentNotFoundException("Student not found");
        }

        students student = optionalStudent.get();

        String pdfPath = student.getPdfPath();

        if (pdfPath == null || pdfPath.isEmpty())
        {
            throw new StudentNotFoundException("PDF not found for this student");
        }

        try
        {
            Path path = Paths.get(pdfPath);

            if (!Files.exists(path))
            {
                throw new StudentNotFoundException("PDF file does not exist");
            }

            Resource resource =
                    new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName()
                                    + "\"")
                    .contentType(MediaType.APPLICATION_PDF).body(resource);

        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to open PDF", e);
        }
    }

    // ==========================================
    // DATABASE BACKUP
    // ==========================================

    @Override
    public Map<String, Object> backupDatabase() {

        Map<String, Object> backup = new LinkedHashMap<>();

        backup.put("database", "hms");
        backup.put("version", 1);
        backup.put("encrypted", false);
        backup.put("mode", "full");


        // ==========================================
        // TABLES
        // ==========================================

        List<Map<String, Object>> tables = new ArrayList<>();


        // ==========================================
        // ROOM DETAILS
        // ==========================================

        Map<String, Object> roomTable = new LinkedHashMap<>();

        roomTable.put(
                "name",
                "room_details"
        );

        List<Map<String, String>> roomSchema = new ArrayList<>();

        roomSchema.add(Map.of(
                "column", "room_no",
                "value", "INTEGER PRIMARY KEY"
        ));

        roomSchema.add(Map.of(
                "column", "room_type",
                "value", "TEXT"
        ));

        roomSchema.add(Map.of(
                "column", "floor",
                "value", "INTEGER"
        ));

        roomSchema.add(Map.of(
                "column", "beds",
                "value", "INTEGER"
        ));

        roomSchema.add(Map.of(
                "column", "tables",
                "value", "INTEGER"
        ));

        roomSchema.add(Map.of(
                "column", "chairs",
                "value", "INTEGER"
        ));

        roomSchema.add(Map.of(
                "column", "coolers",
                "value", "TEXT"
        ));

        roomSchema.add(Map.of(
                "column", "monthly_rent",
                "value", "INTEGER"
        ));

        roomSchema.add(Map.of(
                "column", "light_bill",
                "value", "TEXT"
        ));

        roomSchema.add(Map.of(
                "column", "lastMeterReading",
                "value", "INTEGER"
        ));

        roomSchema.add(Map.of(
                "column", "arrearBill",
                "value", "INTEGER"
        ));

        roomSchema.add(Map.of(
                "column", "security_amount",
                "value", "INTEGER"
        ));

        roomSchema.add(Map.of(
                "column", "security_amount_status",
                "value", "TEXT"
        ));

        roomSchema.add(Map.of(
                "column", "occupancy_status",
                "value", "TEXT"
        ));

        roomTable.put(
                "schema",
                roomSchema
        );


        List<List<Object>> roomValues = new ArrayList<>();

        for (roomDetails room : roomDetailsRepository.findAllByOrderByRoomNoAsc()) {

            roomValues.add(Arrays.asList(
                    room.roomNo,
                    room.roomType,
                    room.floor,
                    room.beds,
                    room.tables,
                    room.chairs,
                    room.coolers,
                    room.monthlyRent,
                    room.lightBill,
                    room.lastMeterReading,
                    room.arrearBill,
                    room.securityAmount,
                    room.securityAmountStatus,
                    room.occupancyStatus
            ));
        }

        roomTable.put(
                "values",
                roomValues
        );

        tables.add(roomTable);


        // ==========================================
        // STUDENTS
        // ==========================================

        Map<String, Object> studentsTable = new LinkedHashMap<>();

        studentsTable.put(
                "name",
                "students"
        );

        List<Map<String, String>> studentsSchema = new ArrayList<>();

        studentsSchema.add(Map.of(
                "column", "student_id",
                "value", "INTEGER PRIMARY KEY AUTOINCREMENT"
        ));

        studentsSchema.add(Map.of(
                "column", "student_name",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "contact_no",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "aadhar_no",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "father_name",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "father_contact",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "address_line_1",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "address_line_2",
                "value", "TEXT"
        ));

        studentsSchema.add(Map.of(
                "column", "city",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "state",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "pincode",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "room_no",
                "value", "INTEGER"
        ));

        studentsSchema.add(Map.of(
                "column", "joining_date",
                "value", "TEXT NOT NULL"
        ));

        studentsSchema.add(Map.of(
                "column", "leaving_date",
                "value", "TEXT"
        ));

        studentsSchema.add(Map.of("column", "status",
                "value", "TEXT NOT NULL"));

        studentsTable.put("schema", studentsSchema);


        List<List<Object>> studentValues = new ArrayList<>();

        for (students student : hms_repository.findAllByOrderByRoomNoAscStudentIdAsc()) {

            studentValues.add(Arrays.asList(
                    student.studentId,
                    student.studentName,
                    student.contactNo,
                    student.aadharNo,
                    student.fatherName,
                    student.fatherContact,
                    student.addressLine1,
                    student.addressLine2,
                    student.city,
                    student.state,
                    student.pincode,
                    student.roomNo,
                    student.joiningDate,
                    student.leavingDate,
                    student.status
            ));
        }

        studentsTable.put("values", studentValues);

        tables.add(studentsTable);


        // ==========================================
        // MONTHLY RENT DETAILS
        // ==========================================

        Map<String, Object> rentTable = new LinkedHashMap<>();

        rentTable.put("name", "monthly_rent_details");

        List<Map<String, String>> rentSchema = new ArrayList<>();

        rentSchema.add(Map.of("column", "id",
                "value", "INTEGER PRIMARY KEY AUTOINCREMENT"));

        rentSchema.add(Map.of("column", "month",
                "value", "TEXT NOT NULL"));

        rentSchema.add(Map.of("column", "year",
                "value", "TEXT NOT NULL"));

        rentSchema.add(Map.of("column", "room_no",
                "value", "INTEGER NOT NULL"));

        rentSchema.add(Map.of("column", "rent",
                "value", "INTEGER NOT NULL"));

        rentSchema.add(Map.of("column", "last_reading",
                "value", "INTEGER NOT NULL"));

        rentSchema.add(Map.of("column", "current_reading",
                "value", "INTEGER NOT NULL"));

        rentSchema.add(Map.of("column", "total_light_bill",
                "value", "INTEGER NOT NULL"));

        rentSchema.add(Map.of("column", "arrear_bill",
                "value", "INTEGER NOT NULL DEFAULT 0"));

        rentSchema.add(Map.of("column", "total_rent",
                "value", "INTEGER NOT NULL"));

        rentSchema.add(Map.of("column", "total_rent_paid",
                "value", "INTEGER DEFAULT 0"));

        rentSchema.add(Map.of("column", "payment_status",
                "value", "TEXT NOT NULL"));

        rentTable.put("schema", rentSchema);


        List<List<Object>> rentValues = new ArrayList<>();

        for (monthlyRentDetailsModel rent : mrd_repository.findAllByOrderByRoomNoAscIdAsc()) {

            rentValues.add(Arrays.asList(
                    rent.Id,
                    rent.month,
                    rent.year,
                    rent.roomNo,
                    rent.rent,
                    rent.lastReading,
                    rent.currentReading,
                    rent.totalLightBill,
                    rent.arrearBill,
                    rent.totalRent,
                    rent.totalRentPaid,
                    rent.paymentStatus
            ));
        }

        rentTable.put("values", rentValues);

        tables.add(rentTable);


        // ==========================================
        // ADD TABLES TO BACKUP
        // ==========================================

        backup.put("tables", tables);

        return backup;
    }

    // ==========================================
// DATABASE RESTORE
// ==========================================

    @Override
    @Transactional
    public void restoreDatabase(Map<String, Object> backupData) {

        // ==========================================
        // VALIDATE BACKUP
        // ==========================================

        if (backupData == null) {
            throw new IllegalArgumentException(
                    "Backup data cannot be null."
            );
        }

        if (!"hms".equals(backupData.get("database"))) {
            throw new IllegalArgumentException(
                    "Invalid backup database."
            );
        }

        Object tablesObject =
                backupData.get("tables");

        if (!(tablesObject instanceof List<?>)) {
            throw new IllegalArgumentException(
                    "Invalid backup: tables are missing."
            );
        }

        List<?> tables =
                (List<?>) tablesObject;


        // ==========================================
        // FIND REQUIRED TABLES
        // ==========================================

        Map<String, Object> roomTable = null;
        Map<String, Object> studentsTable = null;
        Map<String, Object> rentTable = null;


        for (Object tableObject : tables) {

            if (!(tableObject instanceof Map<?, ?>)) {
                continue;
            }

            Map<?, ?> table =
                    (Map<?, ?>) tableObject;

            Object tableName =
                    table.get("name");

            if ("room_details".equals(tableName)) {

                roomTable =
                        (Map<String, Object>) table;

            } else if ("students".equals(tableName)) {

                studentsTable =
                        (Map<String, Object>) table;

            } else if ("monthly_rent_details".equals(tableName)) {

                rentTable =
                        (Map<String, Object>) table;
            }
        }


        // ==========================================
        // CHECK REQUIRED TABLES
        // ==========================================

        if (
                roomTable == null ||
                        studentsTable == null ||
                        rentTable == null
        ) {

            throw new IllegalArgumentException(
                    "Invalid backup: required HMS tables are missing."
            );
        }


        // ==========================================
        // GET VALUES
        // ==========================================

        List<?> roomValues =
                getBackupValues(
                        roomTable,
                        "room_details"
                );

        List<?> studentValues =
                getBackupValues(
                        studentsTable,
                        "students"
                );

        List<?> rentValues =
                getBackupValues(
                        rentTable,
                        "monthly_rent_details"
                );


        // ==========================================
        // CLEAR EXISTING DATA
        // ==========================================

        /*
         * Delete in this order because
         * monthly_rent_details references room_details.
         */



        jdbcTemplate.update("DELETE FROM monthly_rent_details");
        jdbcTemplate.update("DELETE FROM students");
        jdbcTemplate.update("DELETE FROM room_details");


        // ==========================================
// RESTORE ROOM DETAILS
// ==========================================

        for (Object rowObject : roomValues) {

            List<?> row =
                    (List<?>) rowObject;

            if (row.size() != 14) {

                throw new IllegalArgumentException(
                        "Invalid room_details row."
                );
            }

            jdbcTemplate.update(
                    """
                    INSERT INTO room_details
                    (
                        room_no,
                        room_type,
                        floor,
                        beds,
                        tables,
                        chairs,
                        coolers,
                        monthly_rent,
                        light_bill,
                        last_meter_reading,
                        arrear_bill,
                        security_amount,
                        security_amount_status,
                        occupancy_status
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,

                    toInteger(row.get(0)),
                    toStringValue(row.get(1)),
                    toInt(row.get(2)),
                    toInt(row.get(3)),
                    toInt(row.get(4)),
                    toInt(row.get(5)),
                    toStringValue(row.get(6)),
                    toInt(row.get(7)),
                    toStringValue(row.get(8)),
                    toInt(row.get(9)),
                    toInt(row.get(10)),
                    toInt(row.get(11)),
                    toStringValue(row.get(12)),
                    toStringValue(row.get(13))
            );
        }


        // ==========================================
// RESTORE STUDENTS
// ==========================================

        for (Object rowObject : studentValues) {

            List<?> row =
                    (List<?>) rowObject;

            if (row.size() != 15) {

                throw new IllegalArgumentException(
                        "Invalid students row."
                );
            }

            jdbcTemplate.update(
                    """
                    INSERT INTO students
                    (
                        student_id,
                        student_name,
                        contact_no,
                        aadhar_no,
                        father_name,
                        father_contact,
                        address_line_1,
                        address_line_2,
                        city,
                        state,
                        pincode,
                        room_no,
                        joining_date,
                        leaving_date,
                        status
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,

                    toInt(row.get(0)),
                    toStringValue(row.get(1)),
                    toStringValue(row.get(2)),
                    toStringValue(row.get(3)),
                    toStringValue(row.get(4)),
                    toStringValue(row.get(5)),
                    toStringValue(row.get(6)),
                    toStringValue(row.get(7)),
                    toStringValue(row.get(8)),
                    toStringValue(row.get(9)),
                    toStringValue(row.get(10)),
                    toInteger(row.get(11)),
                    toLocalDate(row.get(12)),
                    toLocalDate(row.get(13)),
                    toStringValue(row.get(14))
            );
        }


        // ==========================================
// RESTORE MONTHLY RENT DETAILS
// ==========================================

        for (Object rowObject : rentValues) {

            List<?> row =
                    (List<?>) rowObject;

            if (row.size() != 12) {

                throw new IllegalArgumentException(
                        "Invalid monthly_rent_details row."
                );
            }

            jdbcTemplate.update(
                    """
                    INSERT INTO monthly_rent_details
                    (
                        Id,
                        month,
                        year,
                        room_no,
                        rent,
                        last_reading,
                        current_reading,
                        total_light_bill,
                        arrear_bill,
                        total_rent,
                        total_rent_paid,
                        payment_status
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,

                    toInt(row.get(0)),
                    toStringValue(row.get(1)),
                    toStringValue(row.get(2)),
                    toInteger(row.get(3)),
                    toInt(row.get(4)),
                    toInt(row.get(5)),
                    toInt(row.get(6)),
                    toInt(row.get(7)),
                    toInt(row.get(8)),
                    toInt(row.get(9)),
                    toInt(row.get(10)),
                    toStringValue(row.get(11))
            );
        }


        // ==========================================
        // RESET AUTO-INCREMENT VALUES
        // ==========================================

        resetAutoIncrementValues();


        System.out.println(
                "DATABASE RESTORE: Restore successful"
        );
    }

    // ==========================================
// BACKUP VALUE HELPERS
// ==========================================

    private List<?> getBackupValues(
            Map<String, Object> table,
            String tableName) {

        Object valuesObject =
                table.get("values");

        if (!(valuesObject instanceof List<?>)) {

            throw new IllegalArgumentException(
                    "Invalid backup: values missing for table "
                            + tableName
            );
        }

        return (List<?>) valuesObject;
    }


    private String toStringValue(Object value) {

        if (value == null) {
            return null;
        }

        return String.valueOf(value);
    }


    private Integer toInteger(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return Integer.parseInt(
                String.valueOf(value)
        );
    }


    private int toInt(Object value) {

        Integer result =
                toInteger(value);

        if (result == null) {
            return 0;
        }

        return result;
    }


    private java.time.LocalDate toLocalDate(Object value) {

        if (value == null) {
            return null;
        }

        return java.time.LocalDate.parse(
                String.valueOf(value)
        );
    }

    // ==========================================
// RESET AUTO INCREMENT
// ==========================================

    private void resetAutoIncrementValues() {

        Integer maxStudentId =
                hms_repository.findAllByOrderByRoomNoAscStudentIdAsc()
                        .stream()
                        .map(student -> student.studentId)
                        .max(Integer::compareTo)
                        .orElse(0);

        Integer maxRentId =
                mrd_repository.findAllByOrderByRoomNoAscIdAsc()
                        .stream()
                        .map(rent -> rent.Id)
                        .max(Integer::compareTo)
                        .orElse(0);


        // MySQL AUTO_INCREMENT should continue
        // from the highest restored ID.

        hms_repository.flush();

        mrd_repository.flush();


        // Native SQL is intentionally avoided here.
        // MySQL will automatically adjust AUTO_INCREMENT
        // when explicit higher IDs are inserted.
    }

}

