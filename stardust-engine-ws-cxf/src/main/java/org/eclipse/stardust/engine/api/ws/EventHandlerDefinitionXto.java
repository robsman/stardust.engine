
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			A client side view of a workflow event handler.
 * 			
 * 
 * <p>Java class for EventHandlerDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventHandlerDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="rtOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="type" type="{http://eclipse.org/stardust/ws/v2012a/api}EventHandlerTypeDefinition" minOccurs="0"/>
 *         &lt;element name="eventActions" type="{http://eclipse.org/stardust/ws/v2012a/api}EventActionDefinitions" minOccurs="0"/>
 *         &lt;element name="bindActions" type="{http://eclipse.org/stardust/ws/v2012a/api}BindActionDefinitions" minOccurs="0"/>
 *         &lt;element name="unbindActions" type="{http://eclipse.org/stardust/ws/v2012a/api}UnbindActionDefinitions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventHandlerDefinition", propOrder = {
    "rtOid",
    "type",
    "eventActions",
    "bindActions",
    "unbindActions"
})
public class EventHandlerDefinitionXto
    extends ModelElementXto
{

    protected long rtOid;
    protected EventHandlerTypeDefinitionXto type;
    protected EventActionDefinitionsXto eventActions;
    protected BindActionDefinitionsXto bindActions;
    protected UnbindActionDefinitionsXto unbindActions;

    /**
     * Gets the value of the rtOid property.
     * 
     */
    public long getRtOid() {
        return rtOid;
    }

    /**
     * Sets the value of the rtOid property.
     * 
     */
    public void setRtOid(long value) {
        this.rtOid = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link EventHandlerTypeDefinitionXto }
     *     
     */
    public EventHandlerTypeDefinitionXto getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventHandlerTypeDefinitionXto }
     *     
     */
    public void setType(EventHandlerTypeDefinitionXto value) {
        this.type = value;
    }

    /**
     * Gets the value of the eventActions property.
     * 
     * @return
     *     possible object is
     *     {@link EventActionDefinitionsXto }
     *     
     */
    public EventActionDefinitionsXto getEventActions() {
        return eventActions;
    }

    /**
     * Sets the value of the eventActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventActionDefinitionsXto }
     *     
     */
    public void setEventActions(EventActionDefinitionsXto value) {
        this.eventActions = value;
    }

    /**
     * Gets the value of the bindActions property.
     * 
     * @return
     *     possible object is
     *     {@link BindActionDefinitionsXto }
     *     
     */
    public BindActionDefinitionsXto getBindActions() {
        return bindActions;
    }

    /**
     * Sets the value of the bindActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link BindActionDefinitionsXto }
     *     
     */
    public void setBindActions(BindActionDefinitionsXto value) {
        this.bindActions = value;
    }

    /**
     * Gets the value of the unbindActions property.
     * 
     * @return
     *     possible object is
     *     {@link UnbindActionDefinitionsXto }
     *     
     */
    public UnbindActionDefinitionsXto getUnbindActions() {
        return unbindActions;
    }

    /**
     * Sets the value of the unbindActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnbindActionDefinitionsXto }
     *     
     */
    public void setUnbindActions(UnbindActionDefinitionsXto value) {
        this.unbindActions = value;
    }

}
