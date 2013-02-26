
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="batchSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="evaluateTotalCount" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "batchSize",
    "evaluateTotalCount"
})
@XmlRootElement(name = "migrateRepository")
public class MigrateRepository {

    protected int batchSize;
    protected boolean evaluateTotalCount;

    /**
     * Gets the value of the batchSize property.
     * 
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Sets the value of the batchSize property.
     * 
     */
    public void setBatchSize(int value) {
        this.batchSize = value;
    }

    /**
     * Gets the value of the evaluateTotalCount property.
     * 
     */
    public boolean isEvaluateTotalCount() {
        return evaluateTotalCount;
    }

    /**
     * Sets the value of the evaluateTotalCount property.
     * 
     */
    public void setEvaluateTotalCount(boolean value) {
        this.evaluateTotalCount = value;
    }

}
