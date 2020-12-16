package org.onosproject.mcp.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MyMessageEncoder extends MessageToByteEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
//        System.out.println("MyMessageEncoder encode 方法被调用");
        String res = msg + System.getProperty("line.separator");
        ByteBuf resp= Unpooled.copiedBuffer(res.getBytes());
        out.writeBytes(resp);
    }
}
