
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Restricts the resulting items to users who are logged in.
 *         
 * 
 * <p>Java class for UserStateFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserStateFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;attribute name="loggedInOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserStateFilter")
public class UserStateFilterXto
    extends PredicateBaseXto
{

    @XmlAttribute(name = "loggedInOnly")
    protected Boolean loggedInOnly;

    /**
     * Gets the value of the loggedInOnly property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLoggedInOnly() {
        if (loggedInOnly == null) {
            return true;
        } else {
            return loggedInOnly;
        }
    }

    /**
     * Sets the value of the loggedInOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLoggedInOnly(Boolean value) {
        this.loggedInOnly = value;
    }

}
