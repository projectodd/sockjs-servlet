/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 */

package org.projectodd.sockjs;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public abstract class SockJsResponse {

    public SockJsResponse() {
        pendingWrites = new ArrayList<>();
    }

    public abstract String getHeader(String name);

    public abstract void setHeader(String name, String value);

    public abstract void writeHead(int statusCode) throws SockJsException;

    protected abstract void write(byte[] bytes) throws Exception;

    protected abstract void flush() throws Exception;

    protected abstract void endResponse() throws SockJsException;

    public Integer cacheFor() {
        return cacheFor;
    }

    public void cacheFor(Integer cacheFor) {
        this.cacheFor = cacheFor;
    }

    public int statusCode() {
        return statusCode;
    }

    public void statusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public synchronized void write(String content) throws SockJsException {
        System.err.println("!!! WRITING " + content);

        byte[] bytes = content.getBytes(UTF8);
//        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(bytes.length);
//        byteStream.write(bytes);
//        pendingWrites.add(byteStream);
//
//        System.err.println("!!! ADDED NEW PENDING WRITTE, SIZE IS NOW " + pendingWrites.size());
        try {
            write(bytes);
            flush();
        } catch (Exception ex) {
            throw new SockJsException("Error writing response:", ex);
        }
    }

    public void end() throws SockJsException {
        end(null);
    }

    public synchronized void end(String content) throws SockJsException {
        System.err.println("!!! ENDING WITH " + content);
        if (content != null) {
            write(content);
        }
        endResponse();
        finished = true;
    }

    public boolean finished() {
        return finished;
    }

    final List<ByteArrayOutputStream> pendingWrites;
    private Integer cacheFor;
    private int statusCode = 200;
    private boolean finished = false;

    private static final Charset UTF8 = Charset.forName("UTF-8");
}
