
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Configures the user's private worklist to be partially included in the result.
 *  	    
 * 
 * <p>Java class for UserContribution complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserContribution">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="subsetPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}SubsetPolicy" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="included" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserContribution", propOrder = {
    "subsetPolicy"
})
public class UserContributionXto {

    protected SubsetPolicyXto subsetPolicy;
    @XmlAttribute(name = "included")
    protected Boolean included;

    /**
     * Gets the value of the subsetPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link SubsetPolicyXto }
     *     
     */
    public SubsetPolicyXto getSubsetPolicy() {
        return subsetPolicy;
    }

    /**
     * Sets the value of the subsetPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubsetPolicyXto }
     *     
     */
    public void setSubsetPolicy(SubsetPolicyXto value) {
        this.subsetPolicy = value;
    }

    /**
     * Gets the value of the included property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIncluded() {
        if (included == null) {
            return true;
        } else {
            return included;
        }
    }

    /**
     * Sets the value of the included property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncluded(Boolean value) {
        this.included = value;
    }

}
