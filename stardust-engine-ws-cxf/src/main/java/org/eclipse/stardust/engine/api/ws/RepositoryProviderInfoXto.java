
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				Provides information about a repository provider.
 * 				A repository provider supplies a configurationTemplate to expose
 * 				which information is needed
 * 				to bind a new repository instance.
 * 			
 * 
 * <p>Java-Klasse für RepositoryProviderInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RepositoryProviderInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}RepositoryCapabilities">
 *       &lt;sequence>
 *         &lt;element name="providerId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="providerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="configurationTemplate" type="{http://eclipse.org/stardust/ws/v2012a/api}RepositoryConfiguration" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryProviderInfo", propOrder = {
    "providerId",
    "providerName",
    "configurationTemplate"
})
public class RepositoryProviderInfoXto
    extends RepositoryCapabilitiesXto
{

    @XmlElement(required = true)
    protected String providerId;
    protected String providerName;
    protected RepositoryConfigurationXto configurationTemplate;

    /**
     * Ruft den Wert der providerId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * Legt den Wert der providerId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderId(String value) {
        this.providerId = value;
    }

    /**
     * Ruft den Wert der providerName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Legt den Wert der providerName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderName(String value) {
        this.providerName = value;
    }

    /**
     * Ruft den Wert der configurationTemplate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RepositoryConfigurationXto }
     *     
     */
    public RepositoryConfigurationXto getConfigurationTemplate() {
        return configurationTemplate;
    }

    /**
     * Legt den Wert der configurationTemplate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RepositoryConfigurationXto }
     *     
     */
    public void setConfigurationTemplate(RepositoryConfigurationXto value) {
        this.configurationTemplate = value;
    }

}
