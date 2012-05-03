package llc.rockford.webcast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

public class StringUtils {

	public static String encodeBase64BinaryFile(InputStream is) {
		String base64EncodedFile = "";
		try {
			byte[] bytesFromFile = IOUtils.toByteArray(is);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream stream = new ObjectOutputStream(baos);
			stream.write(bytesFromFile);
			stream.flush();
			stream.close();
			base64EncodedFile = new String(Base64.encodeBase64(baos.toByteArray()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return base64EncodedFile;
	}


	public static String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
	
	public static byte[] getBytes(InputStream is) throws IOException {

	    int len;
	    int size = 1024;
	    byte[] buf;

	    if (is instanceof ByteArrayInputStream) {
	      size = is.available();
	      buf = new byte[size];
	      len = is.read(buf, 0, size);
	    } else {
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      buf = new byte[size];
	      while ((len = is.read(buf, 0, size)) != -1)
	        bos.write(buf, 0, len);
	      buf = bos.toByteArray();
	    }
	    return buf;
	  }

}
