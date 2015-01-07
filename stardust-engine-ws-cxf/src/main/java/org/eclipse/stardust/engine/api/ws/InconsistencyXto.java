
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			The Inconsistency provides information about a model inconsistency.
 *  			Inconsistencies are of two types: errors and warnings. When an error inconsistency is
 *  			issued, the model is unable to work (models with errors cannot be deployed). A warning
 *  			inconsistency implies that the specific workflow operation may fail.
 * 			
 * 
 * <p>Java class for Inconsistency complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Inconsistency">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sourceElementOid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Inconsistency", propOrder = {
    "message",
    "sourceElementOid"
})
public class InconsistencyXto {

    protected String message;
    protected int sourceElementOid;

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the value of the sourceElementOid property.
     * 
     */
    public int getSourceElementOid() {
        return sourceElementOid;
    }

    /**
     * Sets the value of the sourceElementOid property.
     * 
     */
    public void setSourceElementOid(int value) {
        this.sourceElementOid = value;
    }

}
