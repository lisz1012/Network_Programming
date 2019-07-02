/**
 * 比较BIO的好处：BIO的问题出在每个客户一个线程，客户端多了消耗就比较大的资源
 * 而在NIO这种情况下，一个线程就可以；在NIO里面可以写成阻塞也可以写成非阻塞（write的时候）
 * 有一个选择权。但是NIO的ByteBuffer是臭名昭著的难用！Netty自己又写了个ByteBuf，
 * 区别是前者只有一个指针，读的时候，写的时候，读写的时候都用它，麻烦且容易出错；而后者
 * 有两个指针，分别负责读和写。这一点，面试经常会问！
 */

package com.lisz.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

	public static void main(String[] args) throws Exception {
		ServerSocketChannel ssc = ServerSocketChannel.open();//双全工，开门迎客
		ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8888));
		ssc.configureBlocking(false); //关键！非阻塞
		
		System.out.println("Server started listening on: " + ssc.getLocalAddress());
		Selector selector = Selector.open(); // 轮询，看哪里来连接了。一个大管家可以管好多客户端
		ssc.register(selector, SelectionKey.OP_ACCEPT); // 注册要观察的事件,有客户端申请要连上来的时候会触发事件
		
		while (true) { // 大管家selector对有客户端连接事件的处理方式
			selector.select(); // 阻塞方法，虽然是NIO，监听的channel上任何一个channel上有它关心的事件发生的时候就会返回，就把拿到的事件装在selectionKey的set里
			Set<SelectionKey> keys = selector.selectedKeys(); // 把selectionKeys要挨个拿出来处理，各种的事件都会被封装成SelectionKey来被遍历。上一次循环中handle的期间可能又发生一些事件
			Iterator<SelectionKey> it = keys.iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove(); // 先拿掉，不然多线程轮询的话会出问题。拿掉是为了不要下一次在处理一次连接或者read事件
				handle(key);
			}
		}
	}

	private static void handle(SelectionKey key) {
		if (key.isAcceptable()) {
			try {
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept(); // 是acceptable就把客人接到屋里来，这会新生成一个channel。对应BIO里的Socket
				sc.configureBlocking(false);
				sc.register(key.selector(), SelectionKey.OP_READ); // 在新生成的channel上，让大管家监听读取事件，然后每次轮询selector不只监听accept，也监听这里的read
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
		} else if (key.isReadable()) {
			try (SocketChannel sc = (SocketChannel) key.channel()) {
				ByteBuffer buffer = ByteBuffer.allocate(512);
				buffer.clear();
				int len = sc.read(buffer);
				if (len != -1) {
					System.out.println(new String(buffer.array(), 0, len));
				}
				ByteBuffer bufferToWrite = ByteBuffer.wrap("Hello client!".getBytes());
				sc.write(bufferToWrite); // 阻塞，但是可以写成非阻塞的
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
