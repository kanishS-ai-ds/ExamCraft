package Kanish.example.ExamCraft.controller;

import Kanish.example.ExamCraft.dto.SubmitAttemptRequest;
import Kanish.example.ExamCraft.entity.Attempt;
import Kanish.example.ExamCraft.service.AttemptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
@CrossOrigin(origins = "*")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping
    public ResponseEntity<Attempt> submitAttempt(@Valid @RequestBody SubmitAttemptRequest request) {
        Attempt attempt = attemptService.submitAttempt(request);
        return new ResponseEntity<>(attempt, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Attempt>> getAllAttempts() {
        return ResponseEntity.ok(attemptService.getAllAttempts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attempt> getAttemptById(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.getAttemptById(id));
    }

    @GetMapping("/test-paper/{testPaperId}")
    public ResponseEntity<List<Attempt>> getAttemptsByTestPaper(@PathVariable Long testPaperId) {
        return ResponseEntity.ok(attemptService.getAttemptsByTestPaper(testPaperId));
    }
}
