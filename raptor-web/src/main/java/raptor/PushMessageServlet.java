package raptor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;

@WebServlet("/pushMessageServlet")
public class PushMessageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final String serverNode = "tcs";
	
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

		response.getWriter().write("RPC execute start!");

		try {
			RpcResponseBody result = rpc.sendSyncMessage(serverNode, "LoginAuth");
			System.out.println("RPC Result: " + result);
		} catch (RpcException e) {
			System.out.println("异常: " + e.getMessage());
		}
		
		response.getWriter().write("RPC execute ok!");
	}

}
