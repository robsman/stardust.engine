
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
 *         &lt;element name="qualifiedBusinessObjectId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="initialValue" type="{http://eclipse.org/stardust/ws/v2012a/api}Parameter"/>
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
    "qualifiedBusinessObjectId",
    "initialValue"
})
@XmlRootElement(name = "createBusinessObjectInstance")
public class CreateBusinessObjectInstance {

    @XmlElement(required = true)
    protected String qualifiedBusinessObjectId;
    @XmlElement(required = true)
    protected ParameterXto initialValue;

    /**
     * Gets the value of the qualifiedBusinessObjectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQualifiedBusinessObjectId() {
        return qualifiedBusinessObjectId;
    }

    /**
     * Sets the value of the qualifiedBusinessObjectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQualifiedBusinessObjectId(String value) {
        this.qualifiedBusinessObjectId = value;
    }

    /**
     * Gets the value of the initialValue property.
     * 
     * @return
     *     possible object is
     *     {@link ParameterXto }
     *     
     */
    public ParameterXto getInitialValue() {
        return initialValue;
    }

    /**
     * Sets the value of the initialValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterXto }
     *     
     */
    public void setInitialValue(ParameterXto value) {
        this.initialValue = value;
    }

}
