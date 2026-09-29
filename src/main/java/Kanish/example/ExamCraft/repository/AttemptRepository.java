package Kanish.example.ExamCraft.repository;

import Kanish.example.ExamCraft.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByTestPaperIdOrderBySubmittedAtDesc(Long testPaperId);
    List<Attempt> findAllByOrderBySubmittedAtDesc();
}
