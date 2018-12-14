package raptor;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import raptor.core.client.NettyTestData;
import raptor.core.client.RpcClientHandlerRegistry;
import raptor.core.client.handler.ClientDispatcherHandler;
import raptor.core.message.RpcRequestBody;

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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		response.getWriter().write("RPC execute start!");
		
		String message = "Netty RPC Send, Netty is VeryGood!";
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setBody(new Object[] { new NettyTestData(), message });
		requestBody.setRpcMethod("LoginAuth");
		requestBody.setMessageId("MessageId-[" + 0 + "]");
		ClientDispatcherHandler channelHandler = (ClientDispatcherHandler) RpcClientHandlerRegistry.INSTANCE.getClientHandler(RpcClientHandlerRegistry.classEnum.ClientDispatcherHandler);
		channelHandler.pushMessage(requestBody);
		
		response.getWriter().write("RPC execute ok!");

	}

}