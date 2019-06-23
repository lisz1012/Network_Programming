package com.lisz.netty.encode2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Server {

	public static void main(String[] args) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
		 .channel(NioServerSocketChannel.class)
		 .option(ChannelOption.SO_BACKLOG, 1024)
		 .option(ChannelOption.SO_SNDBUF, 32 * 1024)
		 .option(ChannelOption.SO_RCVBUF, 32 * 1024)
		 .option(ChannelOption.SO_KEEPALIVE, true)
		 .childHandler(new ChannelInitializer<SocketChannel>() {
			 @Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline()
				  .addLast(new FixedLengthFrameDecoder(5))
				  .addLast(new StringDecoder())
				  .addLast(new ServerHandler());
			}
		});
		ChannelFuture f = b.bind(8765).sync();
		f.channel().closeFuture().sync();
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}
	/*
	 frameLength = 5，多余的不触发Handler的channelRead，直到5个长度已满：
	 Received from client: 11111
	 Received from client: 22233
	 Received from client: 33344
	 */
}
