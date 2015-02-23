
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.stardust.common.Direction;

public class Adapter2
    extends XmlAdapter<String, Direction>
{


    public Direction unmarshal(String value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.parseDirection(value));
    }

    public String marshal(Direction value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.printDirection(value));
    }

}
