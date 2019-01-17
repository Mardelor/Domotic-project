import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.Assert;
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
    }

    @After
    public void tearDown() throws Exception {
        model = null;
        inferedModel = null;
    }

    @Test
    public void testContext() {
        String query = QHEADER + "SELECT ?moment ?saison WHERE {ns:Temps ns:estMoment ?moment . ns:Temps ns:estSaison ?saison}";

        updateContext(1, 8);
        String response = JenaEngine.executeQuery(inferedModel, query);
        System.out.print(response);

        Assert.assertTrue(response.contains("Reveil"));
        Assert.assertTrue(response.contains("Hiver"));

        updateContext(8, 13);
        response = JenaEngine.executeQuery(inferedModel, query);
        System.out.print(response);

        Assert.assertTrue(response.contains("Midi"));
        Assert.assertTrue(response.contains("Ete"));
    }

    @Test
    public void testScenario1() {
        String query = QHEADER + "SELECT * WHERE {{?clim rdf:type ns:Climatiseur ; ns:estDans ?piece ; ns:temperatureCible ?temp . FILTER(?temp = 21)}" +
                "UNION" + "{?volet rdf:type ns:Volet ; ns:estDans ?piece ; ns:estDansEtat ?etat . FILTER(?etat = ns:Ouvert)}" +
                "UNION" + "{?fenetre rdf:type ns:Fenetre ; ns:estDans ?piece ; ns:estDansEtat ?etat . ?piece rdf:type ns:Chambre}}";

        updateContext(1, 8);
        String response = JenaEngine.executeQuery(inferedModel, query);
        System.out.print(response);

        updateContext(6, 9);
        response = JenaEngine.executeQuery(inferedModel, query);
        System.out.print(response);

    }

    @Test
    public void testScenario2() {

    }

    @Test
    public void testScenario3() {

    }

    @Test
    public void testScenario4() {
        /*
         * Scenario : On eteint lampes, fenetres et volets partout, et l'on éteint la climatisation dans la cuisine
         */
        String query = QHEADER + "SELECT * WHERE {{?clim rdf:type ns:Climatiseur ; ns:estDans ?piece ; ns:estDansEtat ?etat . FILTER(?etat = ns:Eteint)}" +
                "UNION" + "{?volet rdf:type ns:Volet ; ns:estDans ?piece ; ns:estDansEtat ?etat . FILTER(?etat = ns:Ferme)}" +
                "UNION" + "{?fenetre rdf:type ns:Fenetre ; ns:estDans ?piece ; ns:estDansEtat ?etat . FILTER(?etat = ns:Ferme)}" +
                "UNION" + "{?lampe rdf:type ns:Lampe ; ns:estDans ?piece ; ns:estDansEtat ?etat . FILTER(?etat = ns:Ferme)}}";

        updateContext(4, 2);
        String response = JenaEngine.executeQuery(inferedModel, query);
        System.out.print(response);
    }

    /**
     * To update month and hour
     */
    private void updateContext(int month, int hour) {
        Calendar date = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(2019, month, 18);

        JenaEngine.updateValueOfDataTypeProperty(inferedModel, URL, "Horloge", "date", new XSDDateTime(date));
    }
}
