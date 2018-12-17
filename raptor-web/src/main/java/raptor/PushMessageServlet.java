package raptor;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import raptor.core.AbstractCallBack;
import raptor.core.client.NettyTestData;
import raptor.core.message.RpcResponseBody;
import raptor.core.server.RpcResult;

/**
 * Servlet implementation class PushMessageServlet
 */
@WebServlet("/pushMessageServlet")
public class PushMessageServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

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
		
		Executor execute = Executors.newFixedThreadPool(CPU_CORE * 2);
		CyclicBarrier latch = new CyclicBarrier(CPU_CORE * 2);

		@SuppressWarnings("rawtypes")
		RaptorRpc rpc = new RaptorRpc();

		for (int i = 0; i < CPU_CORE * 2; i++) {
			execute.execute(new Runnable() {
				@Override
				public void run() {
					try {
						latch.await();
						// 发送异步消息.
						for (int j = 0; j< 10000; j++) {
							long start = System.currentTimeMillis();
							rpc.sendAsyncMessage("remote", "LoginAuth", new AbstractCallBack() {
								@Override
								public void invoke(RpcResult result) {
									long end = System.currentTimeMillis();
								    RpcResponseBody responseBody =  (RpcResponseBody) result.getMessageBody();
									System.out.println("请求结果: " + result.getSuccess() + ", Message: " + responseBody.getMessage() + ", Result: "+ responseBody.getBody() +", RPC服务耗时: " + (end - start));
								}
							}, 5, data, message);
						}
					} catch (InterruptedException | BrokenBarrierException e) {
						
					}
				}
			});
		}
		
		response.getWriter().write("RPC execute ok!");

	}

}
