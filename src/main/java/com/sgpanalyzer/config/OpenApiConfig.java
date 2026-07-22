package com.sgpanalyzer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sgpAnalyzerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SGP Analyzer API")
                        .version("1.0.0")
                        .description("""
                                Same Game Parlay correlation analysis and fair odds engine.
                                
                                Analyzes betting parlays by computing player correlations from historical data,
                                running Monte Carlo simulations, and calculating fair odds + expected value.
                                """)
                        .contact(new Contact()
                                .name("SGP Analyzer")));
    }
}
