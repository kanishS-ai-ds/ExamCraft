package Kanish.example.ExamCraft.service;

import Kanish.example.ExamCraft.entity.Unit;
import Kanish.example.ExamCraft.exception.BadRequestException;
import Kanish.example.ExamCraft.exception.ResourceNotFoundException;
import Kanish.example.ExamCraft.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnitService {

    private static final Logger log = LoggerFactory.getLogger(UnitService.class);
    private final UnitRepository unitRepository;

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    public Unit createUnit(Unit unit) {
        if (unit.getName() == null || unit.getName().trim().isEmpty()) {
            throw new BadRequestException("Unit name cannot be blank");
        }
        if (unitRepository.existsByNameIgnoreCase(unit.getName().trim())) {
            throw new BadRequestException("A unit with name '" + unit.getName().trim() + "' already exists");
        }
        unit.setName(unit.getName().trim());
        Unit saved = unitRepository.save(unit);
        log.info("[NOTIFICATION] Unit created successfully: ID={}, Name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }

    public Unit getUnitById(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
    }

    public Unit updateUnit(Long id, Unit updatedUnit) {
        Unit existing = getUnitById(id);
        if (updatedUnit.getName() != null && !updatedUnit.getName().trim().isEmpty()) {
            String newName = updatedUnit.getName().trim();
            if (!existing.getName().equalsIgnoreCase(newName) && unitRepository.existsByNameIgnoreCase(newName)) {
                throw new BadRequestException("A unit with name '" + newName + "' already exists");
            }
            existing.setName(newName);
        }
        if (updatedUnit.getDescription() != null) {
            existing.setDescription(updatedUnit.getDescription());
        }
        Unit saved = unitRepository.save(existing);
        log.info("[NOTIFICATION] Unit updated: ID={}, Name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public void deleteUnit(Long id) {
        Unit unit = getUnitById(id);
        unitRepository.delete(unit);
        log.info("[NOTIFICATION] Unit deleted: ID={}", id);
    }
}
