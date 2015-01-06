
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BinaryPredicate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BinaryPredicate">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}BinaryPredicateBase">
 *       &lt;sequence>
 *         &lt;element name="rhsValue" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ValueLiteral" minOccurs="0"/>
 *         &lt;element name="rhsAttribute" type="{http://eclipse.org/stardust/ws/v2012a/api/query}AttributeReference" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryPredicate", propOrder = {
    "rhsValue",
    "rhsAttribute"
})
@XmlSeeAlso({
    LessThanPredicateXto.class,
    NotEqualPredicateXto.class,
    LessOrEqualPredicateXto.class,
    IsLikePredicateXto.class,
    IsEqualPredicateXto.class,
    GreaterThanPredicateXto.class,
    GreaterOrEqualPredicateXto.class
})
public abstract class BinaryPredicateXto
    extends BinaryPredicateBaseXto
{

    protected ValueLiteralXto rhsValue;
    protected AttributeReferenceXto rhsAttribute;

    /**
     * Gets the value of the rhsValue property.
     * 
     * @return
     *     possible object is
     *     {@link ValueLiteralXto }
     *     
     */
    public ValueLiteralXto getRhsValue() {
        return rhsValue;
    }

    /**
     * Sets the value of the rhsValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueLiteralXto }
     *     
     */
    public void setRhsValue(ValueLiteralXto value) {
        this.rhsValue = value;
    }

    /**
     * Gets the value of the rhsAttribute property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public AttributeReferenceXto getRhsAttribute() {
        return rhsAttribute;
    }

    /**
     * Sets the value of the rhsAttribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public void setRhsAttribute(AttributeReferenceXto value) {
        this.rhsAttribute = value;
    }

}
