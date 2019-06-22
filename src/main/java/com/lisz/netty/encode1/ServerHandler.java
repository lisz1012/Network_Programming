package com.lisz.netty.encode1;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String request = (String)msg;
		System.out.println("Server received request: " + request);
		String response = "我是响应数据1$_我是响应数据2$_我是响应数据3$_";
		// addListener(ChannelFutureListener.CLOSE);长连接变为短连接，server回写数据之后就与client断开
		ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes())).addListener(ChannelFutureListener.CLOSE);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {}
}
