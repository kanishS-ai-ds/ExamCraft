package Kanish.example.ExamCraft.service;

import Kanish.example.ExamCraft.dto.GenerateTestPaperRequest;
import Kanish.example.ExamCraft.entity.Question;
import Kanish.example.ExamCraft.entity.TestPaper;
import Kanish.example.ExamCraft.entity.Unit;
import Kanish.example.ExamCraft.exception.BadRequestException;
import Kanish.example.ExamCraft.exception.ResourceNotFoundException;
import Kanish.example.ExamCraft.repository.QuestionRepository;
import Kanish.example.ExamCraft.repository.TestPaperRepository;
import Kanish.example.ExamCraft.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestPaperService {

    private static final Logger log = LoggerFactory.getLogger(TestPaperService.class);
    private final TestPaperRepository testPaperRepository;
    private final QuestionRepository questionRepository;
    private final UnitRepository unitRepository;
    private final Random random = new SecureRandom();

    public TestPaperService(TestPaperRepository testPaperRepository,
                            QuestionRepository questionRepository,
                            UnitRepository unitRepository) {
        this.testPaperRepository = testPaperRepository;
        this.questionRepository = questionRepository;
        this.unitRepository = unitRepository;
    }

    @Transactional
    public TestPaper generateTestPaper(GenerateTestPaperRequest request) {
        // 1. Validate inputs
        if (request.getTotalQuestions() == null || request.getTotalQuestions() <= 0) {
            throw new BadRequestException("Total questions must be greater than zero.");
        }

        int easyPct = request.getEasyPercentage();
        int medPct = request.getMediumPercentage();
        int hardPct = request.getHardPercentage();

        if (easyPct + medPct + hardPct != 100) {
            throw new BadRequestException("Difficulty percentages must sum to exactly 100%. Current sum: " 
                    + (easyPct + medPct + hardPct) + "% (Easy: " + easyPct + "%, Medium: " + medPct + "%, Hard: " + hardPct + "%)");
        }

        if (request.getUnitId() != null) {
            unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Selected unit not found with id: " + request.getUnitId()));
        }

        // 2. Fetch available question pool
        List<Question> allEligible;
        if (request.getUnitId() != null) {
            allEligible = questionRepository.findByUnitId(request.getUnitId());
        } else {
            allEligible = questionRepository.findAll();
        }

        int totalRequested = request.getTotalQuestions();
        if (allEligible.size() < totalRequested) {
            throw new BadRequestException("Insufficient questions in the question bank. Requested " 
                    + totalRequested + " questions, but only " + allEligible.size() + " are available"
                    + (request.getUnitId() != null ? " in the selected unit." : " in total."));
        }

        // 3. Separate questions by difficulty
        List<Question> easyPool = allEligible.stream()
                .filter(q -> "EASY".equalsIgnoreCase(q.getDifficulty()))
                .collect(Collectors.toList());

        List<Question> mediumPool = allEligible.stream()
                .filter(q -> "MEDIUM".equalsIgnoreCase(q.getDifficulty()))
                .collect(Collectors.toList());

        List<Question> hardPool = allEligible.stream()
                .filter(q -> "HARD".equalsIgnoreCase(q.getDifficulty()))
                .collect(Collectors.toList());

        // Shuffle pools to ensure random distribution
        Collections.shuffle(easyPool, random);
        Collections.shuffle(mediumPool, random);
        Collections.shuffle(hardPool, random);

        // 4. Calculate targets
        int targetEasy = (int) Math.round((easyPct * totalRequested) / 100.0);
        int targetMedium = (int) Math.round((medPct * totalRequested) / 100.0);
        int targetHard = totalRequested - targetEasy - targetMedium;

        if (targetHard < 0) {
            targetHard = 0;
            if (targetMedium > 0) targetMedium--;
            else targetEasy--;
        }

        log.info("[GENERATOR] Target counts -> Easy: {}, Medium: {}, Hard: {} (Total: {})",
                targetEasy, targetMedium, targetHard, totalRequested);

        // 5. Select questions enforcing no duplicates rule
        Set<Long> selectedQuestionIds = new HashSet<>();
        List<Question> selectedQuestions = new ArrayList<>();

        // Helper method to add distinct questions
        pickQuestions(easyPool, targetEasy, selectedQuestionIds, selectedQuestions);
        pickQuestions(mediumPool, targetMedium, selectedQuestionIds, selectedQuestions);
        pickQuestions(hardPool, targetHard, selectedQuestionIds, selectedQuestions);

        // 6. If target difficulty could not be satisfied fully, fill deficit from remaining pools
        // "Generated papers must respect the requested difficulty distribution as closely as the available question bank allows."
        if (selectedQuestions.size() < totalRequested) {
            List<Question> remainingPool = new ArrayList<>(allEligible);
            Collections.shuffle(remainingPool, random);

            for (Question q : remainingPool) {
                if (selectedQuestions.size() >= totalRequested) {
                    break;
                }
                if (!selectedQuestionIds.contains(q.getId())) {
                    selectedQuestionIds.add(q.getId());
                    selectedQuestions.add(q);
                }
            }
        }

        // Final duplicate check
        if (selectedQuestions.size() != totalRequested || selectedQuestionIds.size() != totalRequested) {
            throw new BadRequestException("Could not satisfy request without duplicate questions. Required " 
                    + totalRequested + " distinct questions, found " + selectedQuestions.size());
        }

        // Interleave / shuffle test paper questions so difficulty is well distributed throughout
        Collections.shuffle(selectedQuestions, random);

        // Calculate actual difficulty counts achieved
        int actualEasy = (int) selectedQuestions.stream().filter(q -> "EASY".equalsIgnoreCase(q.getDifficulty())).count();
        int actualMed = (int) selectedQuestions.stream().filter(q -> "MEDIUM".equalsIgnoreCase(q.getDifficulty())).count();
        int actualHard = (int) selectedQuestions.stream().filter(q -> "HARD".equalsIgnoreCase(q.getDifficulty())).count();

        // 7. Save test paper
        TestPaper testPaper = new TestPaper();
        testPaper.setTitle(request.getTitle().trim());
        testPaper.setDescription(request.getDescription());
        testPaper.setTotalQuestions(totalRequested);
        testPaper.setEasyCount(actualEasy);
        testPaper.setMediumCount(actualMed);
        testPaper.setHardCount(actualHard);
        testPaper.setCreatedAt(LocalDateTime.now());
        testPaper.setQuestions(selectedQuestions);

        TestPaper saved = testPaperRepository.save(testPaper);
        log.info("[NOTIFICATION] Generated Test Paper '{}' (ID={}): Total={}, Easy={}, Medium={}, Hard={}",
                saved.getTitle(), saved.getId(), saved.getTotalQuestions(), actualEasy, actualMed, actualHard);

        return saved;
    }

    private void pickQuestions(List<Question> pool, int targetCount, Set<Long> selectedIds, List<Question> destination) {
        int picked = 0;
        for (Question q : pool) {
            if (picked >= targetCount) {
                break;
            }
            if (!selectedIds.contains(q.getId())) {
                selectedIds.add(q.getId());
                destination.add(q);
                picked++;
            }
        }
    }

    public List<TestPaper> getAllTestPapers() {
        return testPaperRepository.findAllOrderByCreatedAtDesc();
    }

    public TestPaper getTestPaperById(Long id) {
        return testPaperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test Paper not found with id: " + id));
    }

    @Transactional
    public void deleteTestPaper(Long id) {
        TestPaper testPaper = getTestPaperById(id);
        testPaperRepository.delete(testPaper);
        log.info("[NOTIFICATION] Test Paper deleted: ID={}", id);
    }
}
