
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="userRealms">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="userRealms" type="{http://eclipse.org/stardust/ws/v2012a/api}UserRealm" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "userRealms"
})
@XmlRootElement(name = "getUserRealmsResponse")
public class GetUserRealmsResponse {

    @XmlElement(required = true, nillable = true)
    protected GetUserRealmsResponse.UserRealmsXto userRealms;

    /**
     * Gets the value of the userRealms property.
     * 
     * @return
     *     possible object is
     *     {@link GetUserRealmsResponse.UserRealmsXto }
     *     
     */
    public GetUserRealmsResponse.UserRealmsXto getUserRealms() {
        return userRealms;
    }

    /**
     * Sets the value of the userRealms property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetUserRealmsResponse.UserRealmsXto }
     *     
     */
    public void setUserRealms(GetUserRealmsResponse.UserRealmsXto value) {
        this.userRealms = value;
    }


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
     *         &lt;element name="userRealms" type="{http://eclipse.org/stardust/ws/v2012a/api}UserRealm" maxOccurs="unbounded" minOccurs="0"/>
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
        "userRealms"
    })
    public static class UserRealmsXto {

        protected List<UserRealmXto> userRealms;

        /**
         * Gets the value of the userRealms property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the userRealms property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getUserRealms().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link UserRealmXto }
         * 
         * 
         */
        public List<UserRealmXto> getUserRealms() {
            if (userRealms == null) {
                userRealms = new ArrayList<UserRealmXto>();
            }
            return this.userRealms;
        }

    }

}
