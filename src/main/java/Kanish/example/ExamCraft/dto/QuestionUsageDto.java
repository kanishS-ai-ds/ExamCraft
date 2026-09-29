package Kanish.example.ExamCraft.dto;

import java.util.ArrayList;
import java.util.List;

public class QuestionUsageDto {

    private Long questionId;
    private String questionText;
    private String topic;
    private String difficulty;
    private String unitName;
    private long usageCount;
    private List<String> testPaperTitles = new ArrayList<>();

    public QuestionUsageDto() {
    }

    public QuestionUsageDto(Long questionId, String questionText, String topic, String difficulty, String unitName, long usageCount, List<String> testPaperTitles) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.topic = topic;
        this.difficulty = difficulty;
        this.unitName = unitName;
        this.usageCount = usageCount;
        this.testPaperTitles = testPaperTitles != null ? testPaperTitles : new ArrayList<>();
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(long usageCount) {
        this.usageCount = usageCount;
    }

    public List<String> getTestPaperTitles() {
        return testPaperTitles;
    }

    public void setTestPaperTitles(List<String> testPaperTitles) {
        this.testPaperTitles = testPaperTitles;
    }
}
