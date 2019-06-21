package com.lisz.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

public class HelloNetty {

	public static void main(String[] args) {
		new NettyServer(8888).serverStart();
	}

}

class NettyServer {
	private int port;
	
	public NettyServer(int port) {
		this.port = port;
	}

	public void serverStart() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup worderGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, worderGroup)							//第一个EventLoopGroup负责连接，第二个负责连接后的IO处理
		 .channel(NioServerSocketChannel.class)					//建立完连接后的通道的类型
		 .childHandler(new ChannelInitializer<SocketChannel>() {//每一个client连上来之后给他一个监听器，让他进行处理
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {//处理的过程是在通道上加一个处理器，又是一个监听器
				ch.pipeline().addLast(new MyHandler());
			}
		});
		try {
			// sync() Waits for this future until it is done, and rethrows the cause of the failure if this future failed.
			ChannelFuture f = b.bind(port).sync();
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}

class MyHandler extends ChannelInboundHandlerAdapter {
	@Override									// Object，Netty甚至可以写出去一个序列化的对象
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("Server channel read");
		ByteBuf buf = (ByteBuf) msg;
		System.out.println(buf.toString(CharsetUtil.UTF_8));
		ctx.writeAndFlush(msg);
		ctx.close();
	}
	
	// Netty把所有的异常进行了封装然后都在这里处理
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}