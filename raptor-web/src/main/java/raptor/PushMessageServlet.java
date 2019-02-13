package raptor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;
import raptor.log.RaptorLogger;

@WebServlet("/pushMessageServlet")
public class PushMessageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final String serverNode = "tcs";
	
	private static final RaptorLogger LOGGER = new RaptorLogger(PushMessageServlet.class);
	
	@SuppressWarnings("rawtypes")
	private static RaptorRpc rpc = new RaptorRpc();

	/**
	 * Default constructor.
	 */
	public PushMessageServlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final String methodName = "doPost";
		response.getWriter().write("RPC execute start!");

		LOGGER.enter(methodName, "RPC execute start!", false);
		try {
			RpcResponseBody result = rpc.sendSyncMessage(serverNode, "LoginAuth");
			LOGGER.info(methodName, "RPC Result: " + result);
		} catch (RpcException e) {
			LOGGER.error(methodName, "异常: " + e.getMessage());
		}
		LOGGER.exit(methodName, "RPC execute ok!");
		
		response.getWriter().write("RPC execute ok!");
	}

}
