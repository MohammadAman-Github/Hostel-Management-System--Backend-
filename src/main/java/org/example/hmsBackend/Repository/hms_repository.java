package org.example.hmsBackend.Repository;

import org.example.hmsBackend.Models.students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface hms_repository extends JpaRepository<students, Integer> {

    Optional<students> findByStudentName(String studentName);
    students findByStudentNameAndRoomNo(String studentName,Integer roomNo);
    List<students> findByRoomNo(Integer roomNo);
    Optional<students> findByStudentIdIs(Integer studentId);
    void deleteById(int id);
    int countByRoomNoAndStatus(Integer roomNo, String status);
}
