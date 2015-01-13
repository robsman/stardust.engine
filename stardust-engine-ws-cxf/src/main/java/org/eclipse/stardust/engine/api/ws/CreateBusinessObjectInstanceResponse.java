
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
 *         &lt;element name="businessObject" type="{http://eclipse.org/stardust/ws/v2012a/api}BusinessObject"/>
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
    "businessObject"
})
@XmlRootElement(name = "createBusinessObjectInstanceResponse")
public class CreateBusinessObjectInstanceResponse {

    @XmlElement(required = true, nillable = true)
    protected BusinessObjectXto businessObject;

    /**
     * Gets the value of the businessObject property.
     * 
     * @return
     *     possible object is
     *     {@link BusinessObjectXto }
     *     
     */
    public BusinessObjectXto getBusinessObject() {
        return businessObject;
    }

    /**
     * Sets the value of the businessObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link BusinessObjectXto }
     *     
     */
    public void setBusinessObject(BusinessObjectXto value) {
        this.businessObject = value;
    }

}
