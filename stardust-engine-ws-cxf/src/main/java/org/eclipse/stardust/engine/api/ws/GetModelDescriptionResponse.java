
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
 *         &lt;element name="modelDescription" type="{http://eclipse.org/stardust/ws/v2012a/api}ModelDescription"/>
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
    "modelDescription"
})
@XmlRootElement(name = "getModelDescriptionResponse")
public class GetModelDescriptionResponse {

    @XmlElement(required = true, nillable = true)
    protected ModelDescriptionXto modelDescription;

    /**
     * Gets the value of the modelDescription property.
     * 
     * @return
     *     possible object is
     *     {@link ModelDescriptionXto }
     *     
     */
    public ModelDescriptionXto getModelDescription() {
        return modelDescription;
    }

    /**
     * Sets the value of the modelDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelDescriptionXto }
     *     
     */
    public void setModelDescription(ModelDescriptionXto value) {
        this.modelDescription = value;
    }

}
