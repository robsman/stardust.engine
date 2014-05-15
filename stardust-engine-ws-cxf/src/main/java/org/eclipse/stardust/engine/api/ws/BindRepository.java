
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
 *         &lt;element name="repositoryConfiguration" type="{http://eclipse.org/stardust/ws/v2012a/api}RepositoryConfiguration"/>
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
    "repositoryConfiguration"
})
@XmlRootElement(name = "bindRepository")
public class BindRepository {

    @XmlElement(required = true)
    protected RepositoryConfigurationXto repositoryConfiguration;

    /**
     * Gets the value of the repositoryConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link RepositoryConfigurationXto }
     *     
     */
    public RepositoryConfigurationXto getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    /**
     * Sets the value of the repositoryConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link RepositoryConfigurationXto }
     *     
     */
    public void setRepositoryConfiguration(RepositoryConfigurationXto value) {
        this.repositoryConfiguration = value;
    }

}