package org.example.hms.Advice;

import org.example.hms.exception.DataNotFoundException;
import org.example.hms.exception.MRD_out_of_database;
import org.example.hms.exception.RoomNotFoundException;
import org.example.hms.exception.StudentNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;



@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<String> studentNotFonndException(StudentNotFoundException e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatusCode.valueOf(400));
    }
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<String> roomNotFoundException(RoomNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(404));
    }

    @ExceptionHandler(MRD_out_of_database.class)
    public ResponseEntity<String> mrd_out_of_database(MRD_out_of_database e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(400));
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<String> dataNotFound(DataNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(404));
    }
}
