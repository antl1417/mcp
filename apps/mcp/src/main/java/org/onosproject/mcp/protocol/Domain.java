package org.onosproject.mcp.protocol;

import io.netty.channel.Channel;
import org.onosproject.mcp.protocol.field.Version;
import org.onosproject.mcp.protocol.impl.*;
import org.onosproject.net.Host;

import java.util.List;

public class Domain {
    private List<Host> hostList;
    private String domainId;
    //private DeviceId deviceId;
    private Version version;
    private String ipAddress;
    private String mask;

    private Channel channel;
    //private boolean connected;
    /**
     * 最近一次收到客户端消息时间
     * */
    private Long lastActiveTimeStamp;
    private boolean isConnected = false;

    public Long getLastActiveTimeStamp() {
        return lastActiveTimeStamp;
    }

    public void setLastActiveTimeStamp(Long lastActiveTimeStamp) {
        this.lastActiveTimeStamp = lastActiveTimeStamp;
    }

    public Domain updataActiveTimeStamp(){
        this.setLastActiveTimeStamp(System.currentTimeMillis());
        return this;
    }

    public List<Host> getHostList() {
        return hostList;
    }

    public void setHostList(List<Host> hostList) {
        this.hostList = hostList;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void sendMessage(Message message) {
        if (!isConnected()){
            System.out.println("client断开连接");
        }
        //TODO:5 添加发送报文
        switch (message.getType()){
            case FEATURE_REQUEST:
                channel.writeAndFlush(((FeatureRequest)message).writeTo());
                break;
            case TOPOLOGY_REQUEST:
                channel.writeAndFlush(((TopologyRequest)message).writeTo());
                break;
            case FLOW_REPLY:
                channel.writeAndFlush(((FlowReply)message).writeTo());
                break;
            case OK:
                channel.writeAndFlush(((MessageBasic)message).writeTo());
                break;
        }
//        if (channel.isOpen()) {
//            channel.writeAndFlush(msgs);
//        } else {
//            //log.warn("Drop msg because oxpdomain channel is disconnected,msgs:{}", msgs);
//            System.out.println("Drop msg because oxpdomain channel is disconnected,msgs:{}");
//        }
    }

    public void setConnected(boolean conneced) {
        this.isConnected = conneced;
    }

    public boolean isConnected() {
        return isConnected;
    }
}

