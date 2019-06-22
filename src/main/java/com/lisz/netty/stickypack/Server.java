package com.lisz.netty.stickypack;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

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
				 ch.pipeline().addLast(new ServerHanlder());
			}
		});
		ChannelFuture f = b.bind(8765).sync();
		f.channel().closeFuture().sync();
		
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

}
