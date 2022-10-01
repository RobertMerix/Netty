package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
            return;
        }
        // Sets the port value (throws a NumberFormat-Exception if the port argument is malformed).
        int port = Integer.parseInt(args[0]);
        // Calls the server's start() method.
        new EchoServer(port).start();
    }

    public void start() throws Exception {
        final org.example.EchoServerHandler serverHandler = new org.example.EchoServerHandler();
        // 1. Creates the EventLoopGroup.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 2. Creates the ServerBoostrap.
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    // 3. Specifies the use of NIO Transport Channel.
                    .channel(NioServerSocketChannel.class)
                    // 4. Set the socket address using the specific port.
                    .localAddress(new InetSocketAddress(port))

                    /* 5. Adds an EchoServerHandler to the Channel's ChannelPipeline.
                    this is key, when a new connection is accepted, a new child Channel will be created,
                    and the ChannelInitializer will add an instance of your class 'EchoServerHandler' to
                    the Channel’s ChannelPipeline, this handler will receive notifications about inbound messages.
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // EchoServerHandler is @Sharable, so we can always use the same one.
                            socketChannel.pipeline().addLast(serverHandler);
                        }
                    });

            // Blinds the server asynchronously; sync() waits for the bind to complete.
            ChannelFuture f = b.bind().sync();
            System.out.println(EchoServer.class.getName() + " started and listening for connections on " +
                    f.channel().localAddress());
            // Gets the CloseFuture of the channel and blocks the current thread until it's complete.
            f.channel().closeFuture().sync();
        } finally {
            // Shuts down the EvenLoopGroup releasing all resources.
            group.shutdownGracefully().sync();
        }
    }
}