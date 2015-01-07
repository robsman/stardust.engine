
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				The client view of a workflow transition.
 * 			
 * 
 * <p>Java class for Transition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Transition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sourceActivityId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="targetActivityId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="condition" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="conditionType" type="{http://eclipse.org/stardust/ws/v2012a/api}ConditionType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Transition", propOrder = {
    "id",
    "sourceActivityId",
    "targetActivityId",
    "condition",
    "conditionType"
})
public class TransitionXto {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String sourceActivityId;
    @XmlElement(required = true)
    protected String targetActivityId;
    @XmlElement(required = true)
    protected String condition;
    protected ConditionTypeXto conditionType;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the sourceActivityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceActivityId() {
        return sourceActivityId;
    }

    /**
     * Sets the value of the sourceActivityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceActivityId(String value) {
        this.sourceActivityId = value;
    }

    /**
     * Gets the value of the targetActivityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetActivityId() {
        return targetActivityId;
    }

    /**
     * Sets the value of the targetActivityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetActivityId(String value) {
        this.targetActivityId = value;
    }

    /**
     * Gets the value of the condition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Sets the value of the condition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCondition(String value) {
        this.condition = value;
    }

    /**
     * Gets the value of the conditionType property.
     * 
     * @return
     *     possible object is
     *     {@link ConditionTypeXto }
     *     
     */
    public ConditionTypeXto getConditionType() {
        return conditionType;
    }

    /**
     * Sets the value of the conditionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConditionTypeXto }
     *     
     */
    public void setConditionType(ConditionTypeXto value) {
        this.conditionType = value;
    }

}
