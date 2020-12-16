package org.onosproject.mcp.domain;

import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.onosproject.net.PortNumber.portNumber;
/**
 * 自定义虚拟端口，采用原子技术，每发现一个跨域端口都给他分配一个端口号从1开始依次递增
 * 如果某一端口不是跨域端口而是与主机相连的端口则赋值为0
 */
public class DomainVportMap {
    private AtomicLong vportNo = new AtomicLong(1);
    private Map<ConnectPoint, PortNumber> vportMap = new HashMap<>();
    public DomainVportMap() {
        ConnectPoint location1 = new ConnectPoint(DeviceId.deviceId("of:0000000000000001"),portNumber(3));
        PortNumber portNumber1 = portNumber(vportNo.getAndIncrement());
        vportMap.put(location1, portNumber1);
        ConnectPoint location2 = new ConnectPoint(DeviceId.deviceId("of:0000000000000005"),portNumber(3));
        PortNumber portNumber2 = portNumber(vportNo.getAndIncrement());
        vportMap.put(location2, portNumber2);
    }
    /**
     * 根据虚拟端口号得到对应的connectPoint
     * @param portNumber
     * @return
     */
    public ConnectPoint getLocationByVport(PortNumber portNumber) {
        for (ConnectPoint connectPoint : vportMap.keySet()) {
            if (vportMap.get(connectPoint).equals(portNumber)) {
                return connectPoint;
            }
        }
        return null;
    }
    /**
     * 根据connectPoint得到虚拟端口号
     * @param connectPoint
     * @return
     */
    public PortNumber getLogicalVportNum(ConnectPoint connectPoint) {
        return vportMap.containsKey(connectPoint) ? vportMap.get(connectPoint) : PortNumber.portNumber(0);
    }
}
