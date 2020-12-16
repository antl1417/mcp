
package org.onosproject.mcp.protocol;


import org.onosproject.mcp.protocol.field.Type;

/**
 * Created by cr on 16-4-7.
 */
public interface HelloMessage extends Message {
    //List<OXPHelloElem> getElements() throws UnsupportedOperationException;

    public interface Builder extends Message.Builder {
        Builder type(Type type);
        Builder msg(String msg);
        //List<OXPHelloElem> getElements() throws UnsupportedOperationException;
        //Builder setElements(List<OXPHelloElem> elements) throws UnsupportedOperationException;
    }
}
