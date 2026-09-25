package com.certificados.app.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones del backend a respuestas JSON uniformes:
 *
 *   { "timestamp": "...", "status": 400, "error": "...", "messages": ["..."] }
 *
 * El panel (app.js -> leerMensajeError) lee siempre el campo "messages".
 *
 * NO MODIFICAR el nombre del campo "messages" ni el handler generico del
 * final: este ultimo evita que se filtren trazas, rutas o SQL internos al
 * navegador (el detalle real queda en la consola del servidor).
 * SE PUEDE MODIFICAR: los textos de los mensajes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ApiError> responder(HttpStatus status, String error, List<String> mensajes) {
        ApiError body = new ApiError(LocalDateTime.now().toString(), status.value(), error, mensajes);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "No encontrado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Solicitud invalida", List.of(ex.getMessage()));
    }

    /** Validaciones de negocio lanzadas con IllegalArgumentException en los servicios. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Solicitud invalida", List.of(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        return responder(HttpStatus.BAD_REQUEST, "Error de validacion", mensajes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Error de lectura",
                List.of("Los datos enviados no tienen el formato esperado."));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiError> handleMissingParam(Exception ex) {
        return responder(HttpStatus.BAD_REQUEST, "Dato faltante",
                List.of("Falta un dato obligatorio en la solicitud."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Dato invalido",
                List.of("El valor de '" + ex.getName() + "' no es valido."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return responder(HttpStatus.PAYLOAD_TOO_LARGE, "Archivo muy pesado",
                List.of("El archivo supera el tamano maximo permitido (20 MB)."));
    }

    /** Duplicados (codigos unicos) o referencias a registros inexistentes. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        LOG.warn("Violacion de integridad: {}", ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "Conflicto de datos",
                List.of("El registro ya existe o hace referencia a un dato que no existe."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "No encontrado", List.of("Recurso no encontrado."));
    }

    /** Cualquier otro error: mensaje generico hacia fuera, detalle solo en consola. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        LOG.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                List.of("Ocurrio un error inesperado. Revisa la consola del servidor."));
    }
}
