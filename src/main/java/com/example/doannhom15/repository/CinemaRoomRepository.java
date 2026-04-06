package com.example.doannhom15.repository;

import com.example.doannhom15.model.CinemaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Long> {
    
    boolean existsByName(String name);
}
