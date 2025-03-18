package no.vebb.f1.components;

import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class SavedRequestFilter extends OncePerRequestFilter {

    @Override
	@SuppressWarnings("null")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
		final Principal principal = request.getUserPrincipal();
		if (principal == null) {			
            String originalRequest = request.getRequestURI();
			if (!originalRequest.matches("/favicon.ico|/.*\\.css|/error|/login*")) {
				SavedRequestImpl savedRequest = new SavedRequestImpl(originalRequest);
            	request.getSession().setAttribute("SPRING_SECURITY_SAVED_REQUEST", savedRequest);
			}
        }
        chain.doFilter(request, response);
    }

	private class SavedRequestImpl implements SavedRequest {

        private final String redirectUrl;

        public SavedRequestImpl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        @Override
        public String getRedirectUrl() {
            return redirectUrl;
        }

        @Override
        public List<Cookie> getCookies() {
            return null;
        }

        @Override
        public String getMethod() {
            return null;
        }

        @Override
        public List<String> getHeaderValues(String name) {
            return null;
        }

        @Override
        public Collection<String> getHeaderNames() {
            return null;
        }

        @Override
        public List<Locale> getLocales() {
            return null;
        }

        @Override
        public String[] getParameterValues(String name) {
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return null;
        }
    }
}