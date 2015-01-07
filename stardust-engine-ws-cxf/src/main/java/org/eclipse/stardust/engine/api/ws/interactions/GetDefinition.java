
package org.eclipse.stardust.engine.api.ws.interactions;

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
 *         &lt;element name="interactionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "interactionId"
})
@XmlRootElement(name = "getDefinition")
public class GetDefinition {

    @XmlElement(required = true, nillable = true)
    protected String interactionId;

    /**
     * Gets the value of the interactionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInteractionId() {
        return interactionId;
    }

    /**
     * Sets the value of the interactionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInteractionId(String value) {
        this.interactionId = value;
    }

}
