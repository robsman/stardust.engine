
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
 * <p>Java class for ModelVersionPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the restrictedToActiveModel property.
     * 
     */
    public boolean isRestrictedToActiveModel() {
        return restrictedToActiveModel;
    }

    /**
     * Sets the value of the restrictedToActiveModel property.
     * 
     */
    public void setRestrictedToActiveModel(boolean value) {
        this.restrictedToActiveModel = value;
    }

}
