
package org.onosproject.mcp.protocol;


import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;

/**
 * Created by cr on 16-4-6.
 */
public interface Message {

    Version getVersion();
    Type getType();

    //void writeTo(ChannelBuffer bb);

    public interface Builder {
        Message build();
        Message fromJSON(String json);
        Builder version(Version version);
    }
}
