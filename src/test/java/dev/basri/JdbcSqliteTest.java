package dev.basri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

public class JdbcSqliteTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSqliteTest.class);

    private static final int DATA_SIZE_BYTES = 16384; // 16 KB

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private int entryCount = 100000;

    private int batchSize = 100;

    private Connection conn;

    @Before
    public void init() throws Exception {
        File dbFile = tempDir.newFile("test.sqlite");
        LOGGER.info("DB FILE: {}", dbFile.getPath());

        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setConfig(Utils.getSqliteConfig());
        ds.setUrl(Utils.getJdbcUrl(dbFile));

        this.conn = ds.getConnection();
        conn.setAutoCommit(false);
        conn.createStatement().executeUpdate(
                "create table if not exists log_entries (log_index int8, log_entry longvarbinary, primary key (log_index))");
        conn.commit();
    }

    @Test
    public void test() throws Exception {
        int index = 0;
        LOGGER.info("START");
        for (int i = 0; i < entryCount / batchSize; i++) {
            if (i % 100 == 0) {
                LOGGER.info("i=" + i);
            }

            PreparedStatement st = conn.prepareStatement("INSERT INTO log_entries VALUES (?, ?)");
            for (int j = 0; j < batchSize; j++) {
                st.setInt(1, index++);
                st.setBinaryStream(2, new ByteArrayInputStream(Utils.randomBytes(DATA_SIZE_BYTES)), DATA_SIZE_BYTES);
                st.addBatch();
            }

            st.executeBatch();
            conn.commit();
        }
        LOGGER.info("END");
        conn.close();
    }

}
