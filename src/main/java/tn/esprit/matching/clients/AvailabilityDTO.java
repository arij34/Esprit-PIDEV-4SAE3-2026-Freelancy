package tn.esprit.matching.clients;


public class AvailabilityDTO {

    private Integer hoursPerDay;
    private Integer hoursPerWeek;
    private String status;
    private Long userId;

    public Integer getHoursPerDay() { return hoursPerDay; }
    public Integer getHoursPerWeek() { return hoursPerWeek; }
    public String getStatus() { return status; }
    public Long getUserId() { return userId; }

    public void setStatus(String partTime) {
        status = partTime;
    }

    public void setHoursPerWeek(int i) {
        hoursPerWeek = i;
    }
}