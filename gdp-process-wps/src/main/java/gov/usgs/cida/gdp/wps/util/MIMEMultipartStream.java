package gov.usgs.cida.gdp.wps.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.fileupload.FileItemStream;

import org.apache.commons.fileupload.util.Closeable;

/**
 * <p> Low level API for processing mime encoded streams.
 *
 * <p> This class can be used to process data streams conforming to MIME
 * 'multipart' format as defined in
 * <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>. Arbitrarily
 * large amounts of data in the stream can be processed under constant
 * memory usage.
 *
 * <p> The format of the stream is defined in the following way:<br>
 *
 * <code>
 *   multipart-body := preamble 1*encapsulation close-delimiter epilogue<br>
 *   encapsulation := delimiter body CRLF<br>
 *   delimiter := "--" boundary CRLF<br>
 *   close-delimiter := "--" boudary "--"<br>
 *   preamble := &lt;ignore&gt;<br>
 *   epilogue := &lt;ignore&gt;<br>
 *   body := header-part CRLF body-part<br>
 *   header-part := 1*header CRLF<br>
 *   header := header-name ":" header-value<br>
 *   header-name := &lt;printable ascii characters except ":"&gt;<br>
 *   header-value := &lt;any ascii characters except CR & LF&gt;<br>
 *   body-data := &lt;arbitrary data&gt;<br>
 * </code>
 *
 * <p>Note that body-data can contain another mulipart entity.  There
 * is limited support for single pass processing of such nested
 * streams.  The nested stream is <strong>required</strong> to have a
 * boundary token of the same length as the parent stream (see {@link
 * #setBoundary(byte[])}).
 *
 * <p>Here is an example of usage of this class.<br>
 *
 * <pre>
 *    try {
 *        MIMEMultipartStream multipartStream = new MIMEMultipartStream(input,
 *                                                              boundary);
 *        boolean nextPart = multipartStream.skipPreamble();
 *        OutputStream output;
 *        while(nextPart) {
 *            header = chunks.readHeader();
 *            // process headers
 *            // create some output stream
 *            multipartStream.readBodyPart(output);
 *            nextPart = multipartStream.readBoundary();
 *        }
 *    } catch(MIMEMultipartStream.MalformedStreamException e) {
 *          // the stream failed to follow required syntax
 *    } catch(IOException) {
 *          // a readByte or write error occurred
 *    }
 *
 * </pre>
 *
 */
public final class MIMEMultipartStream {

	/**
     * The maximum length of a boundary, not including trailing/leading CRLF
	 * or trailing/leading '--' characters
     */
	public final static int MAX_BOUNDARY_LENGTH = 70;

	/**
     * The Carriage Return ASCII character value.
     */
    public static final byte CR = 0x0D;

    /**
     * The Line Feed ASCII character value.
     */
    public static final byte LF = 0x0A;

    /**
     * The dash (-) ASCII character value.
     */
    public static final byte DASH = 0x2D;

	/**
     * byte array of two <code>DASH</code> characters for implementation convenience.
     */
	public static final byte[] DASH_DASH = new byte[] { DASH, DASH };

    /**
     * The default length of the buffer used for processing a request.
     */
    public static final int DEFAULT_BUFFERSIZE = 4096;

	/**
     * The ReadableByteChannel wrapping the InputStream from which data is read.
     */
	private final ReadableByteChannel channel;

	/**
     * The buffer used for processing the request.
     */
	private ByteBuffer channelBuffer;

	/**
     * The byte sequence that partitions the stream.
     */
	private byte[] boundary;

	/**
     * The byte sequence that partitions the stream with leading double dash.
     */
	private byte[] ddboundary;


    private String headerEncoding;

	/**
     * <p> Constructs a <code>MIMEMultipartStream</code> with a custom buffer size
     *
     * <p> Note that the buffer must be at least big enough to contain the
     * boundary string, plus 4 characters for CR/LF and double dash, plus at
     * least one byte of data.  Too small a buffer size setting will degrade
     * performance.
     *
     * @param input    The <code>InputStream</code> to serve as a data source.
     * @param boundary The token used for dividing the stream into
     *                 <code>encapsulations</code>.
     * @param bufSize  The size of the buffer to be used, in bytes.
     *
     */
    MIMEMultipartStream(InputStream inputStream, byte[] boundary, int bufferSize) {

		this.channel = Channels.newChannel(inputStream);

        this.channelBuffer = ByteBuffer.allocate(bufferSize);
		this.channelBuffer.position(bufferSize); // mark buffer as empty

		setBoundary(boundary);
    }

