package com.example.doannhom15.service;

import com.example.doannhom15.model.ConcessionItem;
import com.example.doannhom15.repository.ConcessionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcessionService {
    private final ConcessionItemRepository concessionItemRepository;

    public List<ConcessionItem> getActiveItems() {
        return concessionItemRepository.findByActiveTrueOrderByTypeAscNameAsc();
    }

    public ConcessionItem getById(Long id) {
        return concessionItemRepository.findById(id).orElse(null);
    }

    public List<ConcessionItem> findAll() {
        return concessionItemRepository.findAll();
    }

    public ConcessionItem save(ConcessionItem item) {
        return concessionItemRepository.save(item);
    }

    public void deleteById(Long id) {
        concessionItemRepository.deleteById(id);
    }
}
