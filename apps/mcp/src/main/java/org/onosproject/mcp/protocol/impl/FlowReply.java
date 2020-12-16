package org.onosproject.mcp.protocol.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onosproject.mcp.protocol.HelloMessage;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;
import org.onosproject.net.PortNumber;

public class FlowReply extends Response{
    private final String srcIP;
    private final String dstIP;
    private int inPort;
    private int outPort;
    private final String action;

    public static Builder builder() {
        return new Builder();
    }

    private FlowReply(Builder builder) {
        super(builder.version, builder.type, builder.msg);
        this.srcIP = builder.srcIP;
        this.dstIP = builder.dstIP;
        this.action = builder.action;
        this.inPort = builder.inPort;
        this.outPort = builder.outPort;
    }

    public int getInPort() {
        return inPort;
    }

    public int getOutPort() {
        return outPort;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public String getDstIP() {
        return dstIP;
    }

    public String getAction() {
        return action;
    }

    public String writeTo() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static class Builder implements HelloMessage.Builder {
        private Version version;
        private Type type = Type.FLOW_REPLY;
        private String msg;
        private String srcIP;
        private String dstIP;
        private String action;
        private int inPort;
        private int outPort;

        public Builder() {
        }

        @Override
        public Builder version(Version version){
            this.version = version;
            return this;
        }
        public Builder type(Type type){
            this.type = type;
            return this;
        }
        public Builder inPort(int inPORT){
            this.inPort = inPORT;
            return this;
        }
        public Builder outPort(int outPORT){
            this.outPort = outPORT;
            return this;
        }
        public Builder msg(String msg){
            this.msg = msg;
            return this;
        }
        public Builder srcIP(String srcIP){
            this.srcIP = srcIP;
            return this;
        }
        public Builder dstIP(String dstIP){
            this.dstIP = dstIP;
            return this;
        }
        public Builder action(String action){
            this.action = action;
            return this;
        }

        @Override
        public FlowReply build() {
            return new FlowReply(this);
        }

        @Override
        public FlowReply fromJSON(String json) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(json, FlowReply.class);
        }
    }
}
