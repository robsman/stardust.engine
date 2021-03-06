
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
 *         &lt;element name="modifiedUser" type="{http://eclipse.org/stardust/ws/v2012a/api}User"/>
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
    "modifiedUser"
})
@XmlRootElement(name = "modifyUserResponse")
public class ModifyUserResponse {

    @XmlElement(required = true, nillable = true)
    protected UserXto modifiedUser;

    /**
     * Gets the value of the modifiedUser property.
     * 
     * @return
     *     possible object is
     *     {@link UserXto }
     *     
     */
    public UserXto getModifiedUser() {
        return modifiedUser;
    }

    /**
     * Sets the value of the modifiedUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserXto }
     *     
     */
    public void setModifiedUser(UserXto value) {
        this.modifiedUser = value;
    }

}
