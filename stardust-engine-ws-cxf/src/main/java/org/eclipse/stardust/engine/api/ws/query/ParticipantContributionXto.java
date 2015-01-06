
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Configures the worklist(s) resulting from the given participant filter to be included in the result.
 *         The size of these worklist contributions can optionally be restricted by the subsetPolicy
 *  	    
 * 
 * <p>Java class for ParticipantContribution complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParticipantContribution">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filter" type="{http://eclipse.org/stardust/ws/v2012a/api/query}PerformingParticipantFilter" minOccurs="0"/>
 *         &lt;element name="subsetPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}SubsetPolicy" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParticipantContribution", propOrder = {
    "filter",
    "subsetPolicy"
})
public class ParticipantContributionXto {

    protected PerformingParticipantFilterXto filter;
    protected SubsetPolicyXto subsetPolicy;

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link PerformingParticipantFilterXto }
     *     
     */
    public PerformingParticipantFilterXto getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerformingParticipantFilterXto }
     *     
     */
    public void setFilter(PerformingParticipantFilterXto value) {
        this.filter = value;
    }

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

}
