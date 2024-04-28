package org.geoatlas.metadata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 16:10
 * @since: 1.0
 **/
@Configuration
@EnableJdbcAuditing
@EnableJdbcRepositories(basePackages = "org.geoatlas.metadata.persistence.repository")
public class SpringDataConfiguration {
}
