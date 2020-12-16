package org.onosproject.mcp.protocol;

import com.google.common.base.Objects;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onosproject.net.Host;
import org.projectfloodlight.openflow.protocol.Writeable;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.PrimitiveSinkable;

import java.util.Set;

/**
 * 将主机的ipv4地址、mac地址、掩码、主机状态（是否被删除）封装
 */
public class SuperHost{
    Host host = null;

    //private final IPv4Address ipAddress;
    private final Ip4Address ipAddress;
    private final MacAddress macAddress;
    //private final IPv4Address mask;
    private final SuperHostState state;

    public SuperHost(Ip4Address ipAddress, MacAddress macAddress, SuperHostState state) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.state = state;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }
    /*public IPv4Address getIpAddress() {
        return ipAddress;
    }*/
    public Ip4Address getIpAddress() {
        return ipAddress;
    }
    /*public IPv4Address getMask() {
        return mask;
    }*/

    public SuperHostState getState() {
        return state;
    }

    public static SuperHost of(Ip4Address ipAddress, MacAddress macAddress, SuperHostState state) {
        if (ipAddress == null)
            throw new NullPointerException("Property ipAddress must not be null");
        if (macAddress == null)
            throw new NullPointerException("Property macAddress must not be null");
        /*if (mask == null)
            throw new NullPointerException("Property mask must not be null");
        */
        if (state == null)
            throw new NullPointerException("Property state must not be null");
        return new SuperHost(ipAddress, macAddress, state);
    }




    /*@Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        ipAddress.writeTo(bb);
        macAddress.write6Bytes(bb);
        bb.writeByte(mask.getInt());
    }*/

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
        result = prime * result + ((macAddress == null) ? 0 : macAddress.hashCode());
        //result = prime * result + ((mask == null) ? 0 : mask.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SuperHost other = (SuperHost) obj;
        if (this.ipAddress != null) {
            if (other.ipAddress == null)
                return false;
        }else if (!this.ipAddress.equals(other.ipAddress))
            return false;
        if (this.macAddress != null) {
            if (other.macAddress == null)
                return false;
        }else if (!this.macAddress.equals(other.macAddress))
            return false;
        /*if (this.mask != null) {
            if (other.mask == null)
                return false;
        }else if (!this.mask.equals(other.mask))
            return false;*/
        if (this.state != null) {
            if (other.state == null)
                return false;
        }else if (!this.state.equals(other.state))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("ipAddress", ipAddress)
                .add("macAddress", macAddress)
                .add("state", state)
                .toString();
    }
}

