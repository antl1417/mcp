package org.onosproject.mcp.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;
import org.onosproject.mcp.protocol.impl.MessageBasic;

import static org.onosproject.mcp.protocol.field.Type.*;

public abstract class BasicHandler extends ChannelInboundHandlerAdapter {
    private final Version version; //protocol version
    protected String name;
    //记录次数
    private int heartbeatCount = 0;

    //获取server and client 传入的值
    public BasicHandler(String name, Version version) {
        this.name = name;
        this.version = version;
    }
    /**
     * 继承ChannelInboundHandlerAdapter实现了channelRead就会监听到通道里面的消息
     */

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        String str = (String) msg;
        MessageBasic messageBasic = MessageBasic.fromJSON(str);
        if (messageBasic.getVersion() != Version.VERSION_1_0){
            //TODO : 出错
        }
        switch (messageBasic.getType()) {
            case PING:
                sendPongMsg(ctx);
                break;
            case PONG:
                System.out.println(name + " get  pong  msg  from" + ctx.channel().remoteAddress());
                break;
            default:
                process(ctx,str, messageBasic.getType());
                break;
        }
    }
    protected abstract void process(ChannelHandlerContext ctx, String msg, Type type);

    protected void sendPingMsg(ChannelHandlerContext ctx){
        MessageBasic ping = new MessageBasic(version, Type.PING, "");
        ctx.channel().writeAndFlush(ping.writeTo());

        heartbeatCount++;
        System.out.println(name + " send ping msg to " + ctx.channel().remoteAddress() + "count :" + heartbeatCount);
    }

    private void sendPongMsg(ChannelHandlerContext ctx) {

        MessageBasic pong = new MessageBasic(version, Type.PONG,"");
        ctx.channel().writeAndFlush(pong.writeTo());

        heartbeatCount++;
        System.out.println(name +" send pong msg to "+ctx.channel().remoteAddress() +" , count :" + heartbeatCount);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        IdleStateEvent stateEvent = (IdleStateEvent) evt;
        System.out.println(this.name + "循环心跳监测发送: ");
        switch (stateEvent.state()) {
            case READER_IDLE:
                handlerReaderIdle(ctx);
                break;
            case WRITER_IDLE:
                handlerWriterIdle(ctx);
                break;
            case ALL_IDLE:
                handlerAllIdle(ctx);
                break;
            default:
                break;
        }
    }

    public Version getVersion() {
        return version;
    }

    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        System.err.println("---ALL_IDLE---");
    }

    protected void handlerWriterIdle(ChannelHandlerContext ctx) {
        System.err.println("---WRITER_IDLE---");
    }


    protected void handlerReaderIdle(ChannelHandlerContext ctx) {
        System.err.println("---READER_IDLE---");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //System.err.println(" ---"+ctx.channel().remoteAddress() +"----- is  action" );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //System.err.println(" ---"+ctx.channel().remoteAddress() +"----- is  inAction");
    }
}
