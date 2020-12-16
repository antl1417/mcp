package org.onosproject.mcp.protocol.field;

public enum Type {
    PING(0),
    PONG(1),
    HELLO(100),
    OK(101),
    FEATURE_REQUEST(200),
    FEATURE_REPLY(201),
    FLOW_REQUEST(300),
    FLOW_REPLY(301),
    TOPOLOGY_UPDATE(400),
    TOPOLOGY_REQUEST(500),
    TOPOLOGY_REPLY(501),
    ERROR(-1),
    OTHER(55);

    private int value;

    Type(int value) {
        this.value = value;
    }
    public static Type valaueOf(int value) {
        switch (value) {
            case 0:
                return PING;
            case 1:
                return PONG;
            case 100:
                return HELLO;
            case 101:
                return OK;
            case 200:
                return FEATURE_REQUEST;
            case 201:
                return FEATURE_REPLY;
            case 300:
                return FLOW_REQUEST;
            case 301:
                return FLOW_REPLY;
            case 400:
                return TOPOLOGY_UPDATE;
            case 500:
                return TOPOLOGY_REQUEST;
            case 501:
                return TOPOLOGY_REPLY;
            case -1:
                return ERROR;
            case 55:
                return OTHER;
            default:
                throw new IllegalArgumentException("Illegal wire value for type Type in version 1.0: " + value);
        }
    }

    public int value() {
        return value;
    }

}
