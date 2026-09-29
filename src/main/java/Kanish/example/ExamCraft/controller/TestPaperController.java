package Kanish.example.ExamCraft.controller;

import Kanish.example.ExamCraft.dto.GenerateTestPaperRequest;
import Kanish.example.ExamCraft.entity.TestPaper;
import Kanish.example.ExamCraft.service.TestPaperService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-papers")
@CrossOrigin(origins = "*")
public class TestPaperController {

    private final TestPaperService testPaperService;

    public TestPaperController(TestPaperService testPaperService) {
        this.testPaperService = testPaperService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TestPaper> generateTestPaper(@Valid @RequestBody GenerateTestPaperRequest request) {
        TestPaper generated = testPaperService.generateTestPaper(request);
        return new ResponseEntity<>(generated, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TestPaper>> getAllTestPapers() {
        return ResponseEntity.ok(testPaperService.getAllTestPapers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestPaper> getTestPaperById(@PathVariable Long id) {
        return ResponseEntity.ok(testPaperService.getTestPaperById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestPaper(@PathVariable Long id) {
        testPaperService.deleteTestPaper(id);
        return ResponseEntity.noContent().build();
    }
}
