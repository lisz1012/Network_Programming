package com.lisz.netty.stickypack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class ServerHanlder extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			ByteBuf buf = (ByteBuf)msg;
			System.out.println(buf.toString(CharsetUtil.UTF_8));
			buf = Unpooled.copiedBuffer("Hello, chlient".getBytes());
			ctx.writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
		} finally {
			// 不必要，因为上面有写操作，自动release了
			ReferenceCountUtil.release(msg);
		}
	}
}
