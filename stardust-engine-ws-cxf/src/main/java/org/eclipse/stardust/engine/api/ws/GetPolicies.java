
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
 *         &lt;element name="resourceId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="policyScope" type="{http://eclipse.org/stardust/ws/v2012a/api}PolicyScope"/>
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
    "resourceId",
    "policyScope"
})
@XmlRootElement(name = "getPolicies")
public class GetPolicies {

    @XmlElement(required = true, nillable = true)
    protected String resourceId;
    @XmlElement(required = true, nillable = true)
    protected PolicyScopeXto policyScope;

    /**
     * Gets the value of the resourceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Sets the value of the resourceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceId(String value) {
        this.resourceId = value;
    }

    /**
     * Gets the value of the policyScope property.
     * 
     * @return
     *     possible object is
     *     {@link PolicyScopeXto }
     *     
     */
    public PolicyScopeXto getPolicyScope() {
        return policyScope;
    }

    /**
     * Sets the value of the policyScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link PolicyScopeXto }
     *     
     */
    public void setPolicyScope(PolicyScopeXto value) {
        this.policyScope = value;
    }

}
