package com.example.FinanceDemoApi.financeDemo.Utility;
import com.example.FinanceDemoApi.financeDemo.Error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
public class SetJsonResponse {
    public void setJsonErrorResponse(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Creating the json response
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(new ErrorResponse(message));
            response.getWriter().write(jsonResponse);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
