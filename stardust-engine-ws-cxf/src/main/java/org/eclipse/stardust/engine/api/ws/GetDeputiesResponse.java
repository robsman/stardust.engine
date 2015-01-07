
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
 *         &lt;element name="deputies" type="{http://eclipse.org/stardust/ws/v2012a/api}Deputies"/>
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
    "deputies"
})
@XmlRootElement(name = "getDeputiesResponse")
public class GetDeputiesResponse {

    @XmlElement(required = true, nillable = true)
    protected DeputiesXto deputies;

    /**
     * Gets the value of the deputies property.
     * 
     * @return
     *     possible object is
     *     {@link DeputiesXto }
     *     
     */
    public DeputiesXto getDeputies() {
        return deputies;
    }

    /**
     * Sets the value of the deputies property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeputiesXto }
     *     
     */
    public void setDeputies(DeputiesXto value) {
        this.deputies = value;
    }

}
