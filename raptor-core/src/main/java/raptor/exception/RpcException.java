package raptor.exception;

/**
 * RPC相关周边异常
 */

public class RpcException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public RpcException() {
	      super();
    }


    public RpcException(String s) {
	     super(s);
    }
}
