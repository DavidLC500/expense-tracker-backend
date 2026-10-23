package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.exception.CategoryAlreadyExistsException;
import com.expensetracker.application.exception.NotFoundException;
import com.expensetracker.domain.exception.DomainValidationException;
import jakarta.validation.ConstraintViolationException;
import java.time.DateTimeException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones de dominio/aplicación y de validación a respuestas {@link ProblemDetail}
 * (RFC 9457), en el único punto de la aplicación que conoce ambos mundos (HTTP y excepciones de
 * negocio).
 *
 * <p>Extiende {@link ResponseEntityExceptionHandler} para que las excepciones estándar de Spring
 * MVC (JSON mal formado, tipos incorrectos, 404, 405, 415...) se resuelvan aquí y no en el handler
 * que Spring Boot registra por su cuenta, que se adelantaba a este y descartaba los {@code
 * fieldErrors}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler({
    DomainValidationException.class,
    IllegalArgumentException.class,
    DateTimeException.class
  })
  public ProblemDetail handleBadRequest(RuntimeException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(RuntimeException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(CategoryAlreadyExistsException.class)
  public ProblemDetail handleConflict(RuntimeException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    ProblemDetail problem = exception.getBody();
    problem.setDetail("Request body failed validation");
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    problem.setProperty("fieldErrors", fieldErrors);
    return handleExceptionInternal(exception, problem, headers, status, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  /**
   * Red de seguridad: las carreras conocidas contra restricciones únicas ya se resuelven en
   * persistencia (la importación con {@code ON CONFLICT DO NOTHING} y los presupuestos reintentando
   * como actualización en {@code JpaBudgetRepository}). Este handler cubre cualquier otra violación
   * de integridad no prevista, para que nunca llegue como un 500 sin formato.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, "The request conflicts with the current state of the data");
  }

  /**
   * La base de datos no responde (caída o sin conexiones libres): 503, un fallo temporal que el
   * cliente puede reintentar, y no el 500 de error inesperado.
   */
  @ExceptionHandler({
    DataAccessResourceFailureException.class,
    CannotCreateTransactionException.class
  })
  public ProblemDetail handleDatabaseUnavailable(RuntimeException exception) {
    LOG.warn("Database unavailable: {}", exception.getMessage());
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.SERVICE_UNAVAILABLE, "The database is not available right now");
  }

  /**
   * Última red de seguridad: cualquier excepción no prevista explícitamente (p. ej. la base de
   * datos no está disponible) también responde en {@link ProblemDetail}, nunca con el HTML/JSON por
   * defecto de Spring Boot. El detalle que ve el cliente es deliberadamente genérico — no filtra
   * mensajes internos de infraestructura — pero se registra el error completo en el LOG del
   * servidor para poder diagnosticarlo.
   */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception exception) {
    LOG.error("Unhandled exception", exception);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
  }
}
