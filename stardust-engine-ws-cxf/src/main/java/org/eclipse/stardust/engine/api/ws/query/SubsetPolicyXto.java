
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          Evaluation Policy for specifying retrieval of only a subset of found data.
 *          
 * 
 * <p>Java-Klasse für SubsetPolicy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der evaluateTotalCount-Eigenschaft ab.
     * 
     */
    public boolean isEvaluateTotalCount() {
        return evaluateTotalCount;
    }

    /**
     * Legt den Wert der evaluateTotalCount-Eigenschaft fest.
     * 
     */
    public void setEvaluateTotalCount(boolean value) {
        this.evaluateTotalCount = value;
    }

    /**
     * Ruft den Wert der maxSize-Eigenschaft ab.
     * 
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Legt den Wert der maxSize-Eigenschaft fest.
     * 
     */
    public void setMaxSize(int value) {
        this.maxSize = value;
    }

    /**
     * Ruft den Wert der skippedEntries-Eigenschaft ab.
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
     * Legt den Wert der skippedEntries-Eigenschaft fest.
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
