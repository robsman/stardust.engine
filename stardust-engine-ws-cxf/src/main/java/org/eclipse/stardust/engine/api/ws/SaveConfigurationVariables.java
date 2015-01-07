
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
 *         &lt;element name="configurationVariables" type="{http://eclipse.org/stardust/ws/v2012a/api}ConfigurationVariables"/>
 *         &lt;element name="force" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "configurationVariables",
    "force"
})
@XmlRootElement(name = "saveConfigurationVariables")
public class SaveConfigurationVariables {

    @XmlElement(required = true)
    protected ConfigurationVariablesXto configurationVariables;
    protected boolean force;

    /**
     * Gets the value of the configurationVariables property.
     * 
     * @return
     *     possible object is
     *     {@link ConfigurationVariablesXto }
     *     
     */
    public ConfigurationVariablesXto getConfigurationVariables() {
        return configurationVariables;
    }

    /**
     * Sets the value of the configurationVariables property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConfigurationVariablesXto }
     *     
     */
    public void setConfigurationVariables(ConfigurationVariablesXto value) {
        this.configurationVariables = value;
    }

    /**
     * Gets the value of the force property.
     * 
     */
    public boolean isForce() {
        return force;
    }

    /**
     * Sets the value of the force property.
     * 
     */
    public void setForce(boolean value) {
        this.force = value;
    }

}
