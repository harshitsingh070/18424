package backend.notification_app_be.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestResponseLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String query = httpRequest.getQueryString();

        System.out.println("[REQUEST] " + method + " " + uri + (query != null ? "?" + query : ""));

        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[RESPONSE] " + method + " " + uri + " - Status: " + httpResponse.getStatus() + " - Time: " + duration + "ms");
    }
}
