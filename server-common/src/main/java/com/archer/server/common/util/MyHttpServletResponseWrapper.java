package com.archer.server.common.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author Shinobu
 * @since 2018/11/8
 */
public class MyHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream bout = new ByteArrayOutputStream();

    private PrintWriter pw;

    public MyHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new MyServletOutputStream(bout);
    }

    @Override
    public PrintWriter getWriter() {
        pw = new PrintWriter(new OutputStreamWriter(bout, StandardCharsets.UTF_8));
        return pw;
    }

    public byte[] getBuffer() {
        if (pw != null) {
            pw.flush();
        }
        return bout.toByteArray();
    }
}

class MyServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream bout;

    public MyServletOutputStream(ByteArrayOutputStream bout) {
        this.bout = bout;
    }

    @Override
    public void write(int b) {
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        bout.write(bytes);
        bout.flush();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}
