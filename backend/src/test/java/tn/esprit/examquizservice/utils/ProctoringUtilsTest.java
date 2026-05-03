package tn.esprit.examquizservice.utils;

import org.junit.jupiter.api.Test;
import tn.esprit.examquizservice.dtos.RecordViolationRequest;
import tn.esprit.examquizservice.dtos.RecordViolationResponse;
import tn.esprit.examquizservice.dtos.ViolationStatus;
import tn.esprit.examquizservice.entities.ExamViolationType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProctoringUtilsTest {

    @Test
    void mapperShouldValidateRequestAndParseTypes() {
        RecordViolationRequest valid = RecordViolationRequest.builder()
                .examId(1L)
                .userId(2L)
                .type(ExamViolationType.TAB_SWITCH)
                .build();

        RecordViolationRequest invalid = RecordViolationRequest.builder()
                .examId(1L)
                .build();

        assertTrue(ProctoringMapper.isValidViolationRequest(valid));
        assertFalse(ProctoringMapper.isValidViolationRequest(invalid));
        assertEquals(ExamViolationType.NO_FACE, ProctoringMapper.parseViolationType("no_face"));
        assertNull(ProctoringMapper.parseViolationType("does_not_exist"));
    }

    @Test
    void requestBuilderShouldBuildAndReset() {
        ViolationRequestBuilder builder = new ViolationRequestBuilder()
                .examId(5L)
                .userId(6L)
                .type("tab_switch")
                .details("switched");

        RecordViolationRequest request = builder.build();

        assertEquals(5L, request.getExamId());
        assertEquals(6L, request.getUserId());
        assertEquals(ExamViolationType.TAB_SWITCH, request.getType());

        builder.reset();
        RecordViolationRequest empty = builder.build();
        assertNull(empty.getExamId());
    }

    @Test
    void requestBuilderShouldRejectInvalidType() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ViolationRequestBuilder.builder().type("bad_type"));

        assertTrue(ex.getMessage().contains("Invalid violation type"));
    }

    @Test
    void helperShouldCreateAndValidateResponse() {
        RecordViolationRequest request = ProctoringTestHelper.createSampleViolation(10L, 20L, ExamViolationType.PHONE_DETECTED);

        assertEquals(10L, request.getExamId());
        assertEquals(20L, request.getUserId());
        assertTrue(request.getDetails().contains("PHONE_DETECTED"));

        RecordViolationResponse response = RecordViolationResponse.builder()
                .status(ViolationStatus.WARNING)
                .violationCount(1)
                .message("ok")
                .action("CONTINUE")
                .build();

        assertTrue(ProctoringTestHelper.isValidResponse(response));

        ProctoringTestHelper.logViolationResponse(response);

        assertNotNull(ProctoringTestHelper.SampleViolations.TAB_SWITCH.getDescription());
        assertEquals(ExamViolationType.TAB_SWITCH, ProctoringTestHelper.SampleViolations.TAB_SWITCH.getType());
    }
}
