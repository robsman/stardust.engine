
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
 *         &lt;element name="modifiedUserGroup" type="{http://eclipse.org/stardust/ws/v2012a/api}UserGroup"/>
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
    "modifiedUserGroup"
})
@XmlRootElement(name = "modifyUserGroupResponse")
public class ModifyUserGroupResponse {

    @XmlElement(required = true, nillable = true)
    protected UserGroupXto modifiedUserGroup;

    /**
     * Gets the value of the modifiedUserGroup property.
     * 
     * @return
     *     possible object is
     *     {@link UserGroupXto }
     *     
     */
    public UserGroupXto getModifiedUserGroup() {
        return modifiedUserGroup;
    }

    /**
     * Sets the value of the modifiedUserGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserGroupXto }
     *     
     */
    public void setModifiedUserGroup(UserGroupXto value) {
        this.modifiedUserGroup = value;
    }

}
