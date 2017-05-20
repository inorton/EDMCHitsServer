package org.ednull.hits.server;

import org.ednull.hits.data.DataStore;
import org.ednull.hits.data.SQLiteDataStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

/**
 * Beans we want
 */
@Configuration
public class BeanConfiguration {
    @Bean
    public DataStore getDatabaseInstance() throws SQLException {
        return new SQLiteDataStore("jdbc:sqlite:./hits.sqlite");
    }

}
