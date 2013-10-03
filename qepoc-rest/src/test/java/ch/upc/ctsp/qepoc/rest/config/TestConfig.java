/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.config;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

/**
 * TODO: add type comment.
 * 
 */
public class TestConfig {
    @Test
    public void testGenerateConfigFile() throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(RuleSet.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();

        final RuleSet rootSet = new RuleSet();
        rootSet.getRuleSets().add(new RuleSet());

        marshaller.marshal(rootSet, new File("target/out.xml"));
    }

    @Test
    public void testLoadOfConfig() throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(RuleSet.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        System.out.println(unmarshaller.unmarshal(ClassLoader.getSystemResourceAsStream("rules.xml")));
    }
}
