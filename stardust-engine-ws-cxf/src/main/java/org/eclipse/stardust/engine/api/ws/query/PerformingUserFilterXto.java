
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Restricts the resulting items to the ones currently performed by the specified user.
 *         (The currently logged in user is mapped to userOid = -1 for this filter.)
 *         
 * 
 * <p>Java class for PerformingUserFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PerformingUserFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="userOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PerformingUserFilter", propOrder = {
    "userOid"
})
public class PerformingUserFilterXto
    extends PredicateBaseXto
{

    protected long userOid;

    /**
     * Gets the value of the userOid property.
     * 
     */
    public long getUserOid() {
        return userOid;
    }

    /**
     * Sets the value of the userOid property.
     * 
     */
    public void setUserOid(long value) {
        this.userOid = value;
    }

}
