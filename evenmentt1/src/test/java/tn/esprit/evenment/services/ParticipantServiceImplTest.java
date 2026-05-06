package tn.esprit.evenment.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.evenment.entities.EventStatus;
import tn.esprit.evenment.entities.Participant;
import tn.esprit.evenment.repositories.ParticipantRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceImplTest {

    @Mock
    private ParticipantRepository participantRepository;

    private ParticipantServiceImpl participantService;

    @BeforeEach
    void setUp() {
        participantService = new ParticipantServiceImpl(participantRepository);
    }

    @Test
    void getParticipantsOfClosedEvents_shouldReturnList() {
        Participant participant = new Participant();
        participant.setId(1L);

        when(participantRepository.findByEventStatus(EventStatus.CLOSED)).thenReturn(List.of(participant));

        List<Participant> participants = participantService.getParticipantsOfClosedEvents();

        assertEquals(1, participants.size());
        assertEquals(1L, participants.get(0).getId());
    }

    @Test
    void addParticipant_shouldSaveParticipant() {
        Participant participant = new Participant();
        participant.setId(2L);

        when(participantRepository.save(participant)).thenReturn(participant);

        Participant saved = participantService.addParticipant(participant);

        assertNotNull(saved);
        assertEquals(2L, saved.getId());
    }

    @Test
    void updateParticipant_shouldSaveParticipant() {
        Participant participant = new Participant();
        participant.setId(3L);

        when(participantRepository.save(participant)).thenReturn(participant);

        Participant updated = participantService.updateParticipant(participant);

        assertNotNull(updated);
        assertEquals(3L, updated.getId());
    }

    @Test
    void getParticipantById_shouldReturnParticipant() {
        Participant participant = new Participant();
        participant.setId(4L);

        when(participantRepository.findById(4L)).thenReturn(Optional.of(participant));

        Participant found = participantService.getParticipantById(4L);

        assertNotNull(found);
        assertEquals(4L, found.getId());
    }

    @Test
    void getAllParticipants_shouldReturnAll() {
        Participant participant = new Participant();
        participant.setId(5L);

        when(participantRepository.findAll()).thenReturn(List.of(participant));

        List<Participant> participants = participantService.getAllParticipants();

        assertEquals(1, participants.size());
        assertEquals(5L, participants.get(0).getId());
    }

    @Test
    void deleteParticipant_shouldCallRepository() {
        participantService.deleteParticipant(6L);

        verify(participantRepository).deleteById(6L);
    }

    @Test
    void getParticipantsByEvent_shouldReturnList() {
        Participant participant = new Participant();
        participant.setId(7L);

        when(participantRepository.findByEventId(7L)).thenReturn(List.of(participant));

        List<Participant> participants = participantService.getParticipantsByEvent(7L);

        assertEquals(1, participants.size());
        assertEquals(7L, participants.get(0).getId());
    }
}