	/**
     * <p> Constructs a <code>MIMEMultipartStream</code> with default buffer size
     *
     * @param input    The <code>InputStream</code> to serve as a data source.
     * @param boundary The token used for dividing the stream into
     *                 <code>encapsulations</code>.
     *
     */
    public MIMEMultipartStream(InputStream input, byte[] boundary) {
        this(input, boundary, DEFAULT_BUFFERSIZE);
    }

    /**
     * Retrieves the character encoding used when reading the headers of an
     * individual part. When not specified, or <code>null</code>, the platform
     * default encoding is used.

     *
     * @return The encoding used to readByte part headers.
     */
    public String getHeaderEncoding() {
        return headerEncoding;
    }

	/**
     * Specifies the character encoding to be used when reading the headers of
     * individual parts. When not specified, or <code>null</code>, the platform
     * default encoding is used.
     *
     * @param encoding The encoding used to readByte part headers.
     */
    public void setHeaderEncoding(String encoding) {
        headerEncoding = encoding;
    }

	/**
     * Reads a byte from the <code>buffer</code>, and refills it as
     * necessary.
     *
     * @return The next byte from the input stream.
     *
     * @throws IOException if there is no more data available.
     */
    private byte readByte() throws IOException {
        if (channelBuffer.remaining() == 0) {
			fill();
        }
        return channelBuffer.get();
    }

	/**
     * Fills <code>channelBuffer</code> using <code>channel</code>
     *
     * @throws IOException if there is no more data available.
     */
	private void fill() throws IOException {
        channelBuffer.compact();
		int read = channel.read(channelBuffer);
		if (read < 0) {
			// No more data available.
			throw new IOException("No more data is available");
		} else {
			channelBuffer.flip();
		}
    }

    public boolean readBoundary() throws MalformedStreamException {
        try {
			byte b = readByte();
			if (b == CR) { b = readByte(); }
			if (b == LF) { b = readByte(); }
			if (b == DASH && readByte() == DASH ) {
				boolean match = true;
				ByteBuffer boundaryBuffer = ByteBuffer.wrap(boundary);
				while (boundaryBuffer.hasRemaining() && match) {
					match = readByte() == boundaryBuffer.get();
				}
				if (match) {
					b = readByte();
					if (b == DASH && readByte() == DASH) {
						// boundary was valid but no more content
						return false;
					} else {
						if (b == CR) { b = readByte(); }
						if (b == LF) {
							// boundary was valid and more content
							return true;
						}
					}
				}
			}
            throw new MalformedStreamException("Unexpected characters while parsing boundary");
        } catch (IOException e) {
            throw new MalformedStreamException("Stream ended unexpectedly");
        }
    }

	public void setBoundary(byte[] boundary) {
		if (boundary == null) {
			throw new IllegalArgumentException("boundary may not be null");
		}
		// need enough storage to buffer leading '\r\n' and '--' plus one
		// extra trailing byte to determine stream state.
		if (boundary.length + 5 > channelBuffer.capacity()) {
			throw new IllegalArgumentException("boundary too small for current buffer size");
		}
        this.boundary = boundary;
		this.ddboundary = new byte[boundary.length + 2];
		System.arraycopy(DASH_DASH, 0, ddboundary, 0, 2);
		System.arraycopy(boundary, 0, ddboundary, 2, boundary.length);
    }

