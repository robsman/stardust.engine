
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BpmFault complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BpmFault">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="faultCode" type="{http://eclipse.org/stardust/ws/v2012a/api}BpmFaultCode"/>
 *         &lt;element name="faultId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="faultDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BpmFault", propOrder = {
    "faultCode",
    "faultId",
    "faultDescription"
})
public class BpmFaultXto {

    @XmlElement(required = true)
    protected BpmFaultCodeXto faultCode;
    protected String faultId;
    protected String faultDescription;

    /**
     * Gets the value of the faultCode property.
     * 
     * @return
     *     possible object is
     *     {@link BpmFaultCodeXto }
     *     
     */
    public BpmFaultCodeXto getFaultCode() {
        return faultCode;
    }

    /**
     * Sets the value of the faultCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link BpmFaultCodeXto }
     *     
     */
    public void setFaultCode(BpmFaultCodeXto value) {
        this.faultCode = value;
    }

    /**
     * Gets the value of the faultId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaultId() {
        return faultId;
    }

    /**
     * Sets the value of the faultId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaultId(String value) {
        this.faultId = value;
    }

    /**
     * Gets the value of the faultDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaultDescription() {
        return faultDescription;
    }

    /**
     * Sets the value of the faultDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaultDescription(String value) {
        this.faultDescription = value;
    }

}
