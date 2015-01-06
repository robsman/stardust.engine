
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eventBinding" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityEventBinding"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "eventBinding"
})
@XmlRootElement(name = "createActivityEventBindingResponse")
public class CreateActivityEventBindingResponse {

    @XmlElement(required = true, nillable = true)
    protected ActivityEventBindingXto eventBinding;

    /**
     * Gets the value of the eventBinding property.
     * 
     * @return
     *     possible object is
     *     {@link ActivityEventBindingXto }
     *     
     */
    public ActivityEventBindingXto getEventBinding() {
        return eventBinding;
    }

    /**
     * Sets the value of the eventBinding property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityEventBindingXto }
     *     
     */
    public void setEventBinding(ActivityEventBindingXto value) {
        this.eventBinding = value;
    }

}
