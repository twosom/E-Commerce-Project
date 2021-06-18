package io.twosom.ecommerce.config;

import io.twosom.ecommerce.category.CategoryInterceptor;
import io.twosom.ecommerce.shoppingbag.ShoppingBagInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CategoryInterceptor categoryInterceptor;
    private final ShoppingBagInterceptor shoppingBagInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> staticResourcesPatterns = Arrays.stream(StaticResourceLocation.values()).flatMap(StaticResourceLocation::getPatterns)
                .collect(Collectors.toList());

        registry.addInterceptor(categoryInterceptor)
                .excludePathPatterns(staticResourcesPatterns);

        registry.addInterceptor(shoppingBagInterceptor)
                .excludePathPatterns(staticResourcesPatterns);
    }
}
