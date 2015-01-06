
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			An access control policy consists of access control entries.
 * 			
 * 
 * <p>Java class for AccessControlPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AccessControlPolicy">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="accessControlEntries" type="{http://eclipse.org/stardust/ws/v2012a/api}AccessControlEntries" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccessControlPolicy", propOrder = {
    "accessControlEntries"
})
public class AccessControlPolicyXto {

    protected AccessControlEntriesXto accessControlEntries;

    /**
     * Gets the value of the accessControlEntries property.
     * 
     * @return
     *     possible object is
     *     {@link AccessControlEntriesXto }
     *     
     */
    public AccessControlEntriesXto getAccessControlEntries() {
        return accessControlEntries;
    }

    /**
     * Sets the value of the accessControlEntries property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessControlEntriesXto }
     *     
     */
    public void setAccessControlEntries(AccessControlEntriesXto value) {
        this.accessControlEntries = value;
    }

}
