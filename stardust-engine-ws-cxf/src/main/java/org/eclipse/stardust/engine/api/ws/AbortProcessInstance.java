
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
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="abortScope" type="{http://eclipse.org/stardust/ws/v2012a/api}AbortScope" minOccurs="0"/>
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
    "oid",
    "abortScope"
})
@XmlRootElement(name = "abortProcessInstance")
public class AbortProcessInstance {

    protected long oid;
    protected AbortScopeXto abortScope;

    /**
     * Gets the value of the oid property.
     * 
     */
    public long getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     * 
     */
    public void setOid(long value) {
        this.oid = value;
    }

    /**
     * Gets the value of the abortScope property.
     * 
     * @return
     *     possible object is
     *     {@link AbortScopeXto }
     *     
     */
    public AbortScopeXto getAbortScope() {
        return abortScope;
    }

    /**
     * Sets the value of the abortScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link AbortScopeXto }
     *     
     */
    public void setAbortScope(AbortScopeXto value) {
        this.abortScope = value;
    }

}
