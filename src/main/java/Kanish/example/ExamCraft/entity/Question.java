package Kanish.example.ExamCraft.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question text cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @NotBlank(message = "Option A is required")
    @Column(nullable = false)
    private String optionA;

    @NotBlank(message = "Option B is required")
    @Column(nullable = false)
    private String optionB;

    @NotBlank(message = "Option C is required")
    @Column(nullable = false)
    private String optionC;

    @NotBlank(message = "Option D is required")
    @Column(nullable = false)
    private String optionD;

    @NotBlank(message = "Correct answer is required")
    @Column(nullable = false)
    private String correctAnswer;

    @NotBlank(message = "Difficulty is required")
    @Column(nullable = false)
    private String difficulty; // EASY, MEDIUM, HARD

    @NotBlank(message = "Topic is required")
    @Column(nullable = false)
    private String topic;

    @NotNull(message = "Unit is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToMany(mappedBy = "questions")
    @JsonIgnore
    private List<TestPaper> testPapers = new ArrayList<>();

    public Question() {
    }

    public Question(String questionText, String optionA, String optionB, String optionC, String optionD,
                    String correctAnswer, String difficulty, String topic, Unit unit) {
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.difficulty = difficulty != null ? difficulty.toUpperCase() : "MEDIUM";
        this.topic = topic;
        this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty != null ? difficulty.toUpperCase() : null;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public List<TestPaper> getTestPapers() {
        return testPapers;
    }

    public void setTestPapers(List<TestPaper> testPapers) {
        this.testPapers = testPapers;
    }
}