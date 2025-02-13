package no.vebb.f1.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class BreadcrumbInterceptor implements HandlerInterceptor {
	
	@SuppressWarnings("null")
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Map<String, String> breadcrumbs = new LinkedHashMap<>();
		request.setAttribute("breadcrumbs", breadcrumbs);
		String path = request.getRequestURI();
		if (path.equals("/")) {
			breadcrumbs.put("Hjem", null);
			return true;
		}
		addBreadcrumbs(breadcrumbs, path);
        return true;
    }

	private void addBreadcrumbs(Map<String, String> breadcrumbs, String path) {
		breadcrumbs.put("Hjem", "/");
		List<String> subPaths = getSubPaths(path);
		subPaths.remove(subPaths.size() - 1);
		for (String subPath : subPaths) {
			breadcrumbs.put(getNameForPath(subPath), subPath);
		}
		breadcrumbs.put(getNameForPath(path), null);
	}

	private List<String> getSubPaths(String path) {
		List<String> result = new ArrayList<>();
		String[] segments = path.split("/");

		StringBuilder currentPath = new StringBuilder();
		for (String segment : segments) {
			if (segment.isEmpty()) {
				continue;
			}
			currentPath.append("/").append(segment);
			result.add(currentPath.toString());
		}
		
		return result;
	}

	private String getNameForPath(String path) {
		return path;
	}
}
