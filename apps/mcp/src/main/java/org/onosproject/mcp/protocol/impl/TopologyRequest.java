package org.onosproject.mcp.protocol.impl;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onosproject.mcp.protocol.HelloMessage;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;

public class TopologyRequest extends MessageBasic{
    private String domainID;

    public static Builder builder() {
        return new Builder();
    }

    private TopologyRequest(Builder builder) {
        super(builder.version, builder.type,  builder.msg);
        this.domainID = builder.domainID;
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
        public Builder domainID(String domainID){
            this.domainID = domainID;
            return this;
        }
        public Builder msg(String msg){
            this.msg = msg;
            return this;
        }

        @Override
        public TopologyRequest build() {
            return new TopologyRequest(this);
        }

        @Override
        public TopologyRequest fromJSON(String json) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(json, TopologyRequest.class);
        }
    }

}
