
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
 * <p>Java-Klasse für RepositoryConfiguration complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der attributes-Eigenschaft ab.
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
     * Legt den Wert der attributes-Eigenschaft fest.
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
