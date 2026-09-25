package com.certificados.app.dto;

/**
 * Respuesta generica { success, message, data }.
 *
 * Se escribio sin Lombok a proposito: Lombok falla al compilar con JDK
 * recientes (ExceptionInInitializerError en :compileJava) y era la unica
 * clase del proyecto que lo usaba.
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
