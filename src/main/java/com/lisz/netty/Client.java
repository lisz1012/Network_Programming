package com.lisz.netty;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class Client {

	public static void main(String[] args) {
		new Client().clientStart();
	}

	private void clientStart() {
		EventLoopGroup workers = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(workers)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				System.out.println("Channel initialized");
				ch.pipeline().addLast(new ClientHandler());
			}
		 });
		System.out.println("Start to connect...");
		try {
			ChannelFuture f = b.connect(new InetSocketAddress("127.0.0.1", 8888)).sync();
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			workers.shutdownGracefully();
		}
	}

}

class ClientHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Channel is activated");
		// A Future represents the result of an asynchronous computation.
		/*
		 * Creates a new big-endian buffer whose content is a copy of 
		 * the specified array. The new buffer's readerIndex and 
		 * writerIndex are 0 and array.length respectively.
		 */
		final ChannelFuture f = ctx.writeAndFlush(Unpooled.copiedBuffer("Hello Netty!".getBytes()));
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				System.out.println("Msg sent!");
			}
		});
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			ByteBuf buf = (ByteBuf)msg;
			System.out.println(buf.toString(CharsetUtil.UTF_8));
		} finally {
			ReferenceCountUtil.release(msg);//读操作需要手动release释放ByteBuf对象，写操作不需要，里面帮忙实现了. Netty这里直接用了系统内存，所以绕开了JVM，所以要手动释放
		}
	}
}