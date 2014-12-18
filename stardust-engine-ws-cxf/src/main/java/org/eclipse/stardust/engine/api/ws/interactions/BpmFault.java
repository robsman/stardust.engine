
package org.eclipse.stardust.engine.api.ws.interactions;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.6.1
 * 2014-11-26T10:53:27.995+01:00
 * Generated source version: 2.6.1
 */

@WebFault(name = "bpmInteractionFault", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions")
public class BpmFault extends Exception {
    
    private org.eclipse.stardust.engine.api.ws.interactions.BpmInteractionFaultXto bpmInteractionFault;

    public BpmFault() {
        super();
    }
    
    public BpmFault(String message) {
        super(message);
    }
    
    public BpmFault(String message, Throwable cause) {
        super(message, cause);
    }

    public BpmFault(String message, org.eclipse.stardust.engine.api.ws.interactions.BpmInteractionFaultXto bpmInteractionFault) {
        super(message);
        this.bpmInteractionFault = bpmInteractionFault;
    }

    public BpmFault(String message, org.eclipse.stardust.engine.api.ws.interactions.BpmInteractionFaultXto bpmInteractionFault, Throwable cause) {
        super(message, cause);
        this.bpmInteractionFault = bpmInteractionFault;
    }

    public org.eclipse.stardust.engine.api.ws.interactions.BpmInteractionFaultXto getFaultInfo() {
        return this.bpmInteractionFault;
    }
}
