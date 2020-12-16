package org.onosproject.mcp.protocol;

import io.netty.channel.Channel;
import org.onosproject.mcp.protocol.field.Version;
import org.onosproject.mcp.protocol.impl.FeatureReply;
import org.onosproject.mcp.protocol.impl.FlowRequest;
import org.onosproject.mcp.protocol.impl.HelloMessageImpl;
import org.onosproject.mcp.protocol.impl.MessageBasic;
import org.onosproject.mcp.protocol.impl.TopologyReply;

import static org.onosproject.mcp.protocol.field.Type.*;

public class Root {
    private Version version;
    private Channel channel;
    private String ipAddress;
    private Long lastActiveTimeStamp;
    private boolean isConnected = false;
    private static Root instense = new Root();

    private Root() { }

    public static Root getInstense() {
        return instense;
    }

    public Long getLastActiveTimeStamp() {
        return lastActiveTimeStamp;
    }

    public void setLastActiveTimeStamp(Long lastActiveTimeStamp) {
        this.lastActiveTimeStamp = lastActiveTimeStamp;
    }

    public Root updataActiveTimeStamp(){
        this.setLastActiveTimeStamp(System.currentTimeMillis());
        return this;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void sendMessage(Message message){
        if (!isConnected()){
            System.out.println("服务器断开连接");
            return;
        }
        switch (message.getType()){
            //TODO:5 添加发送报文
            case HELLO:
                channel.writeAndFlush(((HelloMessageImpl)message).writeTo());
                break;
            case TOPOLOGY_REPLY:
            case TOPOLOGY_UPDATE:
                channel.writeAndFlush(((TopologyReply)message).writeTo());
                break;
            case FEATURE_REPLY:
                channel.writeAndFlush(((FeatureReply)message).writeTo());
                break;
            case FLOW_REQUEST:
                channel.writeAndFlush(((FlowRequest)message).writeTo());
                break;
            case OTHER:
                channel.writeAndFlush(((MessageBasic)message).writeTo());
                break;
        }

    }
}
