package org.onosproject.mcp.protocol.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onosproject.mcp.protocol.HelloMessage;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;
public class FlowRequest extends DomainRequest{
    private String srcIP;
    private String dstIP;
    private int inPort;

    public static Builder builder() {
        return new Builder();
    }

    private FlowRequest(Builder builder) {
        super(builder.version, builder.type, builder.msg);
        this.srcIP = builder.srcIP;
        this.dstIP = builder.dstIP;
        this.inPort = builder.inPort;
    }

    public int getInPort() {
        return inPort;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public String getDstIP() {
        return dstIP;
    }

    public String writeTo() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static class Builder implements HelloMessage.Builder {
        private Version version;
        private Type type = Type.FLOW_REQUEST;
        private String msg;
        private String srcIP;
        private String dstIP;
        private int inPort;

        public Builder() {
        }


        @Override
        public Builder version(Version version) {
            this.version = version;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder inPort(int inPORT) {
            this.inPort = inPORT;
            return this;
        }

        public Builder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder srcIP(String srcIP) {
            this.srcIP = srcIP;
            return this;
        }

        public Builder dstIP(String dstIP) {
            this.dstIP = dstIP;
            return this;
        }

        @Override
        public FlowRequest build() {
            return new FlowRequest(this);
        }

        @Override
        public FlowRequest fromJSON(String json) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(json, FlowRequest.class);
        }
    }
}
