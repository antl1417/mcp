package org.onosproject.mcp.protocol.impl;

import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;

public class Response extends MessageBasic{
    private long id;//请求ID
    private int status;//响应状态

    public Response(Version version, Type type, String msg) {
        super(version, type, msg);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
