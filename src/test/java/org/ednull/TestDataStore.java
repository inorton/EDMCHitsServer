package org.ednull;

import org.ednull.hits.data.SQLiteDataStore;
import org.junit.Test;

import java.sql.SQLException;


public class TestDataStore {

    @Test
    public void testAddData() throws Exception {
        SQLiteDataStore data = new SQLiteDataStore("jdbc:sqlite:./testdata.sqlite");

    }
}
