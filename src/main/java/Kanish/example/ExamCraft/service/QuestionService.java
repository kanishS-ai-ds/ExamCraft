package Kanish.example.ExamCraft.service;

import Kanish.example.ExamCraft.dto.QuestionRequestDto;
import Kanish.example.ExamCraft.dto.QuestionUsageDto;
import Kanish.example.ExamCraft.entity.Question;
import Kanish.example.ExamCraft.entity.TestPaper;
import Kanish.example.ExamCraft.entity.Unit;
import Kanish.example.ExamCraft.exception.BadRequestException;
import Kanish.example.ExamCraft.exception.ResourceNotFoundException;
import Kanish.example.ExamCraft.repository.QuestionRepository;
import Kanish.example.ExamCraft.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private final QuestionRepository questionRepository;
    private final UnitRepository unitRepository;

    public QuestionService(QuestionRepository questionRepository, UnitRepository unitRepository) {
        this.questionRepository = questionRepository;
        this.unitRepository = unitRepository;
    }

    public Question addQuestion(QuestionRequestDto dto) {
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + dto.getUnitId()));

        String normalizedDifficulty = normalizeDifficulty(dto.getDifficulty());
        validateCorrectAnswer(dto.getCorrectAnswer());

        Question question = new Question();
        question.setQuestionText(dto.getQuestionText().trim());
        question.setOptionA(dto.getOptionA().trim());
        question.setOptionB(dto.getOptionB().trim());
        question.setOptionC(dto.getOptionC().trim());
        question.setOptionD(dto.getOptionD().trim());
        question.setCorrectAnswer(dto.getCorrectAnswer().trim().toUpperCase());
        question.setDifficulty(normalizedDifficulty);
        question.setTopic(dto.getTopic().trim());
        question.setUnit(unit);

        Question saved = questionRepository.save(question);
        log.info("[NOTIFICATION] New question created: ID={}, Unit='{}', Difficulty={}", saved.getId(), unit.getName(), saved.getDifficulty());
        return saved;
    }

    public List<Question> getAllQuestions(Long unitId, String difficulty) {
        if (unitId != null || difficulty != null) {
            return questionRepository.findByFilters(unitId, difficulty != null ? difficulty.toUpperCase() : null);
        }
        return questionRepository.findAll();
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
    }

    public Question updateQuestion(Long id, QuestionRequestDto dto) {
        Question existing = getQuestionById(id);

        if (dto.getUnitId() != null) {
            Unit unit = unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + dto.getUnitId()));
            existing.setUnit(unit);
        }

        if (dto.getQuestionText() != null && !dto.getQuestionText().trim().isEmpty()) {
            existing.setQuestionText(dto.getQuestionText().trim());
        }
        if (dto.getOptionA() != null) existing.setOptionA(dto.getOptionA().trim());
        if (dto.getOptionB() != null) existing.setOptionB(dto.getOptionB().trim());
        if (dto.getOptionC() != null) existing.setOptionC(dto.getOptionC().trim());
        if (dto.getOptionD() != null) existing.setOptionD(dto.getOptionD().trim());

        if (dto.getCorrectAnswer() != null) {
            validateCorrectAnswer(dto.getCorrectAnswer());
            existing.setCorrectAnswer(dto.getCorrectAnswer().trim().toUpperCase());
        }

        if (dto.getDifficulty() != null) {
            existing.setDifficulty(normalizeDifficulty(dto.getDifficulty()));
        }

        if (dto.getTopic() != null) {
            existing.setTopic(dto.getTopic().trim());
        }

        Question saved = questionRepository.save(existing);
        log.info("[NOTIFICATION] Question updated: ID={}", saved.getId());
        return saved;
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = getQuestionById(id);
        questionRepository.delete(question);
        log.info("[NOTIFICATION] Question deleted: ID={}", id);
    }

    public List<QuestionUsageDto> getQuestionUsageFrequency() {
        List<Question> questions = questionRepository.findAllWithUnitAndTestPapers();
        List<QuestionUsageDto> usageList = new ArrayList<>();

        for (Question q : questions) {
            List<String> titles = q.getTestPapers().stream()
                    .map(TestPaper::getTitle)
                    .collect(Collectors.toList());
            long count = q.getTestPapers().size();
            String unitName = q.getUnit() != null ? q.getUnit().getName() : "Unassigned";

            usageList.add(new QuestionUsageDto(
                    q.getId(),
                    q.getQuestionText(),
                    q.getTopic(),
                    q.getDifficulty(),
                    unitName,
                    count,
                    titles
            ));
        }

        // Sort by usage count descending, then by ID
        usageList.sort((a, b) -> {
            int cmp = Long.compare(b.getUsageCount(), a.getUsageCount());
            return cmp != 0 ? cmp : Long.compare(a.getQuestionId(), b.getQuestionId());
        });

        return usageList;
    }

    private String normalizeDifficulty(String diff) {
        if (diff == null || diff.trim().isEmpty()) {
            return "MEDIUM";
        }
        String upper = diff.trim().toUpperCase();
        if (!upper.equals("EASY") && !upper.equals("MEDIUM") && !upper.equals("HARD")) {
            throw new BadRequestException("Invalid difficulty: " + diff + ". Allowed values are EASY, MEDIUM, HARD.");
        }
        return upper;
    }

    private void validateCorrectAnswer(String ans) {
        if (ans == null || ans.trim().isEmpty()) {
            throw new BadRequestException("Correct answer cannot be blank");
        }
        String upper = ans.trim().toUpperCase();
        if (!upper.equals("A") && !upper.equals("B") && !upper.equals("C") && !upper.equals("D")) {
            throw new BadRequestException("Correct answer must be one of: A, B, C, or D");
        }
    }
}