
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="internalAuthorization" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "internalAuthorization"
})
@XmlRootElement(name = "isInternalAuthorizationResponse")
public class IsInternalAuthorizationResponse {

    protected boolean internalAuthorization;

    /**
     * Gets the value of the internalAuthorization property.
     * 
     */
    public boolean isInternalAuthorization() {
        return internalAuthorization;
    }

    /**
     * Sets the value of the internalAuthorization property.
     * 
     */
    public void setInternalAuthorization(boolean value) {
        this.internalAuthorization = value;
    }

}
