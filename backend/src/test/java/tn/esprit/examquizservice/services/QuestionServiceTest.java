package tn.esprit.examquizservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.examquizservice.entities.Question;
import tn.esprit.examquizservice.repositories.QuestionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository repository;

    @InjectMocks
    private QuestionService questionService;

    @Test
    void findByExamIdShouldDelegateToRepository() {
        List<Question> expected = List.of(new Question());
        when(repository.findByExamId(3L)).thenReturn(expected);

        List<Question> result = questionService.findByExamId(3L);

        assertEquals(expected, result);
    }

    @Test
    void updateShouldPatchOnlyProvidedFields() {
        Question existing = new Question();
        existing.setId(1L);
        existing.setQuestionText("Old question");
        existing.setPoints(2.0);

        Question payload = new Question();
        payload.setQuestionText("New question");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Question saved = questionService.update(1L, payload);

        assertEquals("New question", saved.getQuestionText());
        assertEquals(2.0, saved.getPoints());
    }

    @Test
    void findByIdShouldThrowWhenMissing() {
        when(repository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> questionService.findById(100L));

        assertEquals("Question not found with id: 100", ex.getMessage());
    }

    @Test
    void deleteShouldCallRepository() {
        questionService.delete(9L);
        verify(repository).deleteById(9L);
    }
}
