package de.tomasgng.backupcreators.utils;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class CountingRequestBody extends RequestBody {

    private final RequestBody delegate;
    private final long totalBytes;
    private final Listener listener;

    public CountingRequestBody(RequestBody delegate, Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
        try {
            this.totalBytes = delegate.contentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return totalBytes;
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        CountingSink countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {
        private long bytesWritten = 0;

        public CountingSink(@NotNull Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(@NotNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            listener.onProgressUpdate(bytesWritten, totalBytes);
        }
    }

    public interface Listener {
        void onProgressUpdate(long bytesWritten, long totalBytes);
    }
}
