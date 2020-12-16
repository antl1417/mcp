package org.onosproject.mcp.protocol;

/**
 * 主机状态，被删除时置为INACTIVATE
 */
public enum SuperHostState {
    ACTIVATE(true),
    INACTIVATE(false);

    private final boolean activeState;

    private SuperHostState(boolean activeState) {
        this.activeState = activeState;
    }

    public boolean isActive() {
        return activeState;
    }
}
