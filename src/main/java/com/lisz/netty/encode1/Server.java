package com.lisz.netty.encode1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Server {

	public static void main(String[] args) throws Exception {
		EventLoopGroup bossGroup=  new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
		 .channel(NioServerSocketChannel.class)
		 .option(ChannelOption.SO_BACKLOG, 1024)
		 .option(ChannelOption.SO_SNDBUF, 32 * 1024)
		 .option(ChannelOption.SO_RCVBUF, 32 * 1024)
		 .childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				/* 以"$_"为分隔符，把写过来的Buffer中的信息分割为若干段，每一段是一个String，
				 * 分别都要触发 ServerHandler 的channelRead方法处理一次
				 */
				ByteBuf buf = Unpooled.copiedBuffer("$_".getBytes());
				ch.pipeline()
				  .addLast(new DelimiterBasedFrameDecoder(256, buf))
				  .addLast(new StringDecoder())
				  .addLast(new ServerHandler());
			} 
		});
		
		ChannelFuture f = b.bind(8765).sync();
		f.channel().closeFuture().sync();
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

}
