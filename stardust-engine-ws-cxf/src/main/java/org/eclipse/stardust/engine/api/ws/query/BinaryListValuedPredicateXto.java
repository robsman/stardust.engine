
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für BinaryListValuedPredicate complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BinaryListValuedPredicate">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}BinaryPredicateBase">
 *       &lt;sequence>
 *         &lt;element name="rhsValues" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ValuesLiteral" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryListValuedPredicate", propOrder = {
    "rhsValues"
})
@XmlSeeAlso({
    NotInListPredicateXto.class,
    InListPredicateXto.class,
    NotAnyOfPredicateXto.class,
    BetweenPredicateXto.class
})
public abstract class BinaryListValuedPredicateXto
    extends BinaryPredicateBaseXto
{

    protected ValuesLiteralXto rhsValues;

    /**
     * Ruft den Wert der rhsValues-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ValuesLiteralXto }
     *     
     */
    public ValuesLiteralXto getRhsValues() {
        return rhsValues;
    }

    /**
     * Legt den Wert der rhsValues-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ValuesLiteralXto }
     *     
     */
    public void setRhsValues(ValuesLiteralXto value) {
        this.rhsValues = value;
    }

}
