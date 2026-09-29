package Kanish.example.ExamCraft.repository;

import Kanish.example.ExamCraft.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByUnitId(Long unitId);

    List<Question> findByDifficultyIgnoreCase(String difficulty);

    List<Question> findByUnitIdAndDifficultyIgnoreCase(Long unitId, String difficulty);

    List<Question> findByTopicContainingIgnoreCase(String topic);

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.unit LEFT JOIN FETCH q.testPapers")
    List<Question> findAllWithUnitAndTestPapers();

    @Query("SELECT q FROM Question q WHERE (:unitId IS NULL OR q.unit.id = :unitId) AND (:difficulty IS NULL OR UPPER(q.difficulty) = UPPER(:difficulty))")
    List<Question> findByFilters(@Param("unitId") Long unitId, @Param("difficulty") String difficulty);
}