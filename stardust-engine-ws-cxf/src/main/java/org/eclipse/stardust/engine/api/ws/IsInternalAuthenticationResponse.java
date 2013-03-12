
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
 *         &lt;element name="internalAuthentication" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "internalAuthentication"
})
@XmlRootElement(name = "isInternalAuthenticationResponse")
public class IsInternalAuthenticationResponse {

    protected boolean internalAuthentication;

    /**
     * Gets the value of the internalAuthentication property.
     * 
     */
    public boolean isInternalAuthentication() {
        return internalAuthentication;
    }

    /**
     * Sets the value of the internalAuthentication property.
     * 
     */
    public void setInternalAuthentication(boolean value) {
        this.internalAuthentication = value;
    }

}
