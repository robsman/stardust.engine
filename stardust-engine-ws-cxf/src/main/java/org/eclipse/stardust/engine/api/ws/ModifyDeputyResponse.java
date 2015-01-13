
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
 *         &lt;element name="deputy" type="{http://eclipse.org/stardust/ws/v2012a/api}Deputy"/>
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
    "deputy"
})
@XmlRootElement(name = "modifyDeputyResponse")
public class ModifyDeputyResponse {

    @XmlElement(required = true, nillable = true)
    protected DeputyXto deputy;

    /**
     * Gets the value of the deputy property.
     * 
     * @return
     *     possible object is
     *     {@link DeputyXto }
     *     
     */
    public DeputyXto getDeputy() {
        return deputy;
    }

    /**
     * Sets the value of the deputy property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeputyXto }
     *     
     */
    public void setDeputy(DeputyXto value) {
        this.deputy = value;
    }

}
