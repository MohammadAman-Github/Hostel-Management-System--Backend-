package org.example.hmsBackend.Repository;

import org.example.hmsBackend.Models.monthlyRentDetailsModel;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MRD_repository extends JpaRepository<monthlyRentDetailsModel, Integer> {

    Optional<monthlyRentDetailsModel> findMRDByMonthAndYearAndRoomNo(
            String month,
            String year,
            Integer roomNo
    );

    // Monthly rent for a particular month/year
    // Ordered by Room No
    List<monthlyRentDetailsModel> findByMonthAndYearOrderByRoomNoAsc(
            String month,
            String year
    );

    // Monthly rent for a particular room/year
    // Ordered by ID for deterministic order
    List<monthlyRentDetailsModel> findByRoomNoAndYearOrderByIdAsc(
            int room_no,
            String year
    );

    // All records for a particular room
    List<monthlyRentDetailsModel> findByRoomNoOrderByIdAsc(
            Integer roomNo
    );

    // All Monthly Rent records
    // First by Room No, then by ID
    List<monthlyRentDetailsModel> findAllByOrderByRoomNoAscIdAsc();

    long countByRoomNo(Integer roomNo);
}