package tn.esprit.challengeservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import tn.esprit.challengeservice.exceptions.SonarResultsNotFoundException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleSonarResultsNotFound_withNullMessage_shouldUseDefault() {
        SonarResultsNotFoundException ex = new SonarResultsNotFoundException(null);

        ResponseEntity<Map<String, String>> response = handler.handleSonarResultsNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("SonarCloud results not ready yet", response.getBody().get("error"));
    }

    @Test
    void handleRuntimeException_withNullMessage_shouldUseDefault() {
        RuntimeException ex = new RuntimeException((String) null);

        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("An error occurred", response.getBody().get("error"));
    }
}
