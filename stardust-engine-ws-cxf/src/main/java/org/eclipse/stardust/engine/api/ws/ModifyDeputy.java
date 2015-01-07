
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
 *         &lt;element name="user" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
 *         &lt;element name="deputyUser" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
 *         &lt;element name="options" type="{http://eclipse.org/stardust/ws/v2012a/api}DeputyOptions"/>
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
    "user",
    "deputyUser",
    "options"
})
@XmlRootElement(name = "modifyDeputy")
public class ModifyDeputy {

    @XmlElement(required = true, nillable = true)
    protected UserInfoXto user;
    @XmlElement(required = true, nillable = true)
    protected UserInfoXto deputyUser;
    @XmlElement(required = true, nillable = true)
    protected DeputyOptionsXto options;

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link UserInfoXto }
     *     
     */
    public UserInfoXto getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInfoXto }
     *     
     */
    public void setUser(UserInfoXto value) {
        this.user = value;
    }

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

    /**
     * Gets the value of the options property.
     * 
     * @return
     *     possible object is
     *     {@link DeputyOptionsXto }
     *     
     */
    public DeputyOptionsXto getOptions() {
        return options;
    }

    /**
     * Sets the value of the options property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeputyOptionsXto }
     *     
     */
    public void setOptions(DeputyOptionsXto value) {
        this.options = value;
    }

}
