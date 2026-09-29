package Kanish.example.ExamCraft.repository;

import Kanish.example.ExamCraft.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    Optional<Unit> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
