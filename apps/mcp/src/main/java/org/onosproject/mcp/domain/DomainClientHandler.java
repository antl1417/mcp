package org.onosproject.mcp.domain;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.onosproject.mcp.protocol.BasicHandler;
import org.onosproject.mcp.protocol.Message;
import org.onosproject.mcp.protocol.Root;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.impl.FeatureRequest;
import org.onosproject.mcp.protocol.impl.*;
import org.onosproject.net.Host;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import static org.onosproject.mcp.protocol.field.Type.*;

@ChannelHandler.Sharable
public class DomainClientHandler extends BasicHandler {
    private static final Logger logger=Logger.getLogger(DomainClientHandler.class.getName());

    private final DomainConnector connector;
    private byte[] req;
    private ChannelState state;
    private String domainID;
    private List<Host> hostList;
    private Channel channel;
    private Root root;
    private static Vector<RootListener> oVector = new Vector<>();

    public DomainClientHandler(DomainConnector connector) {
        super("domain", connector.getVersion());
        this.connector = connector;
        this.root = connector.getRoot();
    }

    @Override
    protected void process(ChannelHandlerContext ctx, String str, Type type) {
        //TODO:1.消息进来判断消息类型，并交给状态机进行处理
        switch (type){
            case FEATURE_REQUEST:
                FeatureRequest message = FeatureRequest.builder().fromJSON(str);
                System.out.println("Step2:"+message.writeTo());
                state.processMessage(this, message);
                break;
            case OK:
                MessageBasic messageBasic = MessageBasic.fromJSON(str);
                System.out.println("Step4:OK");
                setState(ChannelState.ACTIVE);
                root.setConnected(true);
                notifySuccess(this.root, this.domainID);
                break;
            case FEATURE_REPLY:
                FlowReply flowReply = FlowReply.builder().fromJSON(str);
                state.processMessage(this, flowReply);
                break;
            case TOPOLOGY_REQUEST:
                TopologyRequest topologyRequest = TopologyRequest.builder().fromJSON(str);
                state.processMessage(this, topologyRequest);
                break;
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
        sendHandshakeHelloMsg();
        setState(ChannelState.WAIT_FEATURES_REQUEST);
    }

    @Override
    protected void handlerWriterIdle(ChannelHandlerContext ctx) {
        super.handlerWriterIdle(ctx);
        sendPingMsg(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        connector.doConnect();
    }

    enum ChannelState{
        INIT(false),
        WAIT_FEATURES_REQUEST(false){
            @Override
            void processMessage(DomainClientHandler handler, Message msg) {
                FeatureRequest request = (FeatureRequest) msg;
                handler.domainID = request.getDomainID();
                handler.root.setChannel(handler.channel);
                handler.root.setVersion(handler.getVersion());
                handler.root.updataActiveTimeStamp();
                handler.sendFeaturesReplyMsg();
            }
        },
        ACTIVE(true){
            @Override
            void processMessage(DomainClientHandler handler, Message msg) {
                //TODO:2.将消息当作接口返回，并进行业务处理
                switch (msg.getType()){
                    case TOPOLOGY_REQUEST:
                        notifyTopologyRequest(handler.root, (TopologyRequest) msg);
                        break;
                    case FLOW_REPLY:
                        notifyFlowReply(handler.root, (FlowReply) msg);
                        break;
                }
            }
        };

        private final boolean handshakeComplete;
        ChannelState(boolean handshakeComplete) {
            this.handshakeComplete = handshakeComplete;
        }
        public boolean isHandshakeComplete() {
            return handshakeComplete;
        }
        void processMessage(DomainClientHandler handler, Message msg) {}

    }
    private void setState(ChannelState state) {
        this.state = state;
    }
    public ChannelState getState(){return this.state;}
    public Root getRoot(){return this.root;}
    private void sendHandshakeHelloMsg() {
        HelloMessageImpl request = HelloMessageImpl.builder().version(getVersion())
                .type(Type.HELLO).build();

        System.out.println("Step1:"+request.writeTo());
        channel.writeAndFlush(request.writeTo());
    }
    private void sendFeaturesReplyMsg() {
        FeatureReply reply = FeatureReply.builder()
                .version(getVersion()).type(Type.FEATURE_REPLY)
                .domainID(domainID).hosts(hostList).build();

        System.out.println("Step3:"+reply.writeTo());
        channel.write(reply.writeTo());
    }

    //增加一个监听器
    public void addRootListener(RootListener observer) {
        this.oVector.add(observer);
    }

    //删除一个监听器
    public void deleteRootListener(RootListener observer) {
        this.oVector.remove(observer);
    }

    //通知所有监听器
    public static void notifyTopologyRequest(Root root, Message msg) {
        for(RootListener observer : oVector) {
            observer.topologyRequest(root, (TopologyRequest) msg);
        }
    }
    public static void notifyFlowReply(Root root, Message msg) {
        for(RootListener observer : oVector) {
            observer.flowReply(root, (FlowReply) msg);
        }
    }
    public static void notifySuccess(Root root, String domainID) {
        for(RootListener observer : oVector) {
            observer.Success(root, domainID);
        }
    }
}
