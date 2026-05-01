package com.wealth.pbor.configuration;

import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.dto.response.HoldingResponse;
import com.wealth.pbor.entity.CashLedger;
import com.wealth.pbor.entity.Holding;
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

        mapper.typeMap(Holding.class, HoldingResponse.class)
                .addMappings(m -> m.map(
                        src -> src.getAccount().getAccountId(),
                        HoldingResponse::setAccountId));

        mapper.typeMap(CashLedger.class, CashLedgerResponse.class)
                .addMappings(m -> m.map(
                        src -> src.getAccount().getAccountId(),
                        CashLedgerResponse::setAccountId));

        return mapper;
    }
}
