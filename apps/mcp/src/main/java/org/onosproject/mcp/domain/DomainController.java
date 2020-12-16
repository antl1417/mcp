package org.onosproject.mcp.domain;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.ARP;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.mcp.protocol.Domain;
import org.onosproject.mcp.protocol.Root;
import org.onosproject.mcp.protocol.SuperHost;
import org.onosproject.mcp.protocol.SuperHostState;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.field.Version;
import org.onosproject.mcp.protocol.impl.FlowReply;
import org.onosproject.mcp.protocol.impl.FlowRequest;
import org.onosproject.mcp.protocol.impl.MessageBasic;
import org.onosproject.mcp.protocol.impl.TopologyReply;
import org.onosproject.mcp.protocol.impl.TopologyRequest;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.edge.EdgePortService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyService;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
public class DomainController {

    private static String domainId;

    private Version version = Version.VERSION_1_0;

    static Root root;

    private HashMap<SrcDstPair, Ethernet> PktMap = new HashMap<>();//把数据包的源ip目的ip作为键，将数据包作为值存到哈希表中

    private DomainVportMap vportMap = new DomainVportMap();//获取虚拟端口，后续需要改进

    public DomainController() {}

    public DomainController(Version version) {
        this.version = version;
    }

    private ApplicationId appID;

    private DomainConnector domainConnector;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;


