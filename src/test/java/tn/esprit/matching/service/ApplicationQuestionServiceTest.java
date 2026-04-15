package tn.esprit.matching.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.matching.entity.ApplicationQuestion;
import tn.esprit.matching.repository.ApplicationQuestionRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationQuestionServiceTest {

    @Mock
    private ApplicationQuestionRepository applicationQuestionRepository;

    @InjectMocks
    private ApplicationQuestionService applicationQuestionService;

    @Test
    void getQuestionsForInvitation_shouldReturnQuestionsOrderedByIndex() {
        // given
        ApplicationQuestion q1 = new ApplicationQuestion();
        q1.setOrderIndex(1);
        q1.setLabel("Question 1");

        ApplicationQuestion q2 = new ApplicationQuestion();
        q2.setOrderIndex(2);
        q2.setLabel("Question 2");

        given(applicationQuestionRepository.findAllByOrderByOrderIndexAsc())
                .willReturn(List.of(q1, q2));

        // when
        List<ApplicationQuestion> result =
                applicationQuestionService.getQuestionsForInvitation(123L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLabel()).isEqualTo("Question 1");
        assertThat(result.get(1).getLabel()).isEqualTo("Question 2");

        verify(applicationQuestionRepository).findAllByOrderByOrderIndexAsc();
    }
}