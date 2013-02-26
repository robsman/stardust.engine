
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;

public class Adapter2
    extends XmlAdapter<String, ProcessInstanceState>
{


    public ProcessInstanceState unmarshal(String value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.parseProcessInstanceState(value));
    }

    public String marshal(ProcessInstanceState value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.printProcessInstanceState(value));
    }

}
