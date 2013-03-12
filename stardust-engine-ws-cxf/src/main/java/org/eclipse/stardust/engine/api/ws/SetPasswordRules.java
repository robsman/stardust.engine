
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
 *         &lt;element name="passwordRules" type="{http://eclipse.org/stardust/ws/v2012a/api}PasswordRules"/>
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
    "passwordRules"
})
@XmlRootElement(name = "setPasswordRules")
public class SetPasswordRules {

    @XmlElement(required = true, nillable = true)
    protected PasswordRulesXto passwordRules;

    /**
     * Gets the value of the passwordRules property.
     * 
     * @return
     *     possible object is
     *     {@link PasswordRulesXto }
     *     
     */
    public PasswordRulesXto getPasswordRules() {
        return passwordRules;
    }

    /**
     * Sets the value of the passwordRules property.
     * 
     * @param value
     *     allowed object is
     *     {@link PasswordRulesXto }
     *     
     */
    public void setPasswordRules(PasswordRulesXto value) {
        this.passwordRules = value;
    }

}
