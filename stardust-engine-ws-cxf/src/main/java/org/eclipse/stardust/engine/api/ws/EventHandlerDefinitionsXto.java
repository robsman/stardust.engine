
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für EventHandlerDefinitions complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="EventHandlerDefinitions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eventHandler" type="{http://eclipse.org/stardust/ws/v2012a/api}EventHandlerDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventHandlerDefinitions", propOrder = {
    "eventHandler"
})
public class EventHandlerDefinitionsXto {

    protected List<EventHandlerDefinitionXto> eventHandler;

    /**
     * Gets the value of the eventHandler property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eventHandler property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEventHandler().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EventHandlerDefinitionXto }
     * 
     * 
     */
    public List<EventHandlerDefinitionXto> getEventHandler() {
        if (eventHandler == null) {
            eventHandler = new ArrayList<EventHandlerDefinitionXto>();
        }
        return this.eventHandler;
    }

}
