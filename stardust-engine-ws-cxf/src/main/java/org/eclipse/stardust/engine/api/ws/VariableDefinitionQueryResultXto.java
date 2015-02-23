
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an VariableDefinitionQuery execution.
 * 			
 * 
 * <p>Java-Klasse für VariableDefinitionQueryResult complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="VariableDefinitionQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="variableDefinitions" type="{http://eclipse.org/stardust/ws/v2012a/api}VariableDefinitions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VariableDefinitionQueryResult", propOrder = {
    "variableDefinitions"
})
public class VariableDefinitionQueryResultXto
    extends QueryResultXto
{

    protected VariableDefinitionsXto variableDefinitions;

    /**
     * Ruft den Wert der variableDefinitions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VariableDefinitionsXto }
     *     
     */
    public VariableDefinitionsXto getVariableDefinitions() {
        return variableDefinitions;
    }

    /**
     * Legt den Wert der variableDefinitions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableDefinitionsXto }
     *     
     */
    public void setVariableDefinitions(VariableDefinitionsXto value) {
        this.variableDefinitions = value;
    }

}
