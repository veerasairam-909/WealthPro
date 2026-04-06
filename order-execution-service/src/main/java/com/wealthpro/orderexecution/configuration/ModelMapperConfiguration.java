package com.wealthpro.orderexecution.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class providing a globally-shared {@link ModelMapper} bean.
 * <p>
 * ModelMapper is used throughout the service layer to convert
 * between JPA entities and DTOs, keeping these layers cleanly separated.
 * </p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Configuration
public class ModelMapperConfiguration {

    /**
     * Creates a {@link ModelMapper} bean with null-skipping enabled.
     * <p>Null-skipping ensures partial updates do not overwrite existing
     * non-null entity fields with null values from DTOs.</p>
     *
     * @return configured ModelMapper instance
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(true);
        return modelMapper;
    }
}
