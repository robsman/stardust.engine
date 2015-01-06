
package org.eclipse.stardust.engine.api.ws.interactions;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.eclipse.stardust.engine.api.ws.interactions package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _BpmInteractionFault_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/interactions", "bpmInteractionFault");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.eclipse.stardust.engine.api.ws.interactions
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetInputParametersResponse }
     * 
     */
    public GetInputParametersResponse createGetInputParametersResponse() {
        return new GetInputParametersResponse();
    }

    /**
     * Create an instance of {@link GetDefinition }
     * 
     */
    public GetDefinition createGetDefinition() {
        return new GetDefinition();
    }

    /**
     * Create an instance of {@link GetDefinitionResponse }
     * 
     */
    public GetDefinitionResponse createGetDefinitionResponse() {
        return new GetDefinitionResponse();
    }

    /**
     * Create an instance of {@link GetOwnerResponse }
     * 
     */
    public GetOwnerResponse createGetOwnerResponse() {
        return new GetOwnerResponse();
    }

    /**
     * Create an instance of {@link BpmInteractionFaultXto }
     * 
     */
    public BpmInteractionFaultXto createBpmInteractionFaultXto() {
        return new BpmInteractionFaultXto();
    }

    /**
     * Create an instance of {@link SetOutputParametersResponse }
     * 
     */
    public SetOutputParametersResponse createSetOutputParametersResponse() {
        return new SetOutputParametersResponse();
    }

    /**
     * Create an instance of {@link SetOutputParameters }
     * 
     */
    public SetOutputParameters createSetOutputParameters() {
        return new SetOutputParameters();
    }

    /**
     * Create an instance of {@link GetOwner }
     * 
     */
    public GetOwner createGetOwner() {
        return new GetOwner();
    }

    /**
     * Create an instance of {@link GetInputParameters }
     * 
     */
    public GetInputParameters createGetInputParameters() {
        return new GetInputParameters();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BpmInteractionFaultXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/interactions", name = "bpmInteractionFault")
    public JAXBElement<BpmInteractionFaultXto> createBpmInteractionFault(BpmInteractionFaultXto value) {
        return new JAXBElement<BpmInteractionFaultXto>(_BpmInteractionFault_QNAME, BpmInteractionFaultXto.class, null, value);
    }

}
