/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * TODO: add type comment.
 * 
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        namespace = "http://www.upc-cablecom.ch/query-engine/config")
public abstract class RuleCollection {
    private List<Attribute> attribute = new ArrayList<Attribute>();
    private List<Reference> reference = new ArrayList<Reference>();
    @XmlElement(
            name = "rule-set", namespace = "http://www.upc-cablecom.ch/query-engine/config")
    private List<RuleSet>   ruleSets  = new ArrayList<RuleSet>();

}