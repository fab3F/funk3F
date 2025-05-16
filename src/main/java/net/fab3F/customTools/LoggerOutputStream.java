package net.fab3F.customTools;

import java.io.IOException;
import java.io.OutputStream;

public class LoggerOutputStream extends OutputStream {
    private final OutputStream out1;
    private final OutputStream out2;

    public LoggerOutputStream(OutputStream out1, OutputStream out2) {
        this.out1 = out1;
        this.out2 = out2;
    }

    @Override
    public void write(int b) throws IOException {
        out1.write(b);
        out2.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out1.write(b);
        out2.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out1.write(b, off, len);
        out2.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out1.flush();
        out2.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            out1.close();
        } finally {
            out2.close();
        }
    }
}