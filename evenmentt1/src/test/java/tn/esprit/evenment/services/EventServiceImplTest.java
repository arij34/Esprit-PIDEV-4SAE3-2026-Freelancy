package tn.esprit.evenment.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.evenment.entities.Event;
import tn.esprit.evenment.entities.EventStatus;
import tn.esprit.evenment.entities.ParticipationStatus;
import tn.esprit.evenment.repositories.EventRepository;
import tn.esprit.evenment.repositories.ParticipantRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository, participantRepository);
    }

    @Test
    void addEvent_shouldSaveEvent() {
        Event event = new Event();
        event.setId(1L);

        when(eventRepository.save(event)).thenReturn(event);

        Event saved = eventService.addEvent(event);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
    }

    @Test
    void updateEvent_shouldSaveEvent() {
        Event event = new Event();
        event.setId(2L);

        when(eventRepository.save(event)).thenReturn(event);

        Event updated = eventService.updateEvent(event);

        assertNotNull(updated);
        assertEquals(2L, updated.getId());
    }

    @Test
    void getEventById_shouldReturnEvent() {
        Event event = new Event();
        event.setId(3L);

        when(eventRepository.findById(3L)).thenReturn(Optional.of(event));

        Event found = eventService.getEventById(3L);

        assertNotNull(found);
        assertEquals(3L, found.getId());
    }

    @Test
    void getAllEvents_shouldReturnAll() {
        Event event = new Event();
        event.setId(4L);

        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<Event> events = eventService.getAllEvents();

        assertEquals(1, events.size());
        assertEquals(4L, events.get(0).getId());
    }

    @Test
    void deleteEvent_shouldCallRepository() {
        eventService.deleteEvent(5L);

        verify(eventRepository).deleteById(5L);
    }

    @Test
    void calculateRate_shouldUseMinimumBaseOfFive() {
        Event event = new Event();
        event.setId(6L);

        when(eventRepository.findById(6L)).thenReturn(Optional.of(event));
        when(participantRepository.countByEventIdAndStatus(6L, ParticipationStatus.ACCEPTED)).thenReturn(2L);
        when(participantRepository.countByEventId(6L)).thenReturn(2L);

        double rate = eventService.calculateRate(6L);

        assertEquals(40.0, rate);
    }

    @Test
    void classifyEvent_shouldReturnPopulaire() {
        Event event = new Event();
        event.setId(7L);

        when(eventRepository.findById(7L)).thenReturn(Optional.of(event));
        when(participantRepository.countByEventIdAndStatus(7L, ParticipationStatus.ACCEPTED)).thenReturn(4L);
        when(participantRepository.countByEventId(7L)).thenReturn(5L);

        String classification = eventService.classifyEvent(7L);

        assertEquals("POPULAIRE", classification);
    }

    @Test
    void suggestBestTime_shouldReturnBestHour() {
        Event event1 = new Event();
        event1.setId(8L);
        event1.setStatus(EventStatus.OPEN);
        event1.setDateDebut(LocalDateTime.now().withHour(10));

        Event event2 = new Event();
        event2.setId(9L);
        event2.setStatus(EventStatus.OPEN);
        event2.setDateDebut(LocalDateTime.now().withHour(14));

        when(eventRepository.findAll()).thenReturn(List.of(event1, event2));
        when(eventRepository.findById(8L)).thenReturn(Optional.of(event1));
        when(eventRepository.findById(9L)).thenReturn(Optional.of(event2));
        when(participantRepository.countByEventIdAndStatus(8L, ParticipationStatus.ACCEPTED)).thenReturn(4L);
        when(participantRepository.countByEventId(8L)).thenReturn(5L);
        when(participantRepository.countByEventIdAndStatus(9L, ParticipationStatus.ACCEPTED)).thenReturn(1L);
        when(participantRepository.countByEventId(9L)).thenReturn(5L);

        String best = eventService.suggestBestTime();

        assertEquals("10:00", best);
    }

    @Test
    void detectRisk_shouldReturnHighRisk() {
        Event event = new Event();
        event.setId(10L);
        event.setDateDebut(LocalDateTime.now().plusDays(1));

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(participantRepository.countByEventIdAndStatus(10L, ParticipationStatus.ACCEPTED)).thenReturn(1L);
        when(participantRepository.countByEventId(10L)).thenReturn(5L);

        String risk = eventService.detectRisk(10L);

        assertEquals("RISQUE_ELEVÉ", risk);
    }

    @Test
    void updateEventStatusesAutomatically_shouldCloseOpenEvents() {
        Event event = new Event();
        event.setId(11L);
        event.setStatus(EventStatus.OPEN);
        event.setDateDebut(LocalDateTime.now().minusHours(1));

        when(eventRepository.findAll()).thenReturn(List.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        eventService.updateEventStatusesAutomatically();

        assertEquals(EventStatus.CLOSED, event.getStatus());
        verify(eventRepository).save(event);
    }
}
