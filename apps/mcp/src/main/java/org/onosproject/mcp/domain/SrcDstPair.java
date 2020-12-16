package org.onosproject.mcp.domain;

import org.onlab.packet.IpAddress;
import org.projectfloodlight.openflow.types.IPv4Address;
/**
 * 源ip目的ip封装为一个对象（用于对应数据包）
 */
public class SrcDstPair {
    
    private String dstAddress;
    
    private String srcAddress;
    
    public SrcDstPair(String srcAddress, String dstAddress) {
        this.srcAddress = srcAddress;
        this.dstAddress = dstAddress;
    }


    public String getDstAddress() {
        return dstAddress;
    }

    public void setSrcAddress(String srcAddress) {
        this.srcAddress = srcAddress;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public void setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
    }
}
