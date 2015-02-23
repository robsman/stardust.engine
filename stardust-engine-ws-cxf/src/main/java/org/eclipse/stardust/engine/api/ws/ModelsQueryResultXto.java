
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an DeployedModelQuery execution.
 * 			
 * 
 * <p>Java-Klasse für ModelsQueryResult complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ModelsQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="deployedModels" type="{http://eclipse.org/stardust/ws/v2012a/api}Models" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelsQueryResult", propOrder = {
    "deployedModels"
})
public class ModelsQueryResultXto
    extends QueryResultXto
{

    protected ModelsXto deployedModels;

    /**
     * Ruft den Wert der deployedModels-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ModelsXto }
     *     
     */
    public ModelsXto getDeployedModels() {
        return deployedModels;
    }

    /**
     * Legt den Wert der deployedModels-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelsXto }
     *     
     */
    public void setDeployedModels(ModelsXto value) {
        this.deployedModels = value;
    }

}
