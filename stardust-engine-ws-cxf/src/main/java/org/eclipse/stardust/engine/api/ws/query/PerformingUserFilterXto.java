
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
 * <p>Java-Klasse für PerformingUserFilter complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der userOid-Eigenschaft ab.
     * 
     */
    public long getUserOid() {
        return userOid;
    }

    /**
     * Legt den Wert der userOid-Eigenschaft fest.
     * 
     */
    public void setUserOid(long value) {
        this.userOid = value;
    }

}
