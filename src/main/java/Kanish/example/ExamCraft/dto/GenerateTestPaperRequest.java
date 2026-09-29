package Kanish.example.ExamCraft.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class GenerateTestPaperRequest {

    @NotBlank(message = "Paper title is required")
    private String title;

    private String description;

    @NotNull(message = "Total questions is required")
    @Positive(message = "Total questions must be greater than zero")
    private Integer totalQuestions;

    @Min(value = 0, message = "Easy percentage cannot be negative")
    @Max(value = 100, message = "Easy percentage cannot exceed 100")
    private Integer easyPercentage = 40;

    @Min(value = 0, message = "Medium percentage cannot be negative")
    @Max(value = 100, message = "Medium percentage cannot exceed 100")
    private Integer mediumPercentage = 40;

    @Min(value = 0, message = "Hard percentage cannot be negative")
    @Max(value = 100, message = "Hard percentage cannot exceed 100")
    private Integer hardPercentage = 20;

    private Long unitId; // Optional filter by specific unit
    private String topic; // Optional filter by specific topic

    public GenerateTestPaperRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getEasyPercentage() {
        return easyPercentage != null ? easyPercentage : 40;
    }

    public void setEasyPercentage(Integer easyPercentage) {
        this.easyPercentage = easyPercentage;
    }

    public Integer getMediumPercentage() {
        return mediumPercentage != null ? mediumPercentage : 40;
    }

    public void setMediumPercentage(Integer mediumPercentage) {
        this.mediumPercentage = mediumPercentage;
    }

    public Integer getHardPercentage() {
        return hardPercentage != null ? hardPercentage : 20;
    }

    public void setHardPercentage(Integer hardPercentage) {
        this.hardPercentage = hardPercentage;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
