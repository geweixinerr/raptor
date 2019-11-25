package raptor.exception;

import raptor.core.RpcResult;

/**
 * RPC相关周边异常
 * 
 * @author gewx
 */

public class RpcException extends Exception {

	private static final long serialVersionUID = 5536355594315689736L;

	private RpcResult rpcCode;

	public RpcException() {
		super();
	}

	public RpcException(String s) {
		super(s);
	}

	public RpcException(String s, RpcResult rpcCode) {
		this(s);
		this.rpcCode = rpcCode;
	}

	public RpcResult getRpcCode() {
		return rpcCode;
	}

}
