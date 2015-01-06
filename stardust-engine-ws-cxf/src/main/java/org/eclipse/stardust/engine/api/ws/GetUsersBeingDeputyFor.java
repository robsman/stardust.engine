
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
 *         &lt;element name="deputyUser" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
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
    "deputyUser"
})
@XmlRootElement(name = "getUsersBeingDeputyFor")
public class GetUsersBeingDeputyFor {

    @XmlElement(required = true, nillable = true)
    protected UserInfoXto deputyUser;

    /**
     * Gets the value of the deputyUser property.
     * 
     * @return
     *     possible object is
     *     {@link UserInfoXto }
     *     
     */
    public UserInfoXto getDeputyUser() {
        return deputyUser;
    }

    /**
     * Sets the value of the deputyUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInfoXto }
     *     
     */
    public void setDeputyUser(UserInfoXto value) {
        this.deputyUser = value;
    }

}
