package com.sgpanalyzer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI parlayPricerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ParlayPricer API")
                        .version("1.0.0")
                        .description("""
                                Correlated Monte Carlo pricing engine for Same Game Parlays.
                                
                                Estimates true joint probabilities using correlated simulations,
                                computes fair odds via Cholesky decomposition, and identifies
                                mispriced parlay markets.
                                """)
                        .contact(new Contact()
                                .name("ParlayPricer")));
    }
}
