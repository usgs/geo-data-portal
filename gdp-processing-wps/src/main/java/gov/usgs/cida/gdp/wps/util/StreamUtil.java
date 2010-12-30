package gov.usgs.cida.gdp.wps.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author tkunicki
 */
public class StreamUtil {

	public static File copyInputStreamToTempFile(InputStream inputStream, String suffix) throws IOException {
		File outputFile = File.createTempFile("wps", suffix);
		copyInputStreamToFile(inputStream, outputFile);
		return outputFile;
	}

	public static void copyInputStreamToFile(InputStream inputStream, File outputFile) throws IOException {
		copy(inputStream, new FileOutputStream(outputFile));
	}

	public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
		ReadableByteChannel inputChannel = null;
		WritableByteChannel outputChannel = null;
		try {
			inputChannel = Channels.newChannel(inputStream);
			outputChannel = Channels.newChannel(outputStream);

			ByteBuffer buffer = ByteBuffer.allocateDirect(8 << 10);
			while (inputChannel.read(buffer) > -1) {
				buffer.flip();
				outputChannel.write(buffer);
				buffer.compact();
			}
			buffer.flip();
			while (buffer.hasRemaining()) {
				outputChannel.write(buffer);
			}
		} finally {
			if (inputChannel != null) try { inputChannel.close(); } catch (IOException e) { }
			if (outputChannel != null) try { outputChannel.close(); } catch (IOException e) { }
		}
	}

}
