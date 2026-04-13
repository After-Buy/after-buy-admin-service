package com.After_Buy.AdminService.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 전역 Swagger(OpenAPI) 설정
 *
 * @author 최준혁
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 체계 정의
        SecurityScheme bearerAction = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 요청 건마다 해당 보안 체계(Authorization 헤더)가 필요함을 의미하는 SecurityRequirement 생성
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("After-Buy Admin Service API")
                        .description("관리자 서비스용 API 명세서")
                        .version("1.0.0"))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAction))
                .addSecurityItem(securityRequirement);
    }
}
