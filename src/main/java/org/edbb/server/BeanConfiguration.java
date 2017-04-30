package org.edbb.server;

import org.edbb.data.DataStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans we want
 */
@Configuration
public class BeanConfiguration {
    @Bean
    public DataStore getDataStore() {
        return new DataStore();
    }

}
