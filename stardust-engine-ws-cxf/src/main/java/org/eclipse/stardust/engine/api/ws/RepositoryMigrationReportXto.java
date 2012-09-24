/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

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
 * <p>Java class for RepositoryMigrationReport complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the totalCount property.
     * 
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the value of the totalCount property.
     * 
     */
    public void setTotalCount(long value) {
        this.totalCount = value;
    }

    /**
     * Gets the value of the resourcesDone property.
     * 
     */
    public long getResourcesDone() {
        return resourcesDone;
    }

    /**
     * Sets the value of the resourcesDone property.
     * 
     */
    public void setResourcesDone(long value) {
        this.resourcesDone = value;
    }

    /**
     * Gets the value of the targetRepositoryVersion property.
     * 
     */
    public int getTargetRepositoryVersion() {
        return targetRepositoryVersion;
    }

    /**
     * Sets the value of the targetRepositoryVersion property.
     * 
     */
    public void setTargetRepositoryVersion(int value) {
        this.targetRepositoryVersion = value;
    }

    /**
     * Gets the value of the currentRepositoryVersion property.
     * 
     */
    public int getCurrentRepositoryVersion() {
        return currentRepositoryVersion;
    }

    /**
     * Sets the value of the currentRepositoryVersion property.
     * 
     */
    public void setCurrentRepositoryVersion(int value) {
        this.currentRepositoryVersion = value;
    }

    /**
     * Gets the value of the currentMigrationJob property.
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
     * Sets the value of the currentMigrationJob property.
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
