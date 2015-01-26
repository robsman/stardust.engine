
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für AbstractStoplightPolicy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AbstractStoplightPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="yellowPct" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="redPct" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractStoplightPolicy", propOrder = {
    "yellowPct",
    "redPct"
})
@XmlSeeAlso({
    AbstractStoplightDurationPolicyXto.class,
    AbstractStoplightCostPolicyXto.class
})
public abstract class AbstractStoplightPolicyXto
    extends EvaluationPolicyXto
{

    protected float yellowPct;
    protected float redPct;

    /**
     * Ruft den Wert der yellowPct-Eigenschaft ab.
     * 
     */
    public float getYellowPct() {
        return yellowPct;
    }

    /**
     * Legt den Wert der yellowPct-Eigenschaft fest.
     * 
     */
    public void setYellowPct(float value) {
        this.yellowPct = value;
    }

    /**
     * Ruft den Wert der redPct-Eigenschaft ab.
     * 
     */
    public float getRedPct() {
        return redPct;
    }

    /**
     * Legt den Wert der redPct-Eigenschaft fest.
     * 
     */
    public void setRedPct(float value) {
        this.redPct = value;
    }

}
