
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UnaryPredicate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UnaryPredicate">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="variable" type="{http://eclipse.org/stardust/ws/v2012a/api/query}VariableReference" minOccurs="0"/>
 *         &lt;element name="attribute" type="{http://eclipse.org/stardust/ws/v2012a/api/query}AttributeReference" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnaryPredicate", propOrder = {
    "variable",
    "attribute"
})
@XmlSeeAlso({
    NotNullPredicateXto.class,
    IsNullPredicateXto.class
})
public abstract class UnaryPredicateXto
    extends PredicateBaseXto
{

    protected VariableReferenceXto variable;
    protected AttributeReferenceXto attribute;

    /**
     * Gets the value of the variable property.
     * 
     * @return
     *     possible object is
     *     {@link VariableReferenceXto }
     *     
     */
    public VariableReferenceXto getVariable() {
        return variable;
    }

    /**
     * Sets the value of the variable property.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableReferenceXto }
     *     
     */
    public void setVariable(VariableReferenceXto value) {
        this.variable = value;
    }

    /**
     * Gets the value of the attribute property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public AttributeReferenceXto getAttribute() {
        return attribute;
    }

    /**
     * Sets the value of the attribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public void setAttribute(AttributeReferenceXto value) {
        this.attribute = value;
    }

}
