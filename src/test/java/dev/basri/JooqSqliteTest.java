package dev.basri;

import java.io.File;
import java.sql.Connection;

import org.jooq.CloseableDSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
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

    private static final int DATA_SIZE_BYTES = 16384; // 16 KB

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private int entryCount = 100000;

    private int batchSize = 100;

    private CloseableDSLContext dsl;

    @Before
    public void before() throws Exception {
        File sqliteDb = new File(tempDir.newFolder(), "sqlite.db");
        this.dsl = DSL.using(Utils.getJdbcUrl(sqliteDb), Utils.getSqliteConfig().toProperties());
        dsl.connection(conn -> conn.setAutoCommit(false));

        dsl.createTableIfNotExists(LOG_ENTRIES_TABLE).column(INDEX_FIELD).column(LOG_ENTRY_FIELD)
                .primaryKey(INDEX_FIELD).execute();

        dsl.connection(Connection::commit);
    }

    @Test
    public void test() throws Exception {
        long index = 0;
        LOGGER.info("START");
        for (int i = 0; i < entryCount / batchSize; i++) {
            if (i % 100 == 0) {
                LOGGER.info("i=" + i);
            }

            for (int j = 0; j < batchSize; j++) {
                dsl.insertInto(LOG_ENTRIES_TABLE, INDEX_FIELD, LOG_ENTRY_FIELD)
                        .values(index++, Utils.randomBytes(DATA_SIZE_BYTES))
                        .execute();
            }

            dsl.connection(Connection::commit);
        }
        LOGGER.info("END");
    }

}
