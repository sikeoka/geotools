package org.geotools.data.shapefile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.junit.Test;

/**
 * Verifies that null String, Date, Boolean, Integer, Long, Float, and Double types can be written and read back without
 * losing the proper 'null' Java value.
 *
 * <p>This is a separate test from the DbaseFileTest#testEmptyFields method since it seems to be checking for something
 * else.
 */
public class DbaseNullSupportTest {
    /** declare a specific charset for test portability */
    private static final Charset cs;

    private static final TimeZone tz;

    static {
        cs = StandardCharsets.ISO_8859_1;
        tz = TimeZone.getTimeZone("UTC");
    }

    private static final char[] types = {'C', 'N', 'F', 'L', 'D'};
    private static final int[] sizes = {5, 9, 20, 1, 8};
    private static final int[] decimals = {0, 0, 31, 0, 0};
    /**
     * Creates some non-null values to mix in with the nulls so we have variety. Be sure to use powers of two for the
     * numbers to prevent any possible loss of precision when saving/reloading. Be sure to use a Date with no time
     * component since only the month/day/year are saved.
     */
    private static final Object[] values;

    static {
        Date date;
        try {
            // every jvm should support this, so we should never have an error
            date = new SimpleDateFormat("yyyy-MM-dd z").parse("2010-04-01 UTC");
        } catch (ParseException e) {
            date = null;
        }
        values = new Object[] {"ABCDE", 2 << 20, (2 << 10) + 1d / (2 << 4), true, date};
    }

    public static void main(String[] args) throws IOException {
        new DbaseNullSupportTest().testNulls();
    }

    @Test
    public void testNulls() throws IOException {
        File tmp = File.createTempFile("test", ".dbf");
        if (!tmp.delete()) {
            throw new IllegalStateException("Unable to clear temp file");
        }
        DbaseFileHeader header = new DbaseFileHeader();
        for (int i = 0; i < types.length; i++) {
            header.addColumn("" + types[i], types[i], sizes[i], decimals[i]);
        }
        header.setNumRecords(values.length);
        try (FileOutputStream fos = new FileOutputStream(tmp);
                WritableByteChannel channel = fos.getChannel();
                DbaseFileWriter writer = new DbaseFileWriter(header, channel, cs, tz)) {

            tmp.deleteOnExit();

            // write records such that the i-th row has nulls in every column except the i-th column
            for (int row = 0; row < values.length; row++) {
                Object[] current = new Object[values.length];
                Arrays.fill(current, null);
                current[row] = values[row];
                writer.write(current);
            }
            fos.flush();
        }
        try (FileInputStream in = new FileInputStream(tmp);
                DbaseFileReader reader = new DbaseFileReader(in.getChannel(), false, cs, tz)) {
            assertEquals(
                    "Number of records does not match",
                    values.length,
                    reader.getHeader().getNumRecords());
            for (int row = 0; row < values.length; row++) {
                Object[] current = reader.readEntry();
                assertTrue("Number of columns incorrect", current != null && current.length == values.length);
                for (int column = 0; column < values.length; column++) {
                    if (column == row) {
                        assertNotNull("Column was null and should not have been", current[column]);
                        assertEquals(
                                "Non-null column value "
                                        + current[column]
                                        + " did not match original value "
                                        + values[column],
                                current[column],
                                values[column]);
                    } else {
                        assertNull("Column that should have been null was not", current[column]);
                    }
                }
            }
        }
    }
}
