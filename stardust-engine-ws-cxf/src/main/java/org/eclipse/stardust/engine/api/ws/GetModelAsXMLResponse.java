
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
 *         &lt;element name="xml" type="{http://eclipse.org/stardust/ws/v2012a/api}XmlValue"/>
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
    "xml"
})
@XmlRootElement(name = "getModelAsXMLResponse")
public class GetModelAsXMLResponse {

    @XmlElement(required = true, nillable = true)
    protected XmlValueXto xml;

    /**
     * Gets the value of the xml property.
     * 
     * @return
     *     possible object is
     *     {@link XmlValueXto }
     *     
     */
    public XmlValueXto getXml() {
        return xml;
    }

    /**
     * Sets the value of the xml property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlValueXto }
     *     
     */
    public void setXml(XmlValueXto value) {
        this.xml = value;
    }

}
