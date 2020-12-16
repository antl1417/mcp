package org.onosproject.mcp.root;


import org.onosproject.mcp.protocol.Domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerDataPool {

//    private static final Logger LOGGER = LoggerFactory.getLogger(ServerDataPool.class);

    /**
     * 存放已经登录的connection
     */
    private static final Map<String, Domain> ACTIVE_POOL = new HashMap<String, Domain>();

    /**
     * 存放所有链接到服务器的connection
     */
    private static final List<Domain> CONNAETION_LIST = new ArrayList<Domain>();

    /**
     * 根据domainNo获取cx
     */
//    public static ChannelHandlerContext getCtxByEqpNo(String domainNo) {
//        Domain domain = ACTIVE_POOL.get(domainNo);
//        return domain.updataActiveTimeStamp().getCtx();
//    }

    /**
     * 根据domainNo获取cxpackage
     */
    public static Domain getCtxPackageByEqpNo(String domainNo) {
        Domain domain = ACTIVE_POOL.get(domainNo);
        return domain;
    }

    /**
     * 清除无用的链接
     */
//    public static int removeUnuseCtx(Long intervalTimes) {
//        Long now = System.currentTimeMillis();
//        int count = 0;
//        for (CtxPackage ctxPackage : CONNAETION_LIST) {
//            if (!ctxPackage.getLogIn() && (ctxPackage.getLastActiveTimeStamp() + now) < now) {//未登录并且链接维持时间过长的的关闭
//                try {
//                    ctxPackage.getCtx().close();
//                    CONNAETION_LIST.remove(ctxPackage);
//                    LOGGER.info("【删除过期链接】:"+ CONNAETION_LIST.add(ctxPackage));
//                    count++;
//                } catch (Exception e) {
//                    LOGGER.error("【关闭连接:%s , 异常！】", ctxPackage.getCtx().channel().id().asLongText());
//                }
//            }
//        }
//        return count;
//    }

    /**
     * 向CONNAETION_LIST里面添加
     */
//    public static Boolean setConnectionList(ChannelHandlerContext ctx) {
//        Domain domain = new Domain();
//        domain.setCtx(ctx);
//        domain.setLastActiveTimeStamp(System.currentTimeMillis());
//        domain.setLogIn(false);
//        //LOGGER.info("【创建新链接】:"+ CONNAETION_LIST.add(ctxPackage));
//
//        System.out.println("【创建新链接】:"+ CONNAETION_LIST.add(domain));
//        System.out.println("Current:"+ CONNAETION_LIST.toString());
//
//        // removeUnuseCtx(60*15L);
//        return true;
//    }

    /**
     * 向ACTIVE_POOL里面添加对应的ctxPackage
     */
//    public static Boolean setActivePool(String eqpNo, ChannelHandlerContext ctx) {
//        Domain domain = new Domain();
//        domain.setCtx(ctx);
//        domain.setLastActiveTimeStamp(System.currentTimeMillis());
//        domain.setLogIn(true);
//        ACTIVE_POOL.put(eqpNo, domain);
//        return true;
//    }
}
