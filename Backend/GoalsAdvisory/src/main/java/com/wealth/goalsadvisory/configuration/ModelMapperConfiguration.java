package com.wealth.goalsadvisory.configuration;

import com.wealth.goalsadvisory.dto.response.RecommendationResponse;
import com.wealth.goalsadvisory.entity.Recommendation;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfiguration {

    @Bean
    public ModelMapper mapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.typeMap(Recommendation.class, RecommendationResponse.class).addMappings(m -> {
                    m.map(src -> src.getModelPortfolio().getModelId(),
                            RecommendationResponse::setModelId);
                    m.map(src -> src.getModelPortfolio().getName(),
                            RecommendationResponse::setModelName);
                    m.map(src -> src.getModelPortfolio().getRiskClass(),
                            RecommendationResponse::setRiskClass);
                });

        return mapper;
    }
}