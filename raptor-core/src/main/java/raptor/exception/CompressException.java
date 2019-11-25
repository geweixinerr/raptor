package raptor.exception;

/**
 * 压缩流相关周边异常
 * 
 * @author gewx
 */

public class CompressException extends RuntimeException {

	private static final long serialVersionUID = 7642894841698124294L;

	public CompressException() {
		super();
	}

	public CompressException(String s) {
		super(s);
	}
}
