package org.example.hmsBackend.Repository;

import org.example.hmsBackend.Models.roomDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface roomDetailsRepository extends JpaRepository<roomDetails, Integer> {
    Optional<roomDetails> findByRoomNo(Integer roomNo);
    List<roomDetails> findByOccupancyStatus(String occupancyStatus);
    void deleteByRoomNo(int room_no);
    List<roomDetails> findAllByOrderByRoomNoAsc();
}