package org.onosproject.mcp.root;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.onosproject.mcp.protocol.BasicHandler;
import org.onosproject.mcp.protocol.Domain;
import org.onosproject.mcp.protocol.HelloMessage;
import org.onosproject.mcp.protocol.Message;
import org.onosproject.mcp.protocol.field.Type;
import org.onosproject.mcp.protocol.impl.FeatureReply;
import org.onosproject.mcp.protocol.impl.FeatureRequest;
import org.onosproject.mcp.protocol.impl.FlowRequest;
import org.onosproject.mcp.protocol.impl.HelloMessageImpl;
import org.onosproject.mcp.protocol.impl.MessageBasic;
import org.onosproject.mcp.protocol.impl.TopologyReply;

import static org.onosproject.mcp.protocol.field.Type.*;


@ChannelHandler.Sharable
public class RootServerHandler extends BasicHandler {
//    public static final Logger log = LoggerFactory.getLogger(RootServerHandler.class);

    static final EventExecutorGroup group = new DefaultEventExecutorGroup(16);

    //保留所有与服务器建立连接的channel对象
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private ChannelState state;
    private Channel channel;
    private Domain domain;

    public RootServerHandler(RootController rootController) {
        super("root", rootController.getVersion());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        setState(ChannelState.INIT);
        state.processMessage(this, null);
    }

    /*
     * 数据包的处理：按照状态类型，在下面状态机ACTIVE中处理相应业务逻辑    *
     * */
    @Override
    protected void process(ChannelHandlerContext ctx, String str, Type type) {
        //TODO:1.消息进来判断消息类型，并交给状态机进行处理
        switch (type){
            case HELLO:
                HelloMessageImpl msg = HelloMessageImpl.builder().fromJSON(str);
                System.out.println("Step2:"+msg.writeTo());
                state.processMessage(this, msg);
                break;
            case FEATURE_REPLY:
                FeatureReply msg1 = FeatureReply.builder().fromJSON(str);
                System.out.println("Step4:"+msg1.writeTo());
                state.processMessage(this, msg1);
                break;
            case FLOW_REQUEST:
                FlowRequest flowRequest = FlowRequest.builder().fromJSON(str);
                state.processMessage(this, flowRequest);
                break;
            case TOPOLOGY_UPDATE:
                TopologyReply topologyReply = TopologyReply.builder().fromJSON(str);
                state.processMessage(this, topologyReply);
            case OTHER:
                System.out.println("--------------------------");
                MessageBasic messageBasic = MessageBasic.fromJSON(str);
                state.processMessage(this, messageBasic);
                break;
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        setState(ChannelState.WAIT_HELLO);
        channel = ctx.channel();
        //ServerDataPool.setConnectionList(ctx);
    }

    //表示服务端与客户端连接建立
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();  //其实相当于一个connection
        //channelGroup.writeAndFlush(" 【服务器】 -" +channel.remoteAddress() +" 加入\n");
        channelGroup.add(channel);
    }

    static DomainListener domainListener;
    public void setDomainListener(DomainListener listener) {
        domainListener = listener;
    }
    enum ChannelState{
        INIT(false){
            @Override
            void processMessage(RootServerHandler handler, Message msg) {
                System.out.println("Step1:建立连接初始化");
            }
        },
        WAIT_HELLO(false){
            @Override
            void processMessage(RootServerHandler handler, Message msg) {
                HelloMessage msgs = (HelloMessage) msg;
                handler.domain = new Domain();
                handler.domain.setVersion(msgs.getVersion());
                handler.domain.updataActiveTimeStamp();
                handler.sendFeaturesRequestMsg(domainListener.setDomainID());
                handler.setState(WAIT_FEATURES_REPLY);
            }
        },
        WAIT_FEATURES_REPLY(false){
            @Override
            void processMessage(RootServerHandler handler, Message msg) {
                FeatureReply msgs = (FeatureReply) msg;
                handler.domain.setDomainId(msgs.getDomainID());
                handler.domain.setHostList(msgs.getHostList());
                handler.domain.setChannel(handler.channel);
                handler.domain.setConnected(true);
                handler.domain.updataActiveTimeStamp();
                domainListener.addDomain(handler.domain);
                handler.sendOKRequestMsg();
                handler.setState(ACTIVE);
            }
        },
        ACTIVE(true){
            @Override
            void processMessage(RootServerHandler handler, Message msg) {
                //TODO:2.将消息当作接口返回，并进行业务处理
                switch (msg.getType()){
                    case OTHER:
                        MessageBasic messageBasic = (MessageBasic) msg;
                        System.out.println("Recv " + messageBasic.getMsg());
                        break;
                    case FLOW_REPLY:
                        domainListener.flowRequest((FlowRequest) msg, handler.domain);
                        break;
                    case TOPOLOGY_UPDATE:
                        domainListener.topologyUpdate((TopologyReply) msg, handler.domain);
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
        void processMessage(RootServerHandler handler, Message msg){};

    }

    @Override
    protected void handlerReaderIdle(ChannelHandlerContext ctx) {
        super.handlerReaderIdle(ctx);
        domain.setConnected(false);
    }

    private void setState(ChannelState state) {
        this.state = state;
    }
    private void sendFeaturesRequestMsg(String domainID) {
        FeatureRequest request = FeatureRequest.builder().version(getVersion())
                .type(Type.FEATURE_REQUEST).domainID(domainID).build();

        System.out.println("Step3:"+request.writeTo());
        channel.write(request.writeTo());
    }
    private void sendOKRequestMsg() {
        MessageBasic ok = new MessageBasic(getVersion(), Type.OK, "");
        channel.write(ok.writeTo());
    }
}