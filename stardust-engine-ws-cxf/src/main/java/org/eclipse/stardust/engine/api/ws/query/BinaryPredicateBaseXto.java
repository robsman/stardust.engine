
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für BinaryPredicateBase complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BinaryPredicateBase">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="lhsVariable" type="{http://eclipse.org/stardust/ws/v2012a/api/query}VariableReference" minOccurs="0"/>
 *         &lt;element name="lhsAttribute" type="{http://eclipse.org/stardust/ws/v2012a/api/query}AttributeReference" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryPredicateBase", propOrder = {
    "lhsVariable",
    "lhsAttribute"
})
@XmlSeeAlso({
    BinaryPredicateXto.class,
    BinaryListValuedPredicateXto.class
})
public abstract class BinaryPredicateBaseXto
    extends PredicateBaseXto
{

    protected VariableReferenceXto lhsVariable;
    protected AttributeReferenceXto lhsAttribute;

    /**
     * Ruft den Wert der lhsVariable-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VariableReferenceXto }
     *     
     */
    public VariableReferenceXto getLhsVariable() {
        return lhsVariable;
    }

    /**
     * Legt den Wert der lhsVariable-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableReferenceXto }
     *     
     */
    public void setLhsVariable(VariableReferenceXto value) {
        this.lhsVariable = value;
    }

    /**
     * Ruft den Wert der lhsAttribute-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public AttributeReferenceXto getLhsAttribute() {
        return lhsAttribute;
    }

    /**
     * Legt den Wert der lhsAttribute-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public void setLhsAttribute(AttributeReferenceXto value) {
        this.lhsAttribute = value;
    }

}
