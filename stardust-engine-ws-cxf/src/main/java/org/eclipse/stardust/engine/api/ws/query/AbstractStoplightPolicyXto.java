
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AbstractStoplightPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the yellowPct property.
     * 
     */
    public float getYellowPct() {
        return yellowPct;
    }

    /**
     * Sets the value of the yellowPct property.
     * 
     */
    public void setYellowPct(float value) {
        this.yellowPct = value;
    }

    /**
     * Gets the value of the redPct property.
     * 
     */
    public float getRedPct() {
        return redPct;
    }

    /**
     * Sets the value of the redPct property.
     * 
     */
    public void setRedPct(float value) {
        this.redPct = value;
    }

}
