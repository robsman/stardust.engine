
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
 *         &lt;element name="accessControlPolicies" type="{http://eclipse.org/stardust/ws/v2012a/api}AccessControlPolicies"/>
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
    "accessControlPolicies"
})
@XmlRootElement(name = "getPoliciesResponse")
public class GetPoliciesResponse {

    @XmlElement(required = true, nillable = true)
    protected AccessControlPoliciesXto accessControlPolicies;

    /**
     * Gets the value of the accessControlPolicies property.
     * 
     * @return
     *     possible object is
     *     {@link AccessControlPoliciesXto }
     *     
     */
    public AccessControlPoliciesXto getAccessControlPolicies() {
        return accessControlPolicies;
    }

    /**
     * Sets the value of the accessControlPolicies property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessControlPoliciesXto }
     *     
     */
    public void setAccessControlPolicies(AccessControlPoliciesXto value) {
        this.accessControlPolicies = value;
    }

}
