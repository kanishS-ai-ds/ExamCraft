package Kanish.example.ExamCraft.controller;

import Kanish.example.ExamCraft.dto.QuestionRequestDto;
import Kanish.example.ExamCraft.dto.QuestionUsageDto;
import Kanish.example.ExamCraft.entity.Question;
import Kanish.example.ExamCraft.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<Question> addQuestion(@Valid @RequestBody QuestionRequestDto dto) {
        Question created = questionService.addQuestion(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String difficulty) {
        return ResponseEntity.ok(questionService.getAllQuestions(unitId, difficulty));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long id, @RequestBody QuestionRequestDto dto) {
        return ResponseEntity.ok(questionService.updateQuestion(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usage-frequency")
    public ResponseEntity<List<QuestionUsageDto>> getQuestionUsageFrequency() {
        return ResponseEntity.ok(questionService.getQuestionUsageFrequency());
    }
}