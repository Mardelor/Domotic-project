import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tools.JenaEngine;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Tests de l'ontologie domotic
 */
public class TestOntology {

    /**
     * Ontologies
     */
    private Model model;
    private Model inferedModel;

    /**
     * Chaînes de caractères
     */
    public static final String URL          = "http://www.semanticweb.org/rem/ontologies/2019/0/domotic#";
    public static final String DATA_FILE    = "data/domotic.owl";
    public static final String RULES_FILE   = "data/rules.txt";
    public static final String QHEADER      = "PREFIX ns:  <http://www.semanticweb.org/rem/ontologies/2019/0/domotic#>"
                                            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                                            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                                            + "PREFIX fn:  <http://www.w3.org/2005/xpath-functions#>"
                                            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>";
    @Before
    public void setUp() throws Exception {
        model = JenaEngine.readModel(DATA_FILE);
        inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, RULES_FILE);

        Calendar date = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
        date.set(Calendar.HOUR_OF_DAY, 8);

        JenaEngine.updateValueOfDataTypeProperty(inferedModel, URL, "Horloge", "date", new XSDDateTime(date));
    }

    @After
    public void tearDown() throws Exception {
        model = null;
        inferedModel = null;
    }

    @Test
    public void a() {
        System.out.print(JenaEngine.executeQuery(inferedModel, QHEADER + "SELECT ?piece ?nom WHERE {"
                + " ?piece rdf:type ns:Pièce. ?piece ns:nom ?nom}"));
    }
}
