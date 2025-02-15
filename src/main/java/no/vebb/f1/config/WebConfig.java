package no.vebb.f1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import no.vebb.f1.components.BreadcrumbInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private BreadcrumbInterceptor breadcrumbInterceptor;

	@SuppressWarnings("null")
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(breadcrumbInterceptor);
    }
}