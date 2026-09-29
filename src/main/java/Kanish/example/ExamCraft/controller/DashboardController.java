package Kanish.example.ExamCraft.controller;

import Kanish.example.ExamCraft.entity.Attempt;
import Kanish.example.ExamCraft.entity.Question;
import Kanish.example.ExamCraft.repository.AttemptRepository;
import Kanish.example.ExamCraft.repository.QuestionRepository;
import Kanish.example.ExamCraft.repository.TestPaperRepository;
import Kanish.example.ExamCraft.repository.UnitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final UnitRepository unitRepository;
    private final QuestionRepository questionRepository;
    private final TestPaperRepository testPaperRepository;
    private final AttemptRepository attemptRepository;

    public DashboardController(UnitRepository unitRepository,
                               QuestionRepository questionRepository,
                               TestPaperRepository testPaperRepository,
                               AttemptRepository attemptRepository) {
        this.unitRepository = unitRepository;
        this.questionRepository = questionRepository;
        this.testPaperRepository = testPaperRepository;
        this.attemptRepository = attemptRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUnits = unitRepository.count();
        long totalQuestions = questionRepository.count();
        long totalTestPapers = testPaperRepository.count();
        long totalAttempts = attemptRepository.count();

        List<Question> allQuestions = questionRepository.findAll();
        long easyQuestions = allQuestions.stream().filter(q -> "EASY".equalsIgnoreCase(q.getDifficulty())).count();
        long mediumQuestions = allQuestions.stream().filter(q -> "MEDIUM".equalsIgnoreCase(q.getDifficulty())).count();
        long hardQuestions = allQuestions.stream().filter(q -> "HARD".equalsIgnoreCase(q.getDifficulty())).count();

        List<Attempt> attempts = attemptRepository.findAll();
        double avgScore = attempts.stream()
                .mapToDouble(Attempt::getPercentage)
                .average()
                .orElse(0.0);

        stats.put("totalUnits", totalUnits);
        stats.put("totalQuestions", totalQuestions);
        stats.put("totalTestPapers", totalTestPapers);
        stats.put("totalAttempts", totalAttempts);
        stats.put("easyCount", easyQuestions);
        stats.put("mediumCount", mediumQuestions);
        stats.put("hardCount", hardQuestions);
        stats.put("averageScore", Math.round(avgScore * 10.0) / 10.0);

        return ResponseEntity.ok(stats);
    }
}
