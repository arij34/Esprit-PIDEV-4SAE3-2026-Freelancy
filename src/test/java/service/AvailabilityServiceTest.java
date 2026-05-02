package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.freelancy.skillmanagement.entity.Availability;
import tn.freelancy.skillmanagement.entity.Days;
import tn.freelancy.skillmanagement.entity.Periods;
import tn.freelancy.skillmanagement.repository.AvailabilityRepository;
import tn.freelancy.skillmanagement.service.AvailabilityService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Availability availability;

    @BeforeEach
    void setUp() {
        availability = new Availability();
        availability.setHoursPerDay(6);
        availability.setSelectedDays(Arrays.asList(Days.MON, Days.TUE));
        availability.setSelectedPeriods(Arrays.asList(Periods.MORNING));
    }

    @Test
    void testCreateAvailability_ShouldSaveAndReturnAvailability() {
        when(availabilityRepository.save(any(Availability.class)))
                .thenReturn(availability);

        Availability result = availabilityService.createAvailability(1L, availability);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        verify(availabilityRepository, times(1)).save(any(Availability.class));
    }

    @Test
    void testGetAvailabilityByUserId_ShouldReturnAvailability() {
        availability.setUserId(1L);
        when(availabilityRepository.findByUserId(1L))
                .thenReturn(Optional.of(availability));

        Availability result = availabilityService.getAvailabilityByUserId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
    }

    @Test
    void testGetAvailabilityByUserId_ShouldThrowException_WhenNotFound() {
        when(availabilityRepository.findByUserId(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> availabilityService.getAvailabilityByUserId(99L));
    }

    @Test
    void testGetAllAvailabilities_ShouldReturnList() {
        when(availabilityRepository.findAll())
                .thenReturn(Arrays.asList(availability));

        List<Availability> result = availabilityService.getAllAvailabilities();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testDeleteAvailability_ShouldCallDeleteById() {
        doNothing().when(availabilityRepository).deleteById(1L);

        availabilityService.deleteAvailability(1L);

        verify(availabilityRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCalculatePreview_ShouldComputeHoursPerWeek() {
        Availability result = availabilityService.calculatePreview(availability);

        assertNotNull(result);
        assertNotNull(result.getHoursPerWeek());
        assertNotNull(result.getStatus());
    }

    @Test
    void testUpdateAvailability_ShouldUpdateAndReturn() {
        availability.setHoursPerDay(8);
        when(availabilityRepository.findById(1L))
                .thenReturn(Optional.of(availability));
        when(availabilityRepository.save(any(Availability.class)))
                .thenReturn(availability);

        Availability updated = new Availability();
        updated.setHoursPerDay(8);
        updated.setSelectedDays(Arrays.asList(Days.MON));
        updated.setSelectedPeriods(Arrays.asList(Periods.MORNING));

        Availability result = availabilityService.updateAvailability(1L, updated);

        assertNotNull(result);
        verify(availabilityRepository, times(1)).save(any(Availability.class));
    }
}