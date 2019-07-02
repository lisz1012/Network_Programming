package com.lisz.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/* Blocking IO 必须有大量的线程在空等，消耗CPU资源，一般网络程序都不用。
 * 但是，如果知道连接server的人特别少，这么写简单处理，特别方便，不易出错。
 */
public class Server {
	public static void main(String[] args) throws Exception {
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress("127.0.0.1", 8888));
		while (true) {
			// 阻塞！停在此处不往下执行，直到有一个客户端连接上来，傻傻的等，BIO
			Socket socket = serverSocket.accept();
			// 每一个连接起一个新的线程，防止前面的数据传输耽误后面的连接被accept
			new Thread(()->{
				handle(socket);
			}).start();
		}
	}

	private static void handle(Socket socket) {
		try {
			byte buf[] = new byte[1024];
			InputStream is = socket.getInputStream();
			// Client如果只是连上来但是没有数据写进来，阻塞！什么时候有数据了，CPU唤醒，才能往下执行
			int len = is.read(buf);//阻塞
			System.out.println(new String(buf, 0, len));
			
			// Client如果不接收，也会停在此处，阻塞！
			socket.getOutputStream().write(buf, 0, len);//阻塞
			socket.getOutputStream().flush();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
