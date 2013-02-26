
package org.eclipse.stardust.engine.api.ws.xsd;

import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Adapter1
    extends XmlAdapter<String, Date>
{


    public Date unmarshal(String value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.parseDateTime(value));
    }

    public String marshal(Date value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.printDateTime(value));
    }

}
