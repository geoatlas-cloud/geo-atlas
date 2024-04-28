package org.geoatlas.metadata.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 16:51
 * @since: 1.0
 **/
@Configuration
public class ModelMapperConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = ModelMapper.class)
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        /**
         * 避免source中的null值覆盖destination中的匹配属性
         **/
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setFullTypeMatchingRequired(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
