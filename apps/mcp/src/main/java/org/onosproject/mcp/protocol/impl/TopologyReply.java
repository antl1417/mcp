package org.onosproject.mcp.protocol.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onosproject.mcp.protocol.HelloMessage;
import org.onosproject.mcp.protocol.SuperHost;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;
import org.onosproject.net.Host;

import java.util.List;

public class TopologyReply extends MessageBasic {
    private String domainID;
    private List<SuperHost> hostList;

    public static Builder builder() {
        return new Builder();
    }

    private TopologyReply(Builder builder) {
        super(builder.version, builder.type,  builder.msg);
        this.domainID = builder.domainID;
        this.hostList = builder.hostList;
    }

    public String writeTo() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static class Builder implements HelloMessage.Builder {
        private Version version;
        private Type type;
        private String msg;
        private String domainID;
        private List<SuperHost> hostList;

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
        public Builder hosts(List<SuperHost> hostList){
            this.hostList = hostList;
            return this;
        }
        public Builder domainID(String domainID){
            this.domainID = domainID;
            return this;
        }
        public Builder msg(String msg){
            this.msg = msg;
            return this;
        }

        @Override
        public TopologyReply build() {
            return new TopologyReply(this);
        }

        @Override
        public TopologyReply fromJSON(String json) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(json, TopologyReply.class);
        }
    }
}
