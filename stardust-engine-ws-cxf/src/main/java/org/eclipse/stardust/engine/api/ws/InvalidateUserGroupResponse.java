
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
 *         &lt;element name="userGroup" type="{http://eclipse.org/stardust/ws/v2012a/api}UserGroup"/>
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
    "userGroup"
})
@XmlRootElement(name = "invalidateUserGroupResponse")
public class InvalidateUserGroupResponse {

    @XmlElement(required = true, nillable = true)
    protected UserGroupXto userGroup;

    /**
     * Gets the value of the userGroup property.
     * 
     * @return
     *     possible object is
     *     {@link UserGroupXto }
     *     
     */
    public UserGroupXto getUserGroup() {
        return userGroup;
    }

    /**
     * Sets the value of the userGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserGroupXto }
     *     
     */
    public void setUserGroup(UserGroupXto value) {
        this.userGroup = value;
    }

}
