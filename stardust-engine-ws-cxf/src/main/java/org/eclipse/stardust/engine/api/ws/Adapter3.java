
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;

public class Adapter3
    extends XmlAdapter<String, ActivityInstanceState>
{


    public ActivityInstanceState unmarshal(String value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.parseActivityInstanceState(value));
    }

    public String marshal(ActivityInstanceState value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.printActivityInstanceState(value));
    }

}
