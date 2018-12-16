package raptor;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import raptor.core.AbstractCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.client.NettyTestData;
import raptor.core.client.RpcClientRegistry;
import raptor.core.message.RpcRequestBody;
import raptor.core.server.RpcResult;

/**
 * Servlet implementation class PushMessageServlet
 */
@WebServlet("/pushMessageServlet")
public class PushMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

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

		// 组装发送消息
		String message = "Netty RPC Send, Netty is VeryGood!";
		NettyTestData data = new NettyTestData();
		
		@SuppressWarnings("rawtypes")
		RaptorRpc rpc = new RaptorRpc();
		// 发送异步消息.
		rpc.sendAsyncMessage("remote", "LoginAuth", new AbstractCallBack() {
			@Override
			public void invoke(RpcResult result) {
				System.out.println("请求结果: " + result);
			}
		}, data, message);
		
		response.getWriter().write("RPC execute ok!");

	}

}
