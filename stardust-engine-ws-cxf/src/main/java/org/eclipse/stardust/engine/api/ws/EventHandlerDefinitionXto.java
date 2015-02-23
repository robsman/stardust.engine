
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			A client side view of a workflow event handler.
 * 			
 * 
 * <p>Java-Klasse für EventHandlerDefinition complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der rtOid-Eigenschaft ab.
     * 
     */
    public long getRtOid() {
        return rtOid;
    }

    /**
     * Legt den Wert der rtOid-Eigenschaft fest.
     * 
     */
    public void setRtOid(long value) {
        this.rtOid = value;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
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
     * Legt den Wert der type-Eigenschaft fest.
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
     * Ruft den Wert der eventActions-Eigenschaft ab.
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
     * Legt den Wert der eventActions-Eigenschaft fest.
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
     * Ruft den Wert der bindActions-Eigenschaft ab.
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
     * Legt den Wert der bindActions-Eigenschaft fest.
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
     * Ruft den Wert der unbindActions-Eigenschaft ab.
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
     * Legt den Wert der unbindActions-Eigenschaft fest.
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
