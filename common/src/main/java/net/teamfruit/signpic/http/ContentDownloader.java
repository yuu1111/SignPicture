package net.teamfruit.signpic.http;

import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.config.SignPicConfig;
import net.teamfruit.signpic.state.Progress;
import net.teamfruit.signpic.state.State;
import net.teamfruit.signpic.state.StateType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Downloads content from a URL to a local file.
 */
public class ContentDownloader implements Communicator.CommunicateTask {
    private static final int BUFFER_SIZE = 8192;

    private final String url;
    private final Path destination;
    private final State state;
    private volatile boolean cancelled = false;

    public ContentDownloader(String url, Path destination, State state) {
        this.url = url;
        this.destination = destination;
        this.state = state;
    }

    @Override
    public void execute() throws Exception {
        SignPicture.LOGGER.debug("Downloading: {}", url);
        state.setType(StateType.DOWNLOADING);
        state.setProgress(new Progress());

        HttpURLConnection connection = null;
        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setConnectTimeout(SignPicConfig.get().httpTimeout);
            connection.setReadTimeout(SignPicConfig.get().httpTimeout);
            connection.setRequestProperty("User-Agent", "SignPicture/3.0");
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error: " + responseCode);
            }

            long contentLength = connection.getContentLengthLong();
            int maxBytes = SignPicConfig.get().contentMaxBytes;
            if (maxBytes > 0 && contentLength > maxBytes) {
                throw new ContentTooLargeException(contentLength, maxBytes);
            }

            state.getProgress().overall = contentLength;

            // Create parent directories
            Files.createDirectories(destination.getParent());

            // Download to temp file first
            Path tempFile = destination.resolveSibling(destination.getFileName() + ".tmp");

            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(tempFile))) {

                byte[] buffer = new byte[BUFFER_SIZE];
                long totalRead = 0;
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    if (cancelled) {
                        throw new DownloadCancelledException();
                    }

                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    state.getProgress().done = totalRead;

                    // Check max size during download
                    if (maxBytes > 0 && totalRead > maxBytes) {
                        throw new ContentTooLargeException(totalRead, maxBytes);
                    }
                }
            }

            // Move temp file to final destination
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
            SignPicture.LOGGER.debug("Download complete: {}", destination);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public void onError(Exception e) {
        state.setError(e);
        SignPicture.LOGGER.error("Download failed: {}", url, e);
    }

    public void cancel() {
        this.cancelled = true;
    }

    public static class ContentTooLargeException extends IOException {
        public ContentTooLargeException(long actual, long max) {
            super("Content too large: " + actual + " bytes (max: " + max + ")");
        }
    }

    public static class DownloadCancelledException extends IOException {
        public DownloadCancelledException() {
            super("Download was cancelled");
        }
    }
}
