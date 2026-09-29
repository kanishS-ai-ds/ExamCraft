package Kanish.example.ExamCraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class SubmitAttemptRequest {

    @NotBlank(message = "Student name is required")
    private String studentName;

    private String studentEmail;

    @NotNull(message = "Test paper ID is required")
    private Long testPaperId;

    // Map of questionId -> selectedOption (e.g. 1 -> "A", 2 -> "C")
    private Map<Long, String> answers = new HashMap<>();

    public SubmitAttemptRequest() {
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Long getTestPaperId() {
        return testPaperId;
    }

    public void setTestPaperId(Long testPaperId) {
        this.testPaperId = testPaperId;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }
}
