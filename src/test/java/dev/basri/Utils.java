package dev.basri;

import java.io.File;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.SQLiteConfig.Pragma;
import org.sqlite.SQLiteConfig.TempStore;

public final class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static final Random RANDOM = new Random();

    public static final int DATA_SIZE_BYTES;

    public static final int ENTRY_COUNT;

    public static final int BATCH_SIZE;

    static {
        DATA_SIZE_BYTES = Integer.parseInt(System.getProperty("dataSizeBytes", Integer.toString(16384)));

        ENTRY_COUNT = Integer.parseInt(System.getProperty("entryCount", Integer.toString(100_000)));

        BATCH_SIZE = Integer.parseInt(System.getProperty("batchSize", Integer.toString(100)));

        LOGGER.info("Data size bytes: {}, entry count: {}, batch size: {}", DATA_SIZE_BYTES, ENTRY_COUNT, BATCH_SIZE);
    }

    private Utils() {
    }

    public static SQLiteConfig getSqliteConfig() {
        SQLiteConfig config = new SQLiteConfig();

        // https://www.sqlite.org/pragma.html#pragma_journal_mode
        config.setPragma(Pragma.JOURNAL_MODE, JournalMode.WAL.getValue());
        // config.setPragma(Pragma.JOURNAL_MODE, JournalMode.OFF.getValue());
        // https://www.sqlite.org/pragma.html#pragma_locking_mode
        // database file will be owned by the connection for the whole life-time of the
        // connection.
        config.setPragma(Pragma.LOCKING_MODE, SQLiteConfig.LockingMode.EXCLUSIVE.getValue());
        // https://www.sqlite.org/pragma.html#pragma_synchronous
        config.setPragma(Pragma.SYNCHRONOUS, SQLiteConfig.SynchronousMode.FULL.name());
        // config.setPragma(Pragma.SYNCHRONOUS,
        // SQLiteConfig.SynchronousMode.OFF.name());

        config.setPageSize(65536); // 64 KB
        config.setTempStore(TempStore.MEMORY);
        config.setCacheSize(4194304); // 4 MB

        return config;
    }

    public static String getJdbcUrl(File dbFile) {
        return "jdbc:sqlite:" + dbFile.getPath();
    }

    public static byte[] randomBytes() {
        byte[] b = new byte[DATA_SIZE_BYTES];
        RANDOM.nextBytes(b);
        return b;
    }
}
