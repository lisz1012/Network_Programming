package com.lisz.netty.encode2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Client {

	public static void main(String[] args) throws Exception {
		EventLoopGroup workers = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(workers)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
			 @Override
			protected void initChannel(SocketChannel ch) throws Exception {
				 ByteBuf buf = Unpooled.copiedBuffer("$_".getBytes());
				ch.pipeline()
				 .addLast(new DelimiterBasedFrameDecoder(256, buf))
				 .addLast(new StringDecoder())
				 .addLast(new ClientHandler());
			}
		});
		ChannelFuture f = b.connect("127.0.0.1", 8765).sync();
		f.channel().closeFuture().sync();
		workers.shutdownGracefully();
	}
	/*
	 分隔符$_分开之后：
	 Response from server: 111111
	 Response from server: 22222
	 Response from server: 33333
	 Response from server: 4
	 Response from server: 555
	 Server disconnected...
	 */
}
