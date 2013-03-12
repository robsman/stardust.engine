
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
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
 *         &lt;element name="modelIds" type="{http://eclipse.org/stardust/ws/v2012a/api}StringList" minOccurs="0"/>
 *         &lt;element name="modelXml" type="{http://eclipse.org/stardust/ws/v2012a/api}XmlValue"/>
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
    "modelIds",
    "modelXml"
})
@XmlRootElement(name = "getConfigurationVariables")
public class GetConfigurationVariables {

    @XmlElementRef(name = "modelIds", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<StringListXto> modelIds;
    @XmlElement(required = true, nillable = true)
    protected XmlValueXto modelXml;

    /**
     * Gets the value of the modelIds property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringListXto }{@code >}
     *     
     */
    public JAXBElement<StringListXto> getModelIds() {
        return modelIds;
    }

    /**
     * Sets the value of the modelIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringListXto }{@code >}
     *     
     */
    public void setModelIds(JAXBElement<StringListXto> value) {
        this.modelIds = ((JAXBElement<StringListXto> ) value);
    }

    /**
     * Gets the value of the modelXml property.
     * 
     * @return
     *     possible object is
     *     {@link XmlValueXto }
     *     
     */
    public XmlValueXto getModelXml() {
        return modelXml;
    }

    /**
     * Sets the value of the modelXml property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlValueXto }
     *     
     */
    public void setModelXml(XmlValueXto value) {
        this.modelXml = value;
    }

}
