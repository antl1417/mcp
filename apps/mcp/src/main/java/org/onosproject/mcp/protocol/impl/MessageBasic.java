
package org.onosproject.mcp.protocol.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onosproject.mcp.protocol.Message;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;

/**
 * Created by cr on 16-4-7.
 */
public class MessageBasic implements Message {

    //private static final Logger log = LoggerFactory.getLogger(HelloMessageImpl.class);

    private Version version;
    private Type type;
    private String msg;

    public MessageBasic() {
    }

    public MessageBasic(Version version, Type type, String msg) {
        this.version = version;
        this.type = type;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public Version getVersion() {
        return this.version;
    }

    @Override
    public Type getType() {
        return this.type;
    }

//    @Override
//    public void writeTo(ChannelBuffer channelBuffer) {
//        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.setPrettyPrinting();
//        Gson gson = gsonBuilder.create();
//    }
    public String writeTo() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static MessageBasic fromJSON(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(json, MessageBasic.class);
    }
}
