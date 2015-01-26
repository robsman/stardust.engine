
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          Evaluation policy affecting query execution in a multi-version model environment.
 *          Can be used to restrict query evaluation involving model elements to only consider the
 *          currently active model version.
 *          
 * 
 * <p>Java-Klasse für ModelVersionPolicy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ModelVersionPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="restrictedToActiveModel" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelVersionPolicy", propOrder = {
    "restrictedToActiveModel"
})
public class ModelVersionPolicyXto
    extends EvaluationPolicyXto
{

    @XmlElement(defaultValue = "false")
    protected boolean restrictedToActiveModel;

    /**
     * Ruft den Wert der restrictedToActiveModel-Eigenschaft ab.
     * 
     */
    public boolean isRestrictedToActiveModel() {
        return restrictedToActiveModel;
    }

    /**
     * Legt den Wert der restrictedToActiveModel-Eigenschaft fest.
     * 
     */
    public void setRestrictedToActiveModel(boolean value) {
        this.restrictedToActiveModel = value;
    }

}
