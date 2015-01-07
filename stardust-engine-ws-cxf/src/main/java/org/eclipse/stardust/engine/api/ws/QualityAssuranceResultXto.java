
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				Represents the result of a quality assurance instance resolution
 * 			
 * 
 * <p>Java class for QualityAssuranceResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QualityAssuranceResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="qualityAssuranceCodes" type="{http://eclipse.org/stardust/ws/v2012a/api}QualityAssuranceCode" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="qualityAssuranceState" type="{http://eclipse.org/stardust/ws/v2012a/api}ResultState"/>
 *         &lt;element name="assignFailedInstanceToLastPerformer" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QualityAssuranceResult", propOrder = {
    "qualityAssuranceCodes",
    "qualityAssuranceState",
    "assignFailedInstanceToLastPerformer"
})
public class QualityAssuranceResultXto {

    protected List<QualityAssuranceCodeXto> qualityAssuranceCodes;
    @XmlElement(required = true)
    protected ResultStateXto qualityAssuranceState;
    protected boolean assignFailedInstanceToLastPerformer;

    /**
     * Gets the value of the qualityAssuranceCodes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the qualityAssuranceCodes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQualityAssuranceCodes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QualityAssuranceCodeXto }
     * 
     * 
     */
    public List<QualityAssuranceCodeXto> getQualityAssuranceCodes() {
        if (qualityAssuranceCodes == null) {
            qualityAssuranceCodes = new ArrayList<QualityAssuranceCodeXto>();
        }
        return this.qualityAssuranceCodes;
    }

    /**
     * Gets the value of the qualityAssuranceState property.
     * 
     * @return
     *     possible object is
     *     {@link ResultStateXto }
     *     
     */
    public ResultStateXto getQualityAssuranceState() {
        return qualityAssuranceState;
    }

    /**
     * Sets the value of the qualityAssuranceState property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultStateXto }
     *     
     */
    public void setQualityAssuranceState(ResultStateXto value) {
        this.qualityAssuranceState = value;
    }

    /**
     * Gets the value of the assignFailedInstanceToLastPerformer property.
     * 
     */
    public boolean isAssignFailedInstanceToLastPerformer() {
        return assignFailedInstanceToLastPerformer;
    }

    /**
     * Sets the value of the assignFailedInstanceToLastPerformer property.
     * 
     */
    public void setAssignFailedInstanceToLastPerformer(boolean value) {
        this.assignFailedInstanceToLastPerformer = value;
    }

}
