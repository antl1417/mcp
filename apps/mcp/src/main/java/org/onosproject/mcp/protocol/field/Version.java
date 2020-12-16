package org.onosproject.mcp.protocol.field;

public enum Version {
    VERSION_1_0(1);

    private int value;

    Version(int value) {
        this.value = value;
    }

    public static Version valaueOf(int value) {
        switch (value) {
            case 1:
                return VERSION_1_0;
            default:
                throw new IllegalArgumentException("Illegal wire value of version: " + value);
        }
    }

    public int value() {
        return value;
    }

}
