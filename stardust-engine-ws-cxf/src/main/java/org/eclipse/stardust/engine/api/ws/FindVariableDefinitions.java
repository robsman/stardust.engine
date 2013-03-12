
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.VariableDefinitionQueryXto;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="variableDefinitionQuery" type="{http://eclipse.org/stardust/ws/v2012a/api/query}VariableDefinitionQuery"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "variableDefinitionQuery"
})
@XmlRootElement(name = "findVariableDefinitions")
public class FindVariableDefinitions {

    @XmlElement(required = true, nillable = true)
    protected VariableDefinitionQueryXto variableDefinitionQuery;

    /**
     * Gets the value of the variableDefinitionQuery property.
     * 
     * @return
     *     possible object is
     *     {@link VariableDefinitionQueryXto }
     *     
     */
    public VariableDefinitionQueryXto getVariableDefinitionQuery() {
        return variableDefinitionQuery;
    }

    /**
     * Sets the value of the variableDefinitionQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableDefinitionQueryXto }
     *     
     */
    public void setVariableDefinitionQuery(VariableDefinitionQueryXto value) {
        this.variableDefinitionQuery = value;
    }

}
