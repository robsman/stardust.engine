
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.UserGroupQueryXto;


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
 *         &lt;element name="userGroupQuery" type="{http://eclipse.org/stardust/ws/v2012a/api/query}UserGroupQuery"/>
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
    "userGroupQuery"
})
@XmlRootElement(name = "findUserGroups")
public class FindUserGroups {

    @XmlElement(required = true, nillable = true)
    protected UserGroupQueryXto userGroupQuery;

    /**
     * Gets the value of the userGroupQuery property.
     * 
     * @return
     *     possible object is
     *     {@link UserGroupQueryXto }
     *     
     */
    public UserGroupQueryXto getUserGroupQuery() {
        return userGroupQuery;
    }

    /**
     * Sets the value of the userGroupQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserGroupQueryXto }
     *     
     */
    public void setUserGroupQuery(UserGroupQueryXto value) {
        this.userGroupQuery = value;
    }

}
