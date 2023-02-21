package dev.basri;

import java.io.File;
import java.sql.Connection;

import org.jooq.CloseableDSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JooqSqliteTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JooqSqliteTest.class);

    private static final Table<Record> LOG_ENTRIES_TABLE = DSL.table("log_entries");
    private static final Field<Long> INDEX_FIELD = DSL.field("log_index", SQLDataType.BIGINT);
    private static final Field<byte[]> LOG_ENTRY_FIELD = DSL.field("log_entry", SQLDataType.BINARY);

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private File dbFile;
    private CloseableDSLContext dsl;

    @Before
    public void init() throws Exception {
        this.dbFile = new File(tempDir.newFolder(), "sqlite.db");
        LOGGER.info("DB FILE: {}", dbFile.getPath());
        this.dsl = DSL.using(Utils.getJdbcUrl(dbFile), Utils.getSqliteConfig().toProperties());
        dsl.connection(conn -> conn.setAutoCommit(false));

        dsl.createTableIfNotExists(LOG_ENTRIES_TABLE).column(INDEX_FIELD).column(LOG_ENTRY_FIELD)
                .primaryKey(INDEX_FIELD).execute();

        dsl.connection(Connection::commit);
    }

    @After
    public void shutdown() throws Exception {
        dsl.close();
        LOGGER.info("File size in bytes: {} ", dbFile.length());
    }

    @Test
    public void test() throws Exception {
        long index = 0;
        LOGGER.info("START");
        for (int i = 0, j = Utils.ENTRY_COUNT / Utils.BATCH_SIZE; i < j; i++) {
            if (i % 100 == 0) {
                LOGGER.info("i=" + i);
            }

            for (int k = 0; k < Utils.BATCH_SIZE; k++) {
                dsl.insertInto(LOG_ENTRIES_TABLE, INDEX_FIELD, LOG_ENTRY_FIELD)
                        .values(index++, Utils.randomBytes())
                        .onDuplicateKeyIgnore()
                        .execute();
            }

            dsl.connection(Connection::commit);
        }
        LOGGER.info("END");
    }

}
