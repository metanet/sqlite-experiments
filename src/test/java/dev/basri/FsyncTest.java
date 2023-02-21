package dev.basri;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FsyncTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FsyncTest.class);


    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private FileOutputStream fos;
    private BufferedOutputStream bos;

    @Before
    public void init() throws Exception {
        File file = tempDir.newFile();
        this.fos = new FileOutputStream(file);
        this.bos = new BufferedOutputStream(fos, Utils.BATCH_SIZE * Utils.DATA_SIZE_BYTES);
    }

    @After
    public void shutdown() throws Exception {
        fos.close();
    }

    @Test
    public void test() throws Exception {
        LOGGER.info("START");
        for (int i = 0, j = Utils.ENTRY_COUNT / Utils.BATCH_SIZE; i < j; i++) {
            if (i % 100 == 0) {
                LOGGER.info("i=" + i);
            }

            for (int k = 0; k < Utils.BATCH_SIZE; k++) {
                byte[] data = Utils.randomBytes();
                bos.write(data);
            }

            bos.flush();
            // fos.getFD().sync();
            fos.getChannel().force(false);
        }
        LOGGER.info("END");
    }
    
}
