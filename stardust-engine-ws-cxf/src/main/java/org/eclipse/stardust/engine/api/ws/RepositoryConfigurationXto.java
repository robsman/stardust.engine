
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Represents a repository configuration.
 * 			The attributes 'repositoryId' and 'providerId' are keys that are always required for a valid configuration.
 *             Each specific repository configuration may require additional keys for e.g. connection URL, jndiName, etc.
 * 			
 * 
 * <p>Java class for RepositoryConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RepositoryConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attributes" type="{http://eclipse.org/stardust/ws/v2012a/api}Map"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryConfiguration", propOrder = {
    "attributes"
})
public class RepositoryConfigurationXto {

    @XmlElement(required = true)
    protected MapXto attributes;

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link MapXto }
     *     
     */
    public MapXto getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link MapXto }
     *     
     */
    public void setAttributes(MapXto value) {
        this.attributes = value;
    }

}
