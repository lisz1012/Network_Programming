package com.lisz.netty.stickypack;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {

	public static void main(String[] args) throws Exception {
		EventLoopGroup workers = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(workers)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
			 @Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ClientHandler());
			}
		 });
		ChannelFuture f = b.connect("127.0.0.1", 8765).sync();
		/* 注意⚠️这里会出现粘包现象，在server端输出：111222333444555，5个线程一起往buffer里面写，最后被第一个执行的flush一起发送
		 * 所以说异步（另起一个线程读写），才是粘包现象的根本原因！可以采取分包拆包的策略，或者间隔执行一下Thread.sleep(XXX);
		 * 除了FixedLengthFrameDecoder和DelimiterBasedFrameDecoder之外还可以靠自定义协议，定义好消息总长度，消息头长度等解决
		 */
		f.channel().writeAndFlush(Unpooled.copiedBuffer("111".getBytes()));
		f.channel().writeAndFlush(Unpooled.copiedBuffer("222".getBytes()));
		f.channel().writeAndFlush(Unpooled.copiedBuffer("333".getBytes()));
		f.channel().writeAndFlush(Unpooled.copiedBuffer("444".getBytes()));
		f.channel().writeAndFlush(Unpooled.copiedBuffer("555".getBytes()));
		
		
		f.channel().closeFuture().sync();
		workers.shutdownGracefully();
	}

}
