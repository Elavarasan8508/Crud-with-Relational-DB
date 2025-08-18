package controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RootStatusController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = """
            {
                "status": "OK",
                "message": "JDBC Video Rental Backend is running",
                "timestamp": "%s",
                "endpoints": {
                    "customers": "/customers/*",
                    "films": "/films/*",
                    "stores": "/stores/*",
                    "rentals": "/rentals/*"
                }
            }
            """.formatted(java.time.Instant.now());
        
        response.getWriter().write(jsonResponse);
    }
}
