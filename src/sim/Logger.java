package sim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The <code>Logger</code> class provides a convenient way of creating and
 * managing a log file.
 * 
 * @author Pawel Kleczek
 * @version 0.1
 * @since 10-10-2012
 * 
 */
public class Logger {

    private BufferedWriter writer;

    /**
     * Name of the log file (may also include a file extension).
     */
    private final String logname;


    /**
     * Creates a new <code>Logger</code> instance and associates it with a newly
     * created file specified by the given filename
     * <p>
     * The file is created in the <i>log</i> directory. Any previous log with
     * the given name is overwritten.
     * 
     * @param filename
     *            Name of the log file.
     */
    public Logger(String filename) {
        logname = filename;
        File logfile = new File("log/" + filename);

        try {
            if (logfile.exists())
                logfile.delete();
            logfile.createNewFile();
            writer = new BufferedWriter(new FileWriter(logfile));
        } catch (IOException e) {
            System.err.println(String.format("Could not open file (%s).", logname));
            writer = null;
        }
    }


    /**
     * Writes a string at a given time.
     * <p>
     * Note that a newline character is added at the end of the string.
     * 
     * @param simTime Exact simulation time in seconds.
     * @param str String to be written.
     */
    public void log(long simTime, String str) {

        if (writer == null)
            return;

        try {
            long mins = simTime / 60;
            long secs = simTime % 60;

            writer.write(String.format("%02d:%02d \t%s\n", mins, secs, str));
            writer.flush();
        } catch (IOException e) {
            System.err.println(String.format("Error while writing to the log (%s).", logname));
        }
    }

    /**
     * Writes a string.
     * <p>
     * @param str String to be written.
     */
    public void log(String str) {
        long simTime = Biathlon.getInstance().presentTime().getTimeTruncated();
        log(simTime, str);
    }


    @Override
    protected void finalize() throws Throwable {
        // Clean-up.
        writer.close();
        super.finalize();
    }

}
