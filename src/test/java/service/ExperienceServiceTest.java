package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.freelancy.skillmanagement.entity.Experience;
import tn.freelancy.skillmanagement.repository.ExperienceRepository;
import tn.freelancy.skillmanagement.service.ExperienceService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperienceServiceTest {

    @Mock
    private ExperienceRepository experienceRepository;

    @InjectMocks
    private ExperienceService experienceService;

    private Experience experience;

    @BeforeEach
    void setUp() {
        experience = new Experience();
        experience.setUserId(1L);
        experience.setStartDate(LocalDate.of(2020, 1, 1));
        experience.setEndDate(LocalDate.of(2022, 1, 1));
    }

    @Test
    void testCreateExperience_ShouldSaveAndReturn() {
        when(experienceRepository.save(any(Experience.class)))
                .thenReturn(experience);

        Experience result = experienceService.createExperience(1L, experience);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        verify(experienceRepository, times(1)).save(any(Experience.class));
    }

    @Test
    void testGetAllExperiences_ShouldReturnList() {
        when(experienceRepository.findAll())
                .thenReturn(Arrays.asList(experience));

        List<Experience> result = experienceService.getAllExperiences();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testGetExperienceById_ShouldReturnExperience() {
        when(experienceRepository.findById(1L))
                .thenReturn(Optional.of(experience));

        Experience result = experienceService.getExperienceById(1L);

        assertNotNull(result);
    }

    @Test
    void testGetExperienceById_ShouldReturnNull_WhenNotFound() {
        when(experienceRepository.findById(99L))
                .thenReturn(Optional.empty());

        Experience result = experienceService.getExperienceById(99L);

        assertNull(result);
    }

    @Test
    void testDeleteExperience_ShouldCallDeleteById() {
        doNothing().when(experienceRepository).deleteById(1L);

        experienceService.deleteExperience(1L);

        verify(experienceRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCalculateTotalYearsByUser_ShouldReturn2Years() {
        when(experienceRepository.findByUserId(1L))
                .thenReturn(Arrays.asList(experience));

        double result = experienceService.calculateTotalYearsByUser(1L);

        assertEquals(2.0, result, 0.1);
    }

    @Test
    void testCalculateTotalYearsByUser_ShouldReturn0_WhenNoExperiences() {
        when(experienceRepository.findByUserId(1L))
                .thenReturn(Arrays.asList());

        double result = experienceService.calculateTotalYearsByUser(1L);

        assertEquals(0.0, result);
    }

    @Test
    void testGetExperiencesByUserId_ShouldReturnList() {
        when(experienceRepository.findByUserId(1L))
                .thenReturn(Arrays.asList(experience));

        List<Experience> result = experienceService.getExperiencesByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}