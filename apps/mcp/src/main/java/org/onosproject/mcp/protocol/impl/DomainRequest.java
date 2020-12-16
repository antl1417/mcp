package org.onosproject.mcp.protocol.impl;


import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;

import java.util.concurrent.atomic.AtomicLong;

public class DomainRequest extends MessageBasic {
    private final long id;
    //使用原子技术
    private static final AtomicLong al = new AtomicLong(0);

    public DomainRequest(Version version, Type type, String msg){
        super(version, type, msg);
        //请求唯一标识id 每次都会自增加1
        id = al.incrementAndGet();
    }

    public long getId() {
        return id;
    }
}
