package backend.website.gbcc.handler;

import backend.website.gbcc.model.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/test-path");
    }

    @Test
    void handleMessageNotReadable_shouldReturnBadRequest() {
        HttpInputMessage inputMessage = org.mockito.Mockito.mock(HttpInputMessage.class);
        ResponseEntity<ApiErrorResponse> response = handler.handleMessageNotReadable(
                new HttpMessageNotReadableException("Malformed", inputMessage),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Malformed request body");
        assertThat(response.getBody().path()).isEqualTo("/test-path");
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnConflict() {
        ResponseEntity<ApiErrorResponse> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("Constraint failed"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Data integrity violation");
        assertThat(response.getBody().path()).isEqualTo("/test-path");
    }

    @Test
    void handleResponseStatus_shouldUseReasonMessage() {
        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Resource not found");
        assertThat(response.getBody().path()).isEqualTo("/test-path");
    }
}
