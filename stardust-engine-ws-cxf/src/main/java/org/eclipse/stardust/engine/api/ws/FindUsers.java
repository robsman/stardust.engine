
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.UserQueryXto;


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
 *         &lt;element name="userQuery" type="{http://eclipse.org/stardust/ws/v2012a/api/query}UserQuery"/>
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
    "userQuery"
})
@XmlRootElement(name = "findUsers")
public class FindUsers {

    @XmlElement(required = true, nillable = true)
    protected UserQueryXto userQuery;

    /**
     * Gets the value of the userQuery property.
     * 
     * @return
     *     possible object is
     *     {@link UserQueryXto }
     *     
     */
    public UserQueryXto getUserQuery() {
        return userQuery;
    }

    /**
     * Sets the value of the userQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserQueryXto }
     *     
     */
    public void setUserQuery(UserQueryXto value) {
        this.userQuery = value;
    }

}
