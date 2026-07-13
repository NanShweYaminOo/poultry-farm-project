package com.poultry.broiler_farming_system.security;

import com.poultry.broiler_farming_system.exception.ApiError;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

// GlobalExceptionHandler can return ApiError as a plain object because it
// runs inside Spring MVC's normal response pipeline (message converters
// handle serialization there). Spring Security's entry point / access
// denied handler run outside that pipeline, so this writes the same
// ApiError shape by hand -- no Jackson ObjectMapper bean is guaranteed to
// be compile-visible here (it's only pulled in transitively at runtime
// scope via jjwt-jackson).
final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    static void write(HttpServletResponse response, int status, String error, String message) throws IOException {
        ApiError apiError = new ApiError(LocalDateTime.now(), status, error, message);
        String json = "{\"timestamp\":\"" + apiError.timestamp() + "\","
                + "\"status\":" + apiError.status() + ","
                + "\"error\":\"" + escape(apiError.error()) + "\","
                + "\"message\":\"" + escape(apiError.message()) + "\"}";
        response.getWriter().write(json);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
