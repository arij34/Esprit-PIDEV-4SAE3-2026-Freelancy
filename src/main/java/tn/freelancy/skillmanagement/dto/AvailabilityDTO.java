package tn.freelancy.skillmanagement.dto;

public class AvailabilityDTO {

    private Integer hoursPerDay;
    private Integer hoursPerWeek;
    private String status;
    private Long userId;

    public AvailabilityDTO() {}

    public AvailabilityDTO(Integer hoursPerDay, Integer hoursPerWeek, String status, Long userId) {
        this.hoursPerDay = hoursPerDay;
        this.hoursPerWeek = hoursPerWeek;
        this.status = status;
        this.userId = userId;
    }

    public Integer getHoursPerDay() { return hoursPerDay; }
    public Integer getHoursPerWeek() { return hoursPerWeek; }
    public String getStatus() { return status; }
    public Long getUserId() { return userId; }
}