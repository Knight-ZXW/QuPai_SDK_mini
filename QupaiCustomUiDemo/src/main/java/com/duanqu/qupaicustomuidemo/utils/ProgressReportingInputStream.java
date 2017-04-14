package com.duanqu.qupaicustomuidemo.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressReportingInputStream extends FilterInputStream {

    public interface OnProgressListener {
        void onProgress(long byte_count);
    }

    public ProgressReportingInputStream(InputStream in, OnProgressListener listener) {
        super(in);
        _Listener = listener;
    }

    private final OnProgressListener _Listener;

    private long _ByteCount;

    public long getProgress() { return _ByteCount; }

    @Override
    public int read() throws IOException {
        int val = super.read();
        if (val >= 0) {
            _ByteCount ++;
            _Listener.onProgress(_ByteCount);
        }
        return val;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int count = super.read(b);
        if (count >= 0) {
            _ByteCount += count;
            _Listener.onProgress(_ByteCount);
        }
        return count;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = super.read(b, off, len);
        if (count >= 0) {
            _ByteCount += count;
            _Listener.onProgress(_ByteCount);
        }
        return count;
    }


    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() { return false; }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

}
