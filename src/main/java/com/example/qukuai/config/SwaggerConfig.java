package com.example.qukuai.config;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author deray.wang
 * @date 2019/11/21 11:24
 */
@Slf4j
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${swagger.enable:true}")
    private boolean enable;

    @Value("${swagger.rest-paths:com.example}")
    private String restPaths;

    @Value("${swagger.api-title:数据前台API}")
    private String apiTitle;

    @Value("${swagger.api-description:数据前台报表的取数API和数据对象管理API}")
    private String apiDescription;

    @Value("${swagger.api-version:0.0.1}")
    private String apiVersion;

    @Value("${swagger.service-host}")
    private String serviceHost;

    @Bean
    public Docket swaggerInject() {
        log.info("{}'s swagger injecting for {} with {}", apiTitle, restPaths, enable);
        return new Docket(DocumentationType.SWAGGER_2)
                .host(serviceHost)
                .enable(enable)
                .apiInfo(buildApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(this.restPaths))
                .paths(PathSelectors.any())
                .build()
                //.globalOperationParameters(setHeaderToken())
                .securitySchemes(securitySchemes());
//                .securityContexts(securityContexts());


    }

    private List<Parameter> setHeaderToken() {
        ParameterBuilder tokenPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<>();
        //tokenPar.name("Authorization").description("Authorization").modelRef(new ModelRef("string")).parameterType("header").defaultValue("Bearer ").required(false).build();
        tokenPar.name("Authorization").description("Authorization").modelRef(new ModelRef("string")).parameterType("header").defaultValue("").required(false).build();
        pars.add(tokenPar.build());
        return pars;
    }

    private List<ApiKey> securitySchemes() {
        List<ApiKey> apiKeyList= new ArrayList<>();
        apiKeyList.add(new ApiKey("Authorization", "Authorization", "header"));
        return apiKeyList;
    }

    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts=new ArrayList<>();
        securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .forPaths(PathSelectors.any())
                        .build());
        return securityContexts;
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
        return securityReferences;
    }

    private ApiInfo buildApiInfo() {
        return new ApiInfoBuilder()
                .title(this.apiTitle)
                .description(this.apiTitle)
                .version(this.apiVersion)
                .build();
    }
}