    public Version getVersion() {
        return this.version;
    }
    @Activate
    public void activate() {

        appID = coreService.registerApplication("org.onosproject.mcp");

        DomainController domainController = new DomainController(Version.VERSION_1_0);

        //1.创造客户端启动器
        domainConnector = new DomainConnector(domainController);
        //2.增加客户端监听器
        domainConnector.addRootListener(new RootListener() {
            //TODO:4.在这里实现业务逻辑
            @Override
            public void Success(final Root root, String domainID) {
                //Test
                Thread t = new Thread(new Runnable(){
                    public void run(){
                        // run方法具体重写
                        Scanner scanner = new Scanner(System.in);
                        while (scanner.hasNextLine()) {
                            String msg = scanner.nextLine();
                            //通过channel 发送到服务器端
                            MessageBasic messageBasic = new MessageBasic(Version.VERSION_1_0, Type.OTHER, msg);
                            root.sendMessage(messageBasic);
                        }
                    }});
                t.start();
                DomainController.root = root;
                DomainController.domainId = domainID;
            }

            @Override
            public void flowReply(Root root, FlowReply flowReply) {
                //流表下发消息
                handleflowReply(flowReply);
            }

            @Override
            public void topologyRequest(Root root, TopologyRequest topologyRequest) {
                //根控制器请求拓扑消息 消息类型 Type.TOPOLOGY_REQUEST
                //返回 Type.TOPOLOGY_REPLY
                updateExistHosts(topologyRequest);
            }
        });
        //添加主机监听器，当有主机上线或者下线时，上报给主控制器（上线时，主机的状态是ACTIVATE，下线时是INACTIVATE）
        hostService.addListener(new HostListener() {
            @Override
            public void event(HostEvent event) {
                Host updatedHost = null;
                Host removedHost = null;
                List<SuperHost> SuperHosts = new ArrayList<>();
                switch (event.type()) {
                    case HOST_ADDED:
                        updatedHost = event.subject();
                        break;
                    case HOST_REMOVED:
                        removedHost = event.subject();
                        break;
                    case HOST_UPDATED:
                        updatedHost = event.subject();
                        removedHost = event.prevSubject();
                        break;
                    default:
                }
                if (removedHost != null) {
                    SuperHosts.addAll(toSuperHosts(removedHost, SuperHostState.INACTIVATE));//将要下线的主机状态置成INACTIVATE
                }
                if (updatedHost != null) {
                    SuperHosts.addAll(toSuperHosts(updatedHost, SuperHostState.ACTIVATE));//将上线的主机置成ACTIVATE
                }
                if (SuperHosts.isEmpty()) {
                    return;
                }
                sendHostChangeMsg(SuperHosts,null);//发送TOPOLOGY_UPDATE消息
            }
        });

        packetService.addProcessor(new PacketProcessor() {
            @Override
            public void process(PacketContext context) {
                if (context.isHandled()) {
                    return;//已被处理，不管这个包
                }

                InboundPacket pkt = context.inPacket();
                Ethernet ethPkt = pkt.parsed();

                if (ethPkt == null || ethPkt.getEtherType() == Ethernet.TYPE_LLDP) {
                    System.out.println("find a lldp");
                    return;//LLDP包，不管
                }
                //得到inPort和所在的交换机
                PortNumber srcPort = context.inPacket().receivedFrom().port();
                DeviceId srcDeviceId = context.inPacket().receivedFrom().deviceId();
                ConnectPoint connectPoint = new ConnectPoint(srcDeviceId, srcPort);

                IpAddress dstAddress;
                IpAddress srcAddress;
                //对应ARP的包或者IPV4的包，分别得到源、目的ip
                if (ethPkt.getEtherType() == Ethernet.TYPE_ARP) {
                    System.out.println("find a ARP");
                    dstAddress = Ip4Address.valueOf(((ARP) ethPkt.getPayload()).getTargetProtocolAddress());
                    srcAddress = Ip4Address.valueOf(((ARP) ethPkt.getPayload()).getSenderProtocolAddress());
                }else if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                    dstAddress = Ip4Address.valueOf(((IPv4) ethPkt.getPayload()).getDestinationAddress());
                    srcAddress = Ip4Address.valueOf(((IPv4) ethPkt.getPayload()).getSourceAddress());
                    System.out.println("find a IPV4");
                }else {
                    return;
                }
                //泛洪，如果目的ip在本子网内，不上报这条流
                if (topologyService.isBroadcastPoint(topologyService.currentTopology(),
                                                     context.inPacket().receivedFrom())) {
                    context.treatmentBuilder().setOutput(PortNumber.FLOOD);
                    context.send();
                } else {
                    context.block();
                }
                Set<Host> hosts = hostService.getHostsByIp(dstAddress);
                if (null != hosts && hosts.size() > 0) {
                    System.out.println("host is not null");
                    return;
                }
                System.out.println("host is null");
                //将源ip、目的ip作为键，数据包作为值存起来，以便于之后的转发
                SrcDstPair pair = new SrcDstPair(srcAddress.toString(), dstAddress.toString());
                PktMap.put(pair, ethPkt);
                //发送flowrequest消息
                int inPort = (int) vportMap.getLogicalVportNum(connectPoint).toLong();
                FlowRequest flowRequest = FlowRequest
                        .builder()
                        .srcIP(srcAddress.toString())
                        .dstIP(dstAddress.toString())
                        .version(Version.VERSION_1_0)
                        .type(Type.FEATURE_REQUEST)
                        .inPort(inPort)
                        .build();
                root.sendMessage(flowRequest);
                System.out.println(srcAddress.toString() +" "+ dstAddress.toString()+" "+ inPort);
                System.out.println("send flow msg");
                context.block();
            }
        }, PacketProcessor.director(1));//优先级设为1，fwd里的优先级设为3，proxyarp里的优先级设为2
        //3.启动客户端
        domainConnector.start(9090, "127.0.0.1");
    }
    /**
     * 收到topologyrequest的消息后，将当前的主机信息上报主控制器
     * @param topologyRequest
     */
    private void updateExistHosts(TopologyRequest topologyRequest) {
        List<SuperHost> SuperHosts = new ArrayList<>();
        for(Host host : hostService.getHosts()) {
            SuperHosts.addAll(toSuperHosts(host, SuperHostState.ACTIVATE));
        }
        if (SuperHosts.isEmpty()) {
            return;
        }
        sendHostChangeMsg(SuperHosts, topologyRequest);
    }
    /**
     * 处理主控制器的flowreply，进行流表下发和转发数据包
     * @param flowReply
     */
    private void handleflowReply(FlowReply flowReply) {
        PortNumber inPort = PortNumber.portNumber(flowReply.getInPort());//得到inport
        PortNumber outPort = PortNumber.portNumber(flowReply.getOutPort());//得到outport
        Ip4Address srcIp = Ip4Address.valueOf(flowReply.getSrcIP());//得到源主机Ip
        Ip4Address dstIp = Ip4Address.valueOf(flowReply.getDstIP());//得到目的主机Ip
        SrcDstPair pair = new SrcDstPair(srcIp.toString(), dstIp.toString());
        Ethernet ethpkt = PktMap.get(pair);//得到数据包
        Short type = ethpkt.getEtherType();//得到数据包类型
        Set<Host> srcHosts = hostService.getHostsByIp(srcIp);//根据源Ip获取到源主机（源主机不一定在本子网，这里可能为空）
        Host srcHost = null;
        if (srcHosts != null && srcHosts.size() > 0) {
            srcHost = (Host) srcHosts.toArray()[0];
        }
        Set<Host> dstHosts = hostService.getHostsByIp(dstIp);//根据目的Ip获取到目的主机
        Host dstHost = null;
        if (dstHosts != null && dstHosts.size() > 0) {
            dstHost = (Host) dstHosts.toArray()[0];
        }
        ConnectPoint srcConnectPoint = null;
        ConnectPoint dstConnectPoint = null;
        if (srcHost == null) {
            srcConnectPoint = vportMap.getLocationByVport(inPort);//如果上面得到的源主机为空，则说明inport是一个跨域端口，那么根据vportMap得到srcconnectpoint
        }else {
            srcConnectPoint = srcHost.location();//如果上面得到的源主机不空，则说明inport是一个与主机相连的端口，那么根据主机ip得到srcconnectpoint
        }
        if (dstHost == null) {
            dstConnectPoint = vportMap.getLocationByVport(outPort);//同上
        }else {
            dstConnectPoint = dstHost.location();//同上
        }
        if (srcConnectPoint == null || dstConnectPoint == null) {
            return;
        }
        if (srcConnectPoint.deviceId().equals(dstConnectPoint.deviceId())) {
            installForwardRule(type, srcConnectPoint.deviceId(), srcIp, dstIp, srcConnectPoint.port(), dstConnectPoint.port());//如果入口出口都在一个交换机上，下发流表
            packetOut(dstConnectPoint, ethpkt);
            return;
        }
        Set<Path> paths = pathService.getPaths(srcConnectPoint.deviceId(), dstConnectPoint.deviceId());//如果入口出口不在一个交换机上，得到入口出口之间的路的集合
        if (paths == null || paths.size() == 0) {
            return;
        }
        Path path = (Path) paths.toArray()[0];//选第一条路
        List<Link> links = path.links();//得到这条路的每一个link
        Link lastlink = null;
        for (Link link : links) {
            if (link.src().equals(path.src())) {
                installForwardRule(type, link.src().deviceId(), srcIp, dstIp,
                                   srcConnectPoint.port(),
                                   link.src().port());
            }else {
                installForwardRule(type, link.src().deviceId(), srcIp, dstIp,
                                   lastlink.dst().port(), link.src().port());
            }
            if (link.dst().equals(path.dst())) {
                installForwardRule(type, link.dst().deviceId(),
                                   srcIp, dstIp, link.dst().port(), dstConnectPoint.port());
            }
            lastlink = link;
        }
        packetOut(dstConnectPoint, ethpkt);//转发数据包
    }
    /**
     * 转发数据包
     * @param connectPoint
     * @param ethpkt
     */
    private void packetOut(ConnectPoint connectPoint, Ethernet ethpkt) {
        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();
        builder.setOutput(connectPoint.port());
        packetService.emit(new DefaultOutboundPacket(connectPoint.deviceId(),
                                                     builder.build(), ByteBuffer.wrap(ethpkt.serialize())));
        return;
    }
    /**
     * 下发流表
     * @param type
     * @param deviceId
     * @param srcIp
     * @param dstIp
     * @param inPort
     * @param outPort
     */
    private void installForwardRule(Short type, DeviceId deviceId, Ip4Address srcIp, Ip4Address dstIp, PortNumber inPort, PortNumber outPort) {
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        selectorBuilder.matchInPort(inPort)
                .matchEthType(type)
                .matchIPSrc(Ip4Prefix.valueOf(srcIp.toInt(), Ip4Prefix.MAX_MASK_LENGTH))
                .matchIPDst(Ip4Prefix.valueOf(dstIp.toInt(), Ip4Prefix.MAX_MASK_LENGTH));
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(outPort)
                .build();
        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(10)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appID)
                .makeTemporary(15)
                .add();
        flowObjectiveService.forward(deviceId, forwardingObjective);
    }
    /**
     * 向主控制器发送TOPOLOGY_UPDATE或者TOPOLOGY_REPLY
     * @param superHosts
     * @param topologyRequest
     */
    private void sendHostChangeMsg(List<SuperHost> superHosts, TopologyRequest topologyRequest) {
        //System.out.println("send host msg");
        for (SuperHost superHost : superHosts) {
            System.out.println(superHost);
        }
        TopologyReply topologyReply;
        if (topologyRequest == null) {
            topologyReply = TopologyReply.builder()
                    .domainID(domainId)
                    .hosts(superHosts)
                    .type(Type.TOPOLOGY_UPDATE)
                    .version(Version.VERSION_1_0)
                    .build();
        }else {
            topologyReply = TopologyReply.builder()
                    .domainID(domainId)
                    .hosts(superHosts)
                    .type(Type.TOPOLOGY_REPLY)
                    .version(Version.VERSION_1_0)
                    .msg("")
                    .build();
        }
        root.sendMessage(topologyReply);
    }
    /**
     * 将主机的Ip地址、状态、mac地址上报，主控制器需要啥就拿啥
     * @param host
     * @param state
     * @return
     */
    private List<SuperHost> toSuperHosts(Host host, SuperHostState state) {
        List<SuperHost> hosts = new ArrayList<>();
        for (IpAddress ip : host.ipAddresses()) {
            Ip4Address ipAddress = Ip4Address.valueOf(ip.toOctets());
            MacAddress macAddress = MacAddress.valueOf(host.mac().toBytes());
            SuperHost superhost = SuperHost.of(ipAddress, macAddress, state);
            hosts.add(superhost);
        }
        return hosts;
    }

    @Deactivate
    public void deactivate() {
        domainConnector.stop();
    }
}
