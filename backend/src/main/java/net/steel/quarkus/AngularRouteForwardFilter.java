package net.steel.quarkus;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = "/*")
public class AngularRouteForwardFilter extends HttpFilter {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(".*[.][a-zA-Z\\d]+");

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        chain.doFilter(request, response);

        // Prevent forwarding loop if index does not exists
        String requestUri = request.getRequestURI();
        if (requestUri.isEmpty() || requestUri.equals("/")) {
            return;
        }

        if (response.getStatus() == 404) {
            String path = request.getRequestURI().substring(
                    request.getContextPath().length()).replaceAll("[/]+$", "");
            // Only forward if it is not a request to a file
            if (!FILE_NAME_PATTERN.matcher(path).matches()) {
               try {
                    response.setStatus(200);
                    request.getRequestDispatcher("/").forward(request, response);
                } finally {
                    response.getOutputStream().close();
                }
            }
        }
    }
}
