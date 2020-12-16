package org.onosproject.mcp.root;


import org.onosproject.mcp.protocol.Domain;
import org.onosproject.mcp.protocol.impl.FlowRequest;
import org.onosproject.mcp.protocol.impl.TopologyReply;

public interface DomainListener {
    //TODO:3.在这里添加回调函数，如第二步
    String setDomainID();

    void addDomain(Domain domain);

    void flowRequest(FlowRequest msg, Domain domain);

    void topologyUpdate(TopologyReply topologyReply, Domain domain);
}
