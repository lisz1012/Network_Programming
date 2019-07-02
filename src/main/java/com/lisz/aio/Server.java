/**
 * AIO还是无法摆脱臭名昭著的ByteBuffer。在Windows下，AIO比NIO好用
 * 因为在Windows下用的是Completion port完成端口 -- Windows下线程
 * 编程效率最高的模型；但是在Linux下，AIO只是对NIO的一个封装，他背后
 * 用的还是轮询。Netty现实之中还是写server，99%要跑在Linux上，所以
 * AIO在Linux上只能说接口稍微方便一点，用到了异步（Observer设计模式）
 * 单多线程跟异步没有关系
 */
package com.lisz.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Server {
    public static void main(String[] args) throws Exception {
        final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open()
                .bind(new InetSocketAddress(8888));
        
        // 这里accept非阻塞，NIO在轮询得知有人要连上来的时候才accept，所以虽然是阻塞，但是很快会返回
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
        	// 模版方法，serverChannel监听了事情之后，系统内核发现有人连上来了，通知 server，serverChannel内部调了个程序，在程序里调用了这个completed方法
            @Override
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                serverChannel.accept(null, this);
                try {
                    System.out.println(client.getRemoteAddress());
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() { // 异步
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            System.out.println(new String(attachment.array(), 0, result));
                            client.write(ByteBuffer.wrap("HelloClient".getBytes()));
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();
            }
        });

        while (true) {
            Thread.sleep(1000);
        }

    }
}