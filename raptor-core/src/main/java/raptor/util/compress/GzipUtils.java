package raptor.util.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringUtils;

import raptor.exception.CompressException;

/**
 * @author gewx Gzip压缩 (网络传输前压缩字节流) 更多解压缩方式:
 *         https://blog.csdn.net/mazaiting/article/details/79707927
 **/

public final class GzipUtils {

	private static final String GZIP_ENCODE_UTF_8 = "UTF-8";

	/**
	 * @author gewx Gzip压缩
	 * @param bytes 
	 *            压缩的字节数组
	 **/
	public static byte[] compress(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			throw new IllegalArgumentException("缺失必填参数:bytes!");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(1024 + 512); // init
		try {
			GZIPOutputStream gzip = null;
			try {
				gzip = new GZIPOutputStream(out);
				gzip.write(bytes);
			} finally {
				if (gzip != null) {
					gzip.close();
				}
			}
		} catch (Exception e) {
			throw new CompressException("流压缩异常,message: " + e.getMessage());
		}
		return out.toByteArray();
	}
	
	/**
	 * @author gewx Gzip压缩
	 * @param str
	 *            压缩的字符, encoding 字符编码
	 **/
	public static byte[] compress(String str, String encoding) {
		if (StringUtils.isBlank(str) || StringUtils.isBlank(encoding)) {
			throw new IllegalArgumentException("缺失必填参数:str,encoding!");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(1024 + 512); // init
		try {
			GZIPOutputStream gzip = null;
			try {
				gzip = new GZIPOutputStream(out);
				gzip.write(str.getBytes(encoding));
			} finally {
				if (gzip != null) {
					gzip.close();
				}
			}
		} catch (Exception e) {
			throw new CompressException("流压缩异常,message: " + e.getMessage());
		}
		return out.toByteArray();
	}

	public static byte[] compress(String str) throws IOException {
		return compress(str, GZIP_ENCODE_UTF_8);
	}

	/**
	 * @author gewx 解压Gzip字符流
	 * @param bytes
	 *            Gzip字符流
	 **/
	public static byte[] uncompress(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			throw new IllegalArgumentException("缺失必填参数:bytes!");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(1024 + 512); // init
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		try {
			GZIPInputStream ungzip = null;
			try {
				ungzip = new GZIPInputStream(in);
				byte[] buffer = new byte[256];
				int n;
				while ((n = ungzip.read(buffer)) >= 0) {
					out.write(buffer, 0, n);
				}
			} finally {
				if (ungzip != null) {
					ungzip.close();
				}
			}
		} catch (Exception e) {
			throw new CompressException("流解压缩异常,message: " + e.getMessage());
		}
		return out.toByteArray();
	}

	/**
	 * @author gewx 解压Gzip字符流并转化为字符串
	 * @param bytes
	 *            Gzip字符流 ,encoding 字符编码
	 **/
	public static String uncompressToString(byte[] bytes, String encoding) {
		if (bytes == null || bytes.length == 0 || StringUtils.isBlank(encoding)) {
			throw new IllegalArgumentException("缺失必填参数:bytes , encoding!");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(1024 + 512);
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		try {
			GZIPInputStream ungzip = null;
			try {
				ungzip = new GZIPInputStream(in);
				byte[] buffer = new byte[256];
				int n;
				while ((n = ungzip.read(buffer)) >= 0) {
					out.write(buffer, 0, n);
				}
				return out.toString(encoding);
			} finally {
				if (ungzip != null) {
					ungzip.close();
				}
			}
		} catch (Exception e) {
			throw new CompressException("流解压缩异常,message: " + e.getMessage());
		}
	}

	public static String uncompressToString(byte[] bytes) {
		return uncompressToString(bytes, GZIP_ENCODE_UTF_8);
	}

	public static void main(String[] args) throws IOException {
		String str = "中国人民万岁,非常好的的多功能下.中国人民万岁,非常好的的多功能下.中国人民万岁,非常好的的多功能下.";
		int strLength = str.length();
//		byte[] compressByteArray = compress(str); // 压缩字节数组
		byte[] compressByteArray = compress(str.getBytes("UTF-8")); // 压缩字节数组
		System.out.println("字符串长度: " + strLength);
		System.out.println("字符比特数组长度: " + str.getBytes().length);
		System.out.println("压缩后比特数组: " + compressByteArray.length);
		System.out.println("解压后比特数组长度: " + uncompress(compressByteArray).length);

		System.out.println("解压: " + new String(uncompress(compressByteArray), "UTF-8")); // 解压缩输出字符.
	}

}