package com.tpanh.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PageableConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(final java.util.List<HandlerMethodArgumentResolver> resolvers) {
        final var pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setMaxPageSize(PaginationConfig.MAX_PAGE_SIZE);
        pageableResolver.setPageParameterName("page");
        pageableResolver.setSizeParameterName("size");
        pageableResolver.setOneIndexedParameters(false);
        resolvers.add(pageableResolver);

        final var sortResolver = new SortHandlerMethodArgumentResolver();
        resolvers.add(sortResolver);
    }
}
