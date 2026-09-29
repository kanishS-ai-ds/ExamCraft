package Kanish.example.ExamCraft;

import Kanish.example.ExamCraft.dto.GenerateTestPaperRequest;
import Kanish.example.ExamCraft.dto.QuestionRequestDto;
import Kanish.example.ExamCraft.dto.QuestionUsageDto;
import Kanish.example.ExamCraft.dto.SubmitAttemptRequest;
import Kanish.example.ExamCraft.entity.Attempt;
import Kanish.example.ExamCraft.entity.Question;
import Kanish.example.ExamCraft.entity.TestPaper;
import Kanish.example.ExamCraft.entity.Unit;
import Kanish.example.ExamCraft.exception.BadRequestException;
import Kanish.example.ExamCraft.service.AttemptService;
import Kanish.example.ExamCraft.service.QuestionService;
import Kanish.example.ExamCraft.service.TestPaperService;
import Kanish.example.ExamCraft.service.UnitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExamCraftCoreFeaturesTest {

    @Autowired
    private UnitService unitService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private TestPaperService testPaperService;

    @Autowired
    private AttemptService attemptService;

    @Test
    @DisplayName("Feature 1: Add and Query Units & Questions with tags")
    void testUnitAndQuestionCreation() {
        List<Unit> units = unitService.getAllUnits();
        assertFalse(units.isEmpty(), "Units should be initialized");

        Unit firstUnit = units.get(0);
        QuestionRequestDto dto = new QuestionRequestDto();
        dto.setQuestionText("What is the degree of a relation?");
        dto.setOptionA("Number of attributes");
        dto.setOptionB("Number of tuples");
        dto.setOptionC("Number of primary keys");
        dto.setOptionD("Number of foreign keys");
        dto.setCorrectAnswer("A");
        dto.setDifficulty("EASY");
        dto.setTopic("Relational Model Basics");
        dto.setUnitId(firstUnit.getId());

        Question saved = questionService.addQuestion(dto);
        assertNotNull(saved.getId());
        assertEquals("EASY", saved.getDifficulty());
        assertEquals(firstUnit.getId(), saved.getUnit().getId());
    }

    @Test
    @DisplayName("Feature 2 & 3: Generate randomized test paper with difficulty mix and NO duplicate questions")
    void testRandomizedTestPaperGenerationWithoutDuplicates() {
        GenerateTestPaperRequest request = new GenerateTestPaperRequest();
        request.setTitle("Midterm DBMS Exam 2026 - Set A");
        request.setDescription("Automated generation test");
        request.setTotalQuestions(10);
        request.setEasyPercentage(40);    // target: 4
        request.setMediumPercentage(40);  // target: 4
        request.setHardPercentage(20);    // target: 2

        TestPaper paper = testPaperService.generateTestPaper(request);

        assertNotNull(paper.getId());
        assertEquals(10, paper.getQuestions().size(), "Must contain exactly 10 questions");

        // Rule enforcement: No duplicate questions in the same paper
        Set<Long> uniqueIds = paper.getQuestions().stream()
                .map(Question::getId)
                .collect(Collectors.toSet());
        assertEquals(10, uniqueIds.size(), "Every question in the paper must be unique (no duplicates)");

        // Verify difficulty mix
        assertTrue(paper.getEasyCount() > 0, "Should have easy questions");
        assertTrue(paper.getMediumCount() > 0, "Should have medium questions");
        assertTrue(paper.getHardCount() > 0, "Should have hard questions");
        assertEquals(10, paper.getEasyCount() + paper.getMediumCount() + paper.getHardCount());
    }

    @Test
    @DisplayName("Business Rule: Percentages must sum to 100% or reject immediately")
    void testInvalidDifficultyPercentageRejection() {
        GenerateTestPaperRequest request = new GenerateTestPaperRequest();
        request.setTitle("Invalid Distribution Test");
        request.setTotalQuestions(5);
        request.setEasyPercentage(50);
        request.setMediumPercentage(50);
        request.setHardPercentage(50); // sum = 150%

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            testPaperService.generateTestPaper(request);
        });
        assertTrue(ex.getMessage().contains("must sum to exactly 100%"));
    }

    @Test
    @DisplayName("Feature 4: Auto-grade objective-type questions on submission")
    void testAutoGradingSubmission() {
        GenerateTestPaperRequest paperReq = new GenerateTestPaperRequest();
        paperReq.setTitle("Auto-Grading Verification Paper");
        paperReq.setTotalQuestions(5);
        paperReq.setEasyPercentage(40);
        paperReq.setMediumPercentage(40);
        paperReq.setHardPercentage(20);

        TestPaper paper = testPaperService.generateTestPaper(paperReq);

        SubmitAttemptRequest attemptReq = new SubmitAttemptRequest();
        attemptReq.setStudentName("Kanish");
        attemptReq.setStudentEmail("kanish@example.com");
        attemptReq.setTestPaperId(paper.getId());

        // Answer 3 correctly and 2 incorrectly
        Map<Long, String> answers = new HashMap<>();
        List<Question> questions = paper.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if (i < 3) {
                // Answer correctly
                answers.put(q.getId(), q.getCorrectAnswer());
            } else {
                // Answer incorrectly
                answers.put(q.getId(), "WRONG_OPTION");
            }
        }
        attemptReq.setAnswers(answers);

        Attempt attempt = attemptService.submitAttempt(attemptReq);

        assertNotNull(attempt.getId());
        assertEquals("Kanish", attempt.getStudentName());
        assertEquals(5, attempt.getTotalQuestions());
        assertEquals(3, attempt.getScore(), "Score must be exactly 3 out of 5");
        assertEquals(60.0, attempt.getPercentage(), "Percentage must be 60.0%");
        assertEquals(5, attempt.getAnswers().size());
    }

    @Test
    @DisplayName("Feature 5: View question usage frequency across generated papers")
    void testQuestionUsageFrequency() {
        List<QuestionUsageDto> usageList = questionService.getQuestionUsageFrequency();
        assertNotNull(usageList);
        assertFalse(usageList.isEmpty());

        // At least one question should have been used in our test papers generated above
        long usedQuestionsCount = usageList.stream().filter(u -> u.getUsageCount() > 0).count();
        assertTrue(usedQuestionsCount > 0, "Usage frequency must track questions used in papers");

        QuestionUsageDto mostUsed = usageList.get(0);
        assertTrue(mostUsed.getUsageCount() >= 0);
        assertNotNull(mostUsed.getQuestionText());
        assertNotNull(mostUsed.getUnitName());
    }
}
