package com.example.doannhom15.repository;

import com.example.doannhom15.model.ConcessionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConcessionItemRepository extends JpaRepository<ConcessionItem, Long> {
    List<ConcessionItem> findByActiveTrueOrderByTypeAscNameAsc();
}
