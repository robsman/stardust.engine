
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Reports information about the last migration batch and the migration jobs in general.
 * 			
 * 
 * <p>Java-Klasse für RepositoryMigrationReport complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RepositoryMigrationReport">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="totalCount" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="resourcesDone" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="targetRepositoryVersion" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="currentRepositoryVersion" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="currentMigrationJob" type="{http://eclipse.org/stardust/ws/v2012a/api}RepositoryMigrationJobInfo"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryMigrationReport", propOrder = {
    "totalCount",
    "resourcesDone",
    "targetRepositoryVersion",
    "currentRepositoryVersion",
    "currentMigrationJob"
})
public class RepositoryMigrationReportXto {

    protected long totalCount;
    protected long resourcesDone;
    protected int targetRepositoryVersion;
    protected int currentRepositoryVersion;
    @XmlElement(required = true)
    protected RepositoryMigrationJobInfoXto currentMigrationJob;

    /**
     * Ruft den Wert der totalCount-Eigenschaft ab.
     * 
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Legt den Wert der totalCount-Eigenschaft fest.
     * 
     */
    public void setTotalCount(long value) {
        this.totalCount = value;
    }

    /**
     * Ruft den Wert der resourcesDone-Eigenschaft ab.
     * 
     */
    public long getResourcesDone() {
        return resourcesDone;
    }

    /**
     * Legt den Wert der resourcesDone-Eigenschaft fest.
     * 
     */
    public void setResourcesDone(long value) {
        this.resourcesDone = value;
    }

    /**
     * Ruft den Wert der targetRepositoryVersion-Eigenschaft ab.
     * 
     */
    public int getTargetRepositoryVersion() {
        return targetRepositoryVersion;
    }

    /**
     * Legt den Wert der targetRepositoryVersion-Eigenschaft fest.
     * 
     */
    public void setTargetRepositoryVersion(int value) {
        this.targetRepositoryVersion = value;
    }

    /**
     * Ruft den Wert der currentRepositoryVersion-Eigenschaft ab.
     * 
     */
    public int getCurrentRepositoryVersion() {
        return currentRepositoryVersion;
    }

    /**
     * Legt den Wert der currentRepositoryVersion-Eigenschaft fest.
     * 
     */
    public void setCurrentRepositoryVersion(int value) {
        this.currentRepositoryVersion = value;
    }

    /**
     * Ruft den Wert der currentMigrationJob-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RepositoryMigrationJobInfoXto }
     *     
     */
    public RepositoryMigrationJobInfoXto getCurrentMigrationJob() {
        return currentMigrationJob;
    }

    /**
     * Legt den Wert der currentMigrationJob-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RepositoryMigrationJobInfoXto }
     *     
     */
    public void setCurrentMigrationJob(RepositoryMigrationJobInfoXto value) {
        this.currentMigrationJob = value;
    }

}
