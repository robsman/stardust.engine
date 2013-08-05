package org.eclipse.stardust.engine.api.ws.interactions;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.6.1
 * 2013-08-02T09:17:16.162+02:00
 * Generated source version: 2.6.1
 * 
 */
@WebService(targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions", name = "IBpmInteractionsService")
@XmlSeeAlso({ObjectFactory.class})
public interface IBpmInteractionsService {

    @WebResult(name = "owner", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions")
    @RequestWrapper(localName = "getOwner", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions", className = "org.eclipse.stardust.engine.api.ws.interactions.GetOwner")
    @WebMethod(action = "getOwner")
    @ResponseWrapper(localName = "getOwnerResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions", className = "org.eclipse.stardust.engine.api.ws.interactions.GetOwnerResponse")
    public org.eclipse.stardust.engine.api.ws.UserXto getOwner(
        @WebParam(name = "interactionId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions")
        java.lang.String interactionId
    ) throws BpmFault;

    @RequestWrapper(localName = "setOutputParameters", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions", className = "org.eclipse.stardust.engine.api.ws.interactions.SetOutputParameters")
    @WebMethod(action = "setOutputParameters")
    @ResponseWrapper(localName = "setOutputParametersResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions", className = "org.eclipse.stardust.engine.api.ws.interactions.SetOutputParametersResponse")
    public void setOutputParameters(
        @WebParam(name = "interactionId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions")
        java.lang.String interactionId,
        @WebParam(name = "outputParameters", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions")
        org.eclipse.stardust.engine.api.ws.ParametersXto outputParameters
    ) throws BpmFault;

    @WebResult(name = "definition", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions")
    @RequestWrapper(localName = "getDefinition", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions", className = "org.eclipse.stardust.engine.api.ws.interactions.GetDefinition")
    @WebMethod(action = "getDefinition")
    @ResponseWrapper(localName = "getDefinitionResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions", className = "org.eclipse.stardust.engine.api.ws.interactions.GetDefinitionResponse")
    public org.eclipse.stardust.engine.api.ws.InteractionContextXto getDefinition(
        @WebParam(name = "interactionId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions")
        java.lang.String interactionId
    ) throws BpmFault;

    @WebResult(name = "inputParameters", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions")
    @RequestWrapper(localName = "getInputParameters", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions", className = "org.eclipse.stardust.engine.api.ws.interactions.GetInputParameters")
    @WebMethod(action = "getInputParameters")
    @ResponseWrapper(localName = "getInputParametersResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions", className = "org.eclipse.stardust.engine.api.ws.interactions.GetInputParametersResponse")
    public org.eclipse.stardust.engine.api.ws.ParametersXto getInputParameters(
        @WebParam(name = "interactionId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions")
        java.lang.String interactionId
    ) throws BpmFault;
}
