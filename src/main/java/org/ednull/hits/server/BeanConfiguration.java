package org.ednull.hits.server;

import org.ednull.hits.data.DataStore;
import org.ednull.hits.data.IncidentScanner;
import org.ednull.hits.data.SQLiteDataStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.crypto.Data;
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

    @Bean
    public EddnPump getDataPump(IncidentScanner scanner) {
        return new EddnPump(scanner);
    }

    @Bean
    public IncidentScanner getIncidentScanner(DataStore dataStore) {
        return new IncidentScanner(dataStore);
    }


}
