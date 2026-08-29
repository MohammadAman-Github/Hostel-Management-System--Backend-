package org.example.hms.Repository;

import org.example.hms.Models.monthlyRentDetailsModel;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MRD_repository extends JpaRepository<monthlyRentDetailsModel, Integer> {
    Optional<monthlyRentDetailsModel> findMRDByMonthAndYearAndRoomNo(String month, String year, Integer roomNo);


    List<monthlyRentDetailsModel> findByMonthAndYear(String month, String year);
    List<monthlyRentDetailsModel> findByRoomNoAndYear(int room_no, String year);
    List<monthlyRentDetailsModel> findByRoomNo(Integer roomNo);
}
