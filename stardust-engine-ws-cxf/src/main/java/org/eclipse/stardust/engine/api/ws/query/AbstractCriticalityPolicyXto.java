
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für AbstractCriticalityPolicy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractCriticalityPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="lowPriorityCriticalPct" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="normalPriorityCriticalPct" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="highPriorityCriticalPct" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractCriticalityPolicy", propOrder = {
    "lowPriorityCriticalPct",
    "normalPriorityCriticalPct",
    "highPriorityCriticalPct"
})
@XmlSeeAlso({
    AbstractCriticalDurationPolicyXto.class
})
public abstract class AbstractCriticalityPolicyXto
    extends EvaluationPolicyXto
{

    protected float lowPriorityCriticalPct;
    protected float normalPriorityCriticalPct;
    protected float highPriorityCriticalPct;

    /**
     * Ruft den Wert der lowPriorityCriticalPct-Eigenschaft ab.
     * 
     */
    public float getLowPriorityCriticalPct() {
        return lowPriorityCriticalPct;
    }

    /**
     * Legt den Wert der lowPriorityCriticalPct-Eigenschaft fest.
     * 
     */
    public void setLowPriorityCriticalPct(float value) {
        this.lowPriorityCriticalPct = value;
    }

    /**
     * Ruft den Wert der normalPriorityCriticalPct-Eigenschaft ab.
     * 
     */
    public float getNormalPriorityCriticalPct() {
        return normalPriorityCriticalPct;
    }

    /**
     * Legt den Wert der normalPriorityCriticalPct-Eigenschaft fest.
     * 
     */
    public void setNormalPriorityCriticalPct(float value) {
        this.normalPriorityCriticalPct = value;
    }

    /**
     * Ruft den Wert der highPriorityCriticalPct-Eigenschaft ab.
     * 
     */
    public float getHighPriorityCriticalPct() {
        return highPriorityCriticalPct;
    }

    /**
     * Legt den Wert der highPriorityCriticalPct-Eigenschaft fest.
     * 
     */
    public void setHighPriorityCriticalPct(float value) {
        this.highPriorityCriticalPct = value;
    }

}
