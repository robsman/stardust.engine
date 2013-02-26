
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          Evaluation Policy for specifying details level of returned users.
 *          
 * 
 * <p>Java class for UserDetailsPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserDetailsPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="level" type="{http://eclipse.org/stardust/ws/v2012a/api/query}UserDetailsLevel"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserDetailsPolicy", propOrder = {
    "level"
})
public class UserDetailsPolicyXto
    extends EvaluationPolicyXto
{

    @XmlElement(required = true)
    protected UserDetailsLevelXto level;

    /**
     * Gets the value of the level property.
     * 
     * @return
     *     possible object is
     *     {@link UserDetailsLevelXto }
     *     
     */
    public UserDetailsLevelXto getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserDetailsLevelXto }
     *     
     */
    public void setLevel(UserDetailsLevelXto value) {
        this.level = value;
    }

}
