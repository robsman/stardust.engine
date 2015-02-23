
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	      A client side view of a workflow event action. Each modeled event action contains a
 *           specific set of attributes, depending on the type of the event action.
 * 	      
 * 
 * <p>Java-Klasse für EventActionDefinition complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="EventActionDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="rtOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="type" type="{http://eclipse.org/stardust/ws/v2012a/api}EventActionTypeDefinition" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventActionDefinition", propOrder = {
    "rtOid",
    "type"
})
public class EventActionDefinitionXto
    extends ModelElementXto
{

    protected long rtOid;
    protected EventActionTypeDefinitionXto type;

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
     *     {@link EventActionTypeDefinitionXto }
     *     
     */
    public EventActionTypeDefinitionXto getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EventActionTypeDefinitionXto }
     *     
     */
    public void setType(EventActionTypeDefinitionXto value) {
        this.type = value;
    }

}
