package tn.esprit.matching.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.matching.entity.ApplicationQuestion;
import tn.esprit.matching.service.ApplicationQuestionService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationQuestionController.class)
class ApplicationQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationQuestionService applicationQuestionService;

    @Test
    void getForInvitation_shouldReturnListOfQuestions() throws Exception {
        // given
        ApplicationQuestion q1 = new ApplicationQuestion();
        q1.setOrderIndex(1);
        q1.setLabel("Question 1");

        ApplicationQuestion q2 = new ApplicationQuestion();
        q2.setOrderIndex(2);
        q2.setLabel("Question 2");

        given(applicationQuestionService.getQuestionsForInvitation(10L))
                .willReturn(List.of(q1, q2));

        // when + then
        mockMvc.perform(get("/application-questions/invitation/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].label", is("Question 1")))
                .andExpect(jsonPath("$[1].label", is("Question 2")));
    }
}