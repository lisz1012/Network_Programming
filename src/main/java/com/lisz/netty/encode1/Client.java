package com.lisz.netty.encode1;

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
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(workerGroup)
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
		//f.channel().writeAndFlush(Unpooled.copiedBuffer("aaa$_".getBytes()));
		//f.channel().writeAndFlush(Unpooled.copiedBuffer("bbbb$_".getBytes()));
		//f.channel().writeAndFlush(Unpooled.copiedBuffer("ccccc$_".getBytes()));
		// 异步，另起一个线程进行写操作
		f.channel().writeAndFlush(Unpooled.copiedBuffer("aaa$_bbbb$_ccccccc$_".getBytes()));
		
		f.channel().closeFuture().sync();
		workerGroup.shutdownGracefully();
	}

}
