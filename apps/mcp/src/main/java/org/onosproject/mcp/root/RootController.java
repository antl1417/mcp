package org.onosproject.mcp.root;


import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.onosproject.mcp.protocol.Domain;
import org.onosproject.mcp.protocol.field.Version;
import org.onosproject.mcp.protocol.impl.FlowRequest;
import org.onosproject.mcp.protocol.impl.TopologyReply;

import java.util.ArrayList;
import java.util.List;
//@Component(immediate = true)
public class RootController {
    Version version;
    public RootController(Version version) {
        this.version = version;
    }
    public Version getVersion() {
        return version;
    }

    public void addDomain(Domain domain) {
//        for (OxpDomainListener listener : oxpDomainListeners) {
//            listener.domainConnected(domain);
//        }
    }


    public static void main(String[] args) {
        final List<Domain> domainList = new ArrayList<Domain>();
        RootController rootController = new RootController(Version.VERSION_1_0);
        RootConnector rootConnector = new RootConnector(rootController);
        rootConnector.addDomainListener(new DomainListener(){
            //TODO:4.在这里实现业务逻辑
            @Override
            public String setDomainID() {
                //主控制器分配domainID
                return "1001";
            }

            @Override
            public void addDomain(Domain domain) {
                System.out.println("Step5:addDomain");
            }

            @Override
            public void flowRequest(FlowRequest msg, Domain domain) {
                //domain.sendMessage();
                //流表请求消息
            }

            @Override
            public void topologyUpdate(TopologyReply topologyReply, Domain domain) {
                //拓扑更新消息
            }
        });
        rootConnector.start(9090);
    }
}