	private final static Pattern PATTERN_HEADER_ENTRY = Pattern.compile("(\\S+)\\s*:\\s*(.+)");
    public Map<String,String> readHeaders() throws MalformedStreamException {
		Map<String, String> headerMap = new LinkedHashMap<String, String>();
		byte b = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int newLineCount = 0;
			while (newLineCount < 2) {
				b = readByte();
				if (b == CR) {
					b = readByte();
				}
				if (b == LF) {
					if (newLineCount == 0 && baos.size() > 0) {
						String headerLine = null;
						if (headerEncoding != null) {
							try {
								headerLine = baos.toString(headerEncoding);
							} catch (UnsupportedEncodingException e) {
								// Fall back to platform default if specified encoding is not
								// supported.
								headerLine = baos.toString();
							}
						} else {
							headerLine = baos.toString();
						}
						baos.reset();

						Matcher headerEntryMatcher = PATTERN_HEADER_ENTRY.matcher(headerLine);
						if (headerEntryMatcher.find()) {
							headerMap.put(headerEntryMatcher.group(1), headerEntryMatcher.group(2));
						}
					}
					newLineCount++;
				} else {
					newLineCount = 0;
				}
				baos.write(b);
			}
		} catch (IOException e) {
			throw new MalformedStreamException("Stream ended unexpectedly");
		}
		return headerMap;
    }

    public void readBodyData(OutputStream outputStream) throws IOException {
		StreamUtil.copy(new ItemInputStream(), outputStream);
    }

    public void discardBodyData() throws MalformedStreamException, IOException {
		InputStream inputStream = new ItemInputStream();
		int available = 0;
		while ((available = inputStream.available()) > 0) {
			inputStream.skip(available);
		}
		inputStream.close();
    }

    public boolean skipPreamble() throws IOException {
        try {
			fill();
			boolean match = true;
			ByteBuffer boundaryBuffer = ByteBuffer.wrap(ddboundary);
			// duplicate as we don't want to consume
			ByteBuffer preambleBuffer = channelBuffer.duplicate();
			while (preambleBuffer.hasRemaining() && boundaryBuffer.hasRemaining() && match) {
				match = boundaryBuffer.get() == preambleBuffer.get();
			}
			if (!match) {
				// Discard all data up to the delimiter.
				discardBodyData();
			}
            // Read boundary - if succeded, the stream contains an encapsulation.
            return readBoundary();
        } catch (MalformedStreamException e) {
            return false;
		}
    }

    /**
     * Thrown to indicate that the input stream fails to follow the
     * required syntax.
     */
    public static class MalformedStreamException
    extends IOException {
        /**
         * Constructs a <code>MalformedStreamException</code> with no
         * detail message.
         */
        public MalformedStreamException() {
            super();
        }

        /**
         * Constructs an <code>MalformedStreamException</code> with
         * the specified detail message.
         *
         * @param message The detail message.
         */
        public MalformedStreamException(String message) {
            super(message);
        }
    }

    /**
     * An {@link InputStream} for reading an items contents.
     */
    public class ItemInputStream extends InputStream implements Closeable {

        private boolean closed;
		private boolean found;
		private ByteBuffer streamBuffer;

		public int makeAvailable() throws IOException {

			if (found) { return 0; }

			fill();

			streamBuffer = channelBuffer.duplicate();
			boolean marked = false;
			while (streamBuffer.hasRemaining() && !found) {
				byte b = streamBuffer.get();
				if (b == CR) {
					streamBuffer.mark();
					if (streamBuffer.hasRemaining()) {
						b = streamBuffer.get();
						if (b == LF) {
							marked = boundaryCheck();
						}
					} else {
						// CR was last char in buffer, as it may be an
						marked = true;
					}
				} else if (b == LF) {
					streamBuffer.mark();
					marked = boundaryCheck();
				}
			}
			if (marked) { streamBuffer.reset(); }

			channelBuffer.position(streamBuffer.position());
			
			streamBuffer.flip();

			return streamBuffer.remaining();
		}

		private boolean boundaryCheck() {
			boolean match = true;
			ByteBuffer boundaryBuffer = ByteBuffer.wrap(ddboundary);
			while (streamBuffer.hasRemaining() && boundaryBuffer.hasRemaining() && match) {
				match = boundaryBuffer.get() == streamBuffer.get();
			}
			if (match && !boundaryBuffer.hasRemaining()) {
				found = true;
			}
			return match;
		}

		@Override
        public int available() throws IOException {
			if (streamBuffer == null || !streamBuffer.hasRemaining()) {
				makeAvailable();
			}
			return streamBuffer.remaining();
        }

		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public synchronized void mark(int i) {
			throw new UnsupportedOperationException();
		}

		@Override
		public synchronized void reset() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
        public int read() throws IOException {
            if (closed) {
                throw new FileItemStream.ItemSkippedException();
            }
            if (available() == 0) {
                if (makeAvailable() == 0) {
                    return -1;
                }
            }
            return streamBuffer.get() & 0xff;
        }

		@Override
		public int read(byte[] bytes) throws IOException {
			return read(bytes, 0, bytes.length);
		}

		@Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (closed) {
                throw new FileItemStream.ItemSkippedException();
            }
            if (length == 0) {
                return 0;
            }
            int available = available();
            if (available == 0) {
                available = makeAvailable();
                if (available == 0) {
                    return -1;
                }
            }
            int read = Math.min(available, length);
            streamBuffer.get(bytes, offset, read);
            return read;
        }

		@Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            for (;;) {
                int available = available();
                if (available == 0) {
                    available = makeAvailable();
                    if (available == 0) {
                        break;
                    }
                }
                skip(available);
            }
            closed = true;
        }

		@Override
        public long skip(long bytes) throws IOException {
            if (closed) {
                throw new FileItemStream.ItemSkippedException();
            }
            int available = available();
            if (available == 0) {
                available = makeAvailable();
                if (available == 0) {
                    return 0;
                }
            }
            long result = Math.min(available, bytes);
			streamBuffer.position(streamBuffer.limit());
            return result;
        }

		@Override
        public boolean isClosed() {
            return closed;
        }
    }
}
