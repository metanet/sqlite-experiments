package dev.basri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

public class JdbcSqliteTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSqliteTest.class);

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private File dbFile;
    private Connection conn;

    @Before
    public void init() throws Exception {
        this.dbFile = tempDir.newFile("test.sqlite");
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

    @After
    public void shutdown() throws Exception {
        conn.close();
        LOGGER.info("File size in bytes: {} ", dbFile.length());
    }

    @Test
    public void test() throws Exception {
        int index = 0;
        LOGGER.info("START");
        for (int i = 0, j = Utils.ENTRY_COUNT / Utils.BATCH_SIZE; i < j; i++) {
            if (i % 100 == 0) {
                LOGGER.info("i=" + i);
            }

            PreparedStatement st = conn.prepareStatement("INSERT OR IGNORE INTO log_entries VALUES (?, ?)");
            for (int k = 0; k < Utils.BATCH_SIZE; k++) {
                st.setInt(1, index++);
                byte[] data = Utils.randomBytes();
                st.setBinaryStream(2, new ByteArrayInputStream(data), data.length);
                st.addBatch();
            }

            st.executeBatch();
            conn.commit();
        }
        LOGGER.info("END");
    }

}
