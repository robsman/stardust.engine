
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
 *         &lt;element name="repositoryProviderInfos" type="{http://eclipse.org/stardust/ws/v2012a/api}RepositoryProviderInfos"/>
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
    "repositoryProviderInfos"
})
@XmlRootElement(name = "getRepositoryProviderInfosResponse")
public class GetRepositoryProviderInfosResponse {

    @XmlElement(required = true)
    protected RepositoryProviderInfosXto repositoryProviderInfos;

    /**
     * Gets the value of the repositoryProviderInfos property.
     * 
     * @return
     *     possible object is
     *     {@link RepositoryProviderInfosXto }
     *     
     */
    public RepositoryProviderInfosXto getRepositoryProviderInfos() {
        return repositoryProviderInfos;
    }

    /**
     * Sets the value of the repositoryProviderInfos property.
     * 
     * @param value
     *     allowed object is
     *     {@link RepositoryProviderInfosXto }
     *     
     */
    public void setRepositoryProviderInfos(RepositoryProviderInfosXto value) {
        this.repositoryProviderInfos = value;
    }

}
