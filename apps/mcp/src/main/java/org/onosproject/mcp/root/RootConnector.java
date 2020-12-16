package org.onosproject.mcp.root;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.onosproject.mcp.protocol.MyMessageEncoder;

import java.util.concurrent.TimeUnit;

public class RootConnector {
//    public static final Logger log = LoggerFactory.getLogger(RootConnector.class);

    private RootController rootController;

    private NioServerSocketChannel execFactory;
    private static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;
    private long systemStartTime;
    private int workerThreads = 16;
    private int bossThreads = 4;
    private static Channel serverChannel;

    private RootServerHandler rootServerHandler;

    public RootConnector(RootController rootController) {
        this.rootController = rootController;
        init();
    }

    private void init() {
        this.systemStartTime = System.currentTimeMillis();
        rootServerHandler = new RootServerHandler(rootController);
    }

    private ServerBootstrap createServerBootStrap(Integer port) throws InterruptedException {

        ServerBootstrap bootstrap;
        EventLoopGroup boss=new NioEventLoopGroup(bossThreads);//可以根据机器核心*2设置
        EventLoopGroup worker=new NioEventLoopGroup(workerThreads);
        try {
            //多线程模式处理
            bootstrap = new ServerBootstrap();

            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChildChannelHandler()); //设置过滤器

            ChannelFuture f = bootstrap.bind(port).sync();
            serverChannel = f.channel();
            System.out.println("服务端启动成功...");
            // 监听服务器关闭监听
            serverChannel.closeFuture().sync();
        } finally {
            boss.shutdownGracefully();//关闭EventLoopGroup，释放掉所有资源包括创建的线程
            worker.shutdownGracefully();
        }
        return bootstrap;
    }


    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel arg0) throws Exception {
            arg0.pipeline().addLast(new IdleStateHandler(10,0,0, TimeUnit.SECONDS));
            arg0.pipeline().addLast(new LineBasedFrameDecoder(1024));
            arg0.pipeline().addLast(new StringDecoder());
            arg0.pipeline().addLast(new MyMessageEncoder());
            arg0.pipeline().addLast(rootServerHandler);
        }
    }


    public void addDomainListener(DomainListener domainListener) {
        rootServerHandler.setDomainListener(domainListener);
    }

    public RootConnector start(Integer port) {
        try {
            createServerBootStrap(port);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void stop() {
        if (serverChannel != null) {
            //log.info("Stopping RootController IO");
            System.out.println("Stopping RootController IO");
            serverChannel.close();
            serverChannel = null;
        }
    }
}
