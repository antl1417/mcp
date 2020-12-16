package org.onosproject.mcp.domain;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.onosproject.mcp.protocol.MyMessageEncoder;
import org.onosproject.mcp.protocol.Root;
import org.onosproject.mcp.protocol.field.Version;

import java.util.concurrent.TimeUnit;


public class DomainConnector {
    private DomainController domainController;
    private static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;

    private long systemStartTime;
    private static Channel clientChannel;
    private DomainClientHandler domainClientHandler;
    private Bootstrap bootstrap;
    private Integer port;
    private String host;
    private Root root;
    private static DomainConnector instance;
    private boolean isStart = false;

    public DomainConnector(DomainController domainController) {
        this.domainController = domainController;
        init();
    }
    public Version getVersion(){return domainController.getVersion();}

    public static synchronized DomainConnector getInstance(DomainController controller){
        if (instance == null){
            instance = new DomainConnector(controller);
        }
        return instance;
    }

    private void init() {
        this.root = Root.getInstense();
        domainClientHandler = new DomainClientHandler(this);
        this.systemStartTime = System.currentTimeMillis();
    }

    private Bootstrap createServerBootStrap() {

        //配置客户端NIO线程组
        EventLoopGroup group=new NioEventLoopGroup();
        try {
            //多线程模式处理
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChildChannelHandler()); //设置过滤器

            doConnect();
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return bootstrap;
    }

    /**
     * 重连机制,每隔2s重新连接一次服务器
     */
    protected void doConnect() {
        if (clientChannel != null && clientChannel.isActive()) {
            return;
        }
        root.setConnected(false);
        ChannelFuture future = bootstrap.connect(host, port);

        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()) {
                    clientChannel = futureListener.channel();
                    System.out.println("Connect to server successfully!");
                    root.setConnected(true);
                } else {
                    System.out.println("Failed to connect to server, try connect after 2s");

                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 2, TimeUnit.SECONDS);
                }
            }
        });
    }

    public void addRootListener(RootListener rootListener) {
        domainClientHandler.addRootListener(rootListener);
    }
    public void deleteRootListener(RootListener rootListener){
        domainClientHandler.deleteRootListener(rootListener);
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel arg0) throws Exception {
            arg0.pipeline().addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));
            arg0.pipeline().addLast(new LineBasedFrameDecoder(1024));
            arg0.pipeline().addLast(new StringDecoder());
            arg0.pipeline().addLast(new MyMessageEncoder());
            arg0.pipeline().addLast(domainClientHandler);
        }
    }

    public synchronized DomainConnector start(Integer port, String host) {
        if (!isStart){
            this.port = port;
            this.host = host;
            createServerBootStrap();
        }
        return this;
    }

    public void stop() {
        if (clientChannel != null && isStart) {
            System.out.println("Stopping RootController IO");
            clientChannel.close();
            clientChannel = null;
            isStart = false;
        }
    }
    public boolean isActive(){
        return domainClientHandler.getState().isHandshakeComplete();
    }
    public Root getRoot(){
        return this.root;
    }
}
