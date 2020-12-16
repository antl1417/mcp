
package org.onosproject.mcp.protocol.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onosproject.mcp.protocol.HelloMessage;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;

/**
 * Created by cr on 16-4-7.
 */
public final class HelloMessageImpl implements HelloMessage {

    //private static final Logger log = LoggerFactory.getLogger(HelloMessageImpl.class);
    public static Builder builder() {
        return new Builder();
    }

    private Version version;
    private Type type;
    private String msg;

    private HelloMessageImpl(Builder builder) {
        this.version = builder.version;
        this.type = builder.type;
        this.msg = builder.msg;
    }

    @Override
    public Version getVersion() {
        return this.version;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    public String writeTo() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static class Builder implements HelloMessage.Builder {
        private Version version;
        private Type type = Type.HELLO;
        private String msg;

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
        public Builder msg(String msg){
            this.msg = msg;
            return this;
        }
        
        @Override
        public HelloMessageImpl build() {
            return new HelloMessageImpl(this);
        }

        @Override
        public HelloMessageImpl fromJSON(String json) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(json, HelloMessageImpl.class);
        }
    }
}
