package gov.usgs.cida.gdp.wps.parser;

import gov.usgs.cida.gdp.wps.binding.GeoTIFFFileBinding;
import gov.usgs.cida.gdp.wps.util.MIMEMultipartStream;
import gov.usgs.cida.gdp.wps.util.StreamUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Map;
import org.n52.wps.io.datahandler.binary.AbstractBinaryParser;

public class GeoTIFFFileParser extends AbstractBinaryParser {

	public final static short TIFF_MAGIC_LE = 0x4949; // "II"
	public final static short TIFF_MAGIC_BE = 0x4D4D; // "MM"
	public final static short TIFF_MAGIC_42 = 42;
	public final static short MIME_MAGIC = 0x2D2D;	  // "--"

	@Override
	public Class[] getSupportedInternalOutputDataType() {
		return new Class[]{GeoTIFFFileBinding.class};
	}

	@Override
	public GeoTIFFFileBinding parse(InputStream input, String mimeType) {
		File tempFile = null;
		FileChannel tempChannel = null;

		try {
			tempFile = StreamUtil.copyInputStreamToTempFile(input, ".tmp");

			ByteBuffer buffer = ByteBuffer.allocate(4 + MIMEMultipartStream.MAX_BOUNDARY_LENGTH);
			tempChannel = new FileInputStream(tempFile).getChannel();
			tempChannel.read(buffer);
			tempChannel.close();
			tempChannel = null;

			short magic01 = buffer.getShort();
			if (magic01 == TIFF_MAGIC_LE || magic01 == TIFF_MAGIC_BE) {
				short magic23 = magic01 == TIFF_MAGIC_LE
						? buffer.order(ByteOrder.LITTLE_ENDIAN).getShort()
						: buffer.getShort();
				if (magic23 == TIFF_MAGIC_42) {
					String tiffFileName = tempFile.getAbsolutePath().concat(".tiff");
					File tiffFile = new File(tempFile.getAbsolutePath());
					boolean status = tiffFile.renameTo(new File(tiffFileName));
					if (status) {
						return new GeoTIFFFileBinding(tiffFile);
					} else {
						throw new RuntimeException("Error generating temporary tiff file.");
					}
				} else {
					throw new RuntimeException("Unexpected value parsing tiff file.");
				}
			} else if (magic01 == MIME_MAGIC) {
				byte[] boundary = extractBoundaryFromBuffer(buffer);
				if (boundary != null) {
					File tiffFile = extractFromMIMEMultipartStream(tempFile, boundary);
					if (tiffFile != null) {
						return new GeoTIFFFileBinding(tiffFile);
					} else {
						throw new RuntimeException("unable to extract tiff file from mime-multipart stream");
					}
				} else {
					throw new RuntimeException("unable to extract boundary from mime-multipart stream");
				}
			} else {
				throw new RuntimeException("unknown content");
			}

		} catch (IOException e) {
			throw new RuntimeException("Error extracting GeoTIFF", e);
		} finally {
			if (tempChannel != null) {
				try { tempChannel.close(); } catch (IOException ex) { }
			}
			if (tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
		}

	}

	private byte[] extractBoundaryFromBuffer(ByteBuffer buffer) {
		ByteArrayOutputStream boundaryOutputStream = new ByteArrayOutputStream();
		boolean foundCRLF = false;
		while (buffer.hasRemaining() && !foundCRLF) {
			byte b = buffer.get();
			if (b == MIMEMultipartStream.CR) {
				b = buffer.get();
			}
			if (b == MIMEMultipartStream.LF) {
				foundCRLF = true;
			} else {
				boundaryOutputStream.write(b);
			}
		}
		return foundCRLF
				? boundaryOutputStream.toByteArray()
				: null;
	}

	private File extractFromMIMEMultipartStream(File tempFile, byte[] boundary) throws IOException {
		File extractedFile = null;
		FileInputStream tempFileInputStream = null;
		try {
			MIMEMultipartStream mimeMultipartStream = new MIMEMultipartStream(
					new FileInputStream(tempFile), boundary);
			mimeMultipartStream.skipPreamble();
			boolean hasNext = true;
			while (hasNext && extractedFile == null) {
				Map<String, String> headerMap = mimeMultipartStream.readHeaders();
				String contentType = headerMap.get("Content-Type");
				if ("image/tiff".equals(contentType) || "image/geotiff".equals(contentType)) {
					String extractedFileName = tempFile.getAbsolutePath().concat(".tiff");
					extractedFile = new File(extractedFileName);
					mimeMultipartStream.readBodyData(new FileOutputStream(extractedFile));
				} else {
					mimeMultipartStream.discardBodyData();
				}
				hasNext = mimeMultipartStream.readBoundary();
			}
		} finally {
			if (tempFileInputStream != null) {
				tempFileInputStream.close();
			}
		}
		return extractedFile;
	}
}
