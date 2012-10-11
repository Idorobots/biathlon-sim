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
    private final String logname;


    /**
     * Creates a new <code>Logger</code> instance and associates it with a newly
     * created file specified by the given filename (the file is created in the
     * <i>log</i> directory).
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
     * Writes a string.<br>
     * Note that a newline character is added at the end of the string.
     * 
     * @param str
     *            String to be written.
     */
    public void log(String str) {

        if (writer == null)
            return;

        try {
            String simTime = Biathlon.getInstance().presentTime().toString();
            writer.write(String.format("[ %8s ]\t%s\n", simTime, str));
            writer.flush();
        } catch (IOException e) {
            System.err.println(String.format("Error while writing to the log (%s).", logname));
        }
    }


    @Override
    protected void finalize() throws Throwable {
        writer.close();
        super.finalize();
    }

}
