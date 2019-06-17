package com.lisz.bio;

import java.net.Socket;

public class Client {

	public static void main(String[] args) throws Exception {
		Socket socket = new Socket("127.0.0.1", 8888);
		socket.getOutputStream().write("Hello".getBytes());
		socket.getOutputStream().flush();
		
		byte buf[] = new byte[1024];
		socket.getInputStream().read(buf);
		System.out.println(new String(buf));
		socket.close();
	}

}
