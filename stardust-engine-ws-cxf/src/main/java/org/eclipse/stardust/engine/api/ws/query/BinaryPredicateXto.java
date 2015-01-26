
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für BinaryPredicate complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der rhsValue-Eigenschaft ab.
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
     * Legt den Wert der rhsValue-Eigenschaft fest.
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
     * Ruft den Wert der rhsAttribute-Eigenschaft ab.
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
     * Legt den Wert der rhsAttribute-Eigenschaft fest.
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
