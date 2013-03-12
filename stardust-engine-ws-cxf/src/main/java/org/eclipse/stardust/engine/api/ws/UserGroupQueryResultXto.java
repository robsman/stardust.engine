
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an UserGroupQuery execution.
 * 			
 * 
 * <p>Java class for UserGroupQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserGroupQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="userGroups" type="{http://eclipse.org/stardust/ws/v2012a/api}UserGroups" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserGroupQueryResult", propOrder = {
    "userGroups"
})
public class UserGroupQueryResultXto
    extends QueryResultXto
{

    protected UserGroupsXto userGroups;

    /**
     * Gets the value of the userGroups property.
     * 
     * @return
     *     possible object is
     *     {@link UserGroupsXto }
     *     
     */
    public UserGroupsXto getUserGroups() {
        return userGroups;
    }

    /**
     * Sets the value of the userGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserGroupsXto }
     *     
     */
    public void setUserGroups(UserGroupsXto value) {
        this.userGroups = value;
    }

}
