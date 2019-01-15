package de;

import java.io.IOException;
import java.io.PrintStream;

public class TeePrintStream extends PrintStream {
    private final PrintStream second;
    private String last = "";

    public TeePrintStream(PrintStream second) {
        super(second);
        this.second = second;
    }

    /**
     * Closes the main stream. The second stream is just flushed but <b>not</b> closed.
     *
     * @see java.io.PrintStream#close()
     */
    @Override
    public void close() {
        // just for documentation
        super.close();
    }

    @Override
    public void flush() {
        super.flush();
        second.flush();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);

        String intermediate = (new String(buf)).substring(0, len);

        if (!intermediate.contains("\r\n")) {
            last += intermediate;
            launch.that.writeMsg(last);
        } else {
            last += intermediate;
            launch.that.writeMsg(last, false);
            last = "";
        }

    }

    @Override
    public void write(int b) {
        super.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
    }
}
