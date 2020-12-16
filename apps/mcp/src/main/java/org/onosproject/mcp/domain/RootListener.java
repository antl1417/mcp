package org.onosproject.mcp.domain;


import org.onosproject.mcp.protocol.Root;
import org.onosproject.mcp.protocol.impl.FlowReply;
import org.onosproject.mcp.protocol.impl.TopologyRequest;

public interface RootListener {
    //TODO:3.在这里添加回调函数，如第二步
    void Success(Root root, String domainId);

    void flowReply(Root root, FlowReply flowReply);

    void topologyRequest(Root root, TopologyRequest topologyRequest);
}
