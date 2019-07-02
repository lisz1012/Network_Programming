/**
 * Selector大总管在这里只管着看有哪些客户端连接上来，连接上来的客户端的后续处理，
 * 交给线程池。大总管开门迎客，客人一下涌入之后，有50个服务员为客人们服务.后续
 * 服务大管家就不管了
 */

package com.lisz.nio;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolServer {
	private  ExecutorService pool = Executors.newFixedThreadPool(50);
	private Selector selector;
	
	public static void main(String[] args) throws Exception {
		PoolServer server = new PoolServer();
		server.initServer(8000);
		server.listen();
	}

	private void initServer(int port) throws Exception {
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(port));
		selector = Selector.open(); // 启动大管家
		ssc.register(selector, SelectionKey.OP_ACCEPT); //注册大管家，只关心客户端连接这件事
		System.out.println("Initialized...");
	}
	
	private void listen() throws Exception {
		while (true) {
			selector.select(); //轮询看有没有客户端连接上来
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isAcceptable()) {
					ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
					SocketChannel sc = ssc.accept();
					sc.configureBlocking(false);
					sc.register(selector, SelectionKey.OP_READ);
				} else if (key.isReadable()) {
					key.interestOps(key.interestOps()&(~SelectionKey.OP_READ)); //把OP_READ这件事去除掉，后面不处理这件事了，相当于it.remove();
					pool.execute(new ThreadHandlerChannel(key));
				}
			}
		}
	}
}

class ThreadHandlerChannel extends Thread {
	private SelectionKey key;

	public ThreadHandlerChannel(SelectionKey key) {
		super();
		this.key = key;
	}
	
	@Override
	public void run() {
		SocketChannel sc = (SocketChannel)key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int size = 0;
			while ((size = sc.read(buffer)) > 0) {
				buffer.flip();
				baos.write(buffer.array(), 0, size);
				buffer.clear();
			}
			baos.close();
			
			byte content[] = baos.toByteArray();
			ByteBuffer writeBuf = ByteBuffer.allocate(content.length);
			writeBuf.put(content);
			writeBuf.flip();
			sc.write(writeBuf);
			if (size == -1) {
				sc.close();
			} else {
				key.interestOps(key.interestOps()|SelectionKey.OP_READ);
				key.selector().wakeup();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
}
