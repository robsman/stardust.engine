
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          Evaluation Policy for specifying retrieval of only a subset of found data.
 *          
 * 
 * <p>Java class for SubsetPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubsetPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="evaluateTotalCount" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="maxSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="skippedEntries" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubsetPolicy", propOrder = {
    "evaluateTotalCount",
    "maxSize",
    "skippedEntries"
})
public class SubsetPolicyXto
    extends EvaluationPolicyXto
{

    protected boolean evaluateTotalCount;
    protected int maxSize;
    protected Integer skippedEntries;

    /**
     * Gets the value of the evaluateTotalCount property.
     * 
     */
    public boolean isEvaluateTotalCount() {
        return evaluateTotalCount;
    }

    /**
     * Sets the value of the evaluateTotalCount property.
     * 
     */
    public void setEvaluateTotalCount(boolean value) {
        this.evaluateTotalCount = value;
    }

    /**
     * Gets the value of the maxSize property.
     * 
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the value of the maxSize property.
     * 
     */
    public void setMaxSize(int value) {
        this.maxSize = value;
    }

    /**
     * Gets the value of the skippedEntries property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSkippedEntries() {
        return skippedEntries;
    }

    /**
     * Sets the value of the skippedEntries property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSkippedEntries(Integer value) {
        this.skippedEntries = value;
    }

}
