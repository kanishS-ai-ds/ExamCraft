package Kanish.example.ExamCraft.repository;

import Kanish.example.ExamCraft.entity.TestPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestPaperRepository extends JpaRepository<TestPaper, Long> {

    @Query("SELECT DISTINCT tp FROM TestPaper tp LEFT JOIN FETCH tp.questions ORDER BY tp.createdAt DESC")
    List<TestPaper> findAllOrderByCreatedAtDesc();
}
