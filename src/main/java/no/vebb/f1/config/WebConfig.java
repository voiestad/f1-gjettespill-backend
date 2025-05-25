package no.vebb.f1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import no.vebb.f1.components.BreadcrumbInterceptor;
import no.vebb.f1.components.HeaderInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private BreadcrumbInterceptor breadcrumbInterceptor;

    @Autowired
    private HeaderInterceptor headerInterceptor;

    @Override
    public void addInterceptors(@SuppressWarnings("null") InterceptorRegistry registry) {
        registry.addInterceptor(breadcrumbInterceptor);
        registry.addInterceptor(headerInterceptor);
    }

    @Override
    public void addCorsMappings(@SuppressWarnings("null") CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173/")
            .allowCredentials(true)
            .allowedHeaders("*");
    }
}
