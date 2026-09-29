package Kanish.example.ExamCraft.service;

import Kanish.example.ExamCraft.dto.SubmitAttemptRequest;
import Kanish.example.ExamCraft.entity.Attempt;
import Kanish.example.ExamCraft.entity.AttemptAnswer;
import Kanish.example.ExamCraft.entity.Question;
import Kanish.example.ExamCraft.entity.TestPaper;
import Kanish.example.ExamCraft.exception.BadRequestException;
import Kanish.example.ExamCraft.exception.ResourceNotFoundException;
import Kanish.example.ExamCraft.repository.AttemptRepository;
import Kanish.example.ExamCraft.repository.TestPaperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AttemptService {

    private static final Logger log = LoggerFactory.getLogger(AttemptService.class);
    private final AttemptRepository attemptRepository;
    private final TestPaperRepository testPaperRepository;

    public AttemptService(AttemptRepository attemptRepository, TestPaperRepository testPaperRepository) {
        this.attemptRepository = attemptRepository;
        this.testPaperRepository = testPaperRepository;
    }

    @Transactional
    public Attempt submitAttempt(SubmitAttemptRequest request) {
        if (request.getStudentName() == null || request.getStudentName().trim().isEmpty()) {
            throw new BadRequestException("Student name cannot be blank.");
        }
        if (request.getTestPaperId() == null) {
            throw new BadRequestException("Test paper ID is required.");
        }

        TestPaper testPaper = testPaperRepository.findById(request.getTestPaperId())
                .orElseThrow(() -> new ResourceNotFoundException("Test Paper not found with id: " + request.getTestPaperId()));

        List<Question> questions = testPaper.getQuestions();
        if (questions == null || questions.isEmpty()) {
            throw new BadRequestException("Test paper contains no questions to evaluate.");
        }

        Map<Long, String> submittedAnswers = request.getAnswers() != null ? request.getAnswers() : Map.of();

        int score = 0;
        List<AttemptAnswer> attemptAnswers = new ArrayList<>();

        for (Question q : questions) {
            String selected = submittedAnswers.get(q.getId());
            if (selected != null) {
                selected = selected.trim().toUpperCase();
            } else {
                selected = "UNANSWERED";
            }

            String correct = q.getCorrectAnswer() != null ? q.getCorrectAnswer().trim().toUpperCase() : "";
            boolean isCorrect = correct.equalsIgnoreCase(selected);

            if (isCorrect) {
                score++;
            }

            AttemptAnswer ans = new AttemptAnswer(
                    q.getId(),
                    q.getQuestionText(),
                    selected,
                    correct,
                    isCorrect
            );
            attemptAnswers.add(ans);
        }

        int totalQuestions = questions.size();
        double percentage = (totalQuestions > 0) ? Math.round(((double) score / totalQuestions * 100.0) * 10.0) / 10.0 : 0.0;

        Attempt attempt = new Attempt();
        attempt.setStudentName(request.getStudentName().trim());
        attempt.setStudentEmail(request.getStudentEmail() != null ? request.getStudentEmail().trim() : null);
        attempt.setTestPaper(testPaper);
        attempt.setScore(score);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setPercentage(percentage);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setAnswers(attemptAnswers);

        Attempt saved = attemptRepository.save(attempt);

        log.info("[NOTIFICATION] Student '{}' completed test '{}' (ID={}): Score {}/{} ({}%)",
                saved.getStudentName(), testPaper.getTitle(), testPaper.getId(), saved.getScore(),
                saved.getTotalQuestions(), saved.getPercentage());

        return saved;
    }

    public List<Attempt> getAllAttempts() {
        return attemptRepository.findAllByOrderBySubmittedAtDesc();
    }

    public List<Attempt> getAttemptsByTestPaper(Long testPaperId) {
        return attemptRepository.findByTestPaperIdOrderBySubmittedAtDesc(testPaperId);
    }

    public Attempt getAttemptById(Long id) {
        return attemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + id));
    }
}
