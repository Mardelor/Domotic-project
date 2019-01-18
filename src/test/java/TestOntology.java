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

        // Paris UTC + 1 en hiver -> reveil +1h (donc 8h)
        updateContext(1, 8);
        String response = JenaEngine.executeQuery(inferedModel, query);
        System.out.print(response);

        // Paris UTC + 2 en ete -> reveil +2h (donc 9h)
        updateContext(6, 9);
        response = JenaEngine.executeQuery(inferedModel, query);
        System.out.print(response);

    }

    /**
     * En été, entre 11h et 16h si la température est >28°, les volets se 
     * ferment pour conserver la fraîcheur à l'intérieur.
     */
    @Test
    public void testScenario2() {
      String query = QHEADER + "SELECT * WHERE {{?Volet rdf:type ns:Volet ; ns:estDansEtat ?Etat}" +
        "UNION" + "{?Thermometre rdf:type ns:Thermomètre ; ns:valeur ?Temperature}}";

      /* T=34° en été à 14h, les volets doivent être fermés */
      updateContext(8, 14, 34);
      String response = JenaEngine.executeQuery(inferedModel, query);
      System.out.print(response);

      /* T=20° en été à 14h, les volets doivent être ouverts */
      updateContext(8, 14, 20);
      response = JenaEngine.executeQuery(inferedModel, query);
      System.out.print(response);
    }

    /**
     * Si quelqu'un est dans la bibliothèque, alors la sono s'allume, sinon
     * elle est éteinte
     */
    @Test
    public void testScenario3() {
      String query = QHEADER + "SELECT * WHERE {{?Hifi rdf:type ns:CHiFi ; ns:estDansEtat ?Etat}" +
        "UNION" + "{?DP rdf:type ns:DetecteurDePresence ; ns:valeur ?Presence}}";

      /* Aucune présence dans la bibliothèque, la chaîne Hifi doit être éteinte */
      JenaEngine.updateValueOfDataTypeProperty(inferedModel, URL, "DP1", "valeur", new Integer(0));
      String response = JenaEngine.executeQuery(inferedModel, query);
      System.out.print(response);

      /* Présence dans la bibliothèque, la chaîne Hifi doit être allumée */
      JenaEngine.updateValueOfDataTypeProperty(inferedModel, URL, "DP1", "valeur", new Integer(1));
      response = JenaEngine.executeQuery(inferedModel, query);
      System.out.print(response);
    }

    @Test
    public void testScenario4() {
        /*
         * Scenario : On eteint lampes, fenetres et volets partout, et l'on éteint la climatisation dans la cuisine
         */
        String query = QHEADER + "SELECT * WHERE {{?clim rdf:type ns:Climatiseur ; ns:estDans ?piece ; ns:estDansEtat ?etat . FILTER(?etat = ns:Eteint)}" +
                "UNION" + "{?volet rdf:type ns:Volet ; ns:estDans ?piece ; ns:estDansEtat ?etat . FILTER(?etat = ns:Ferme)}" +
                "UNION" + "{?fenetre rdf:type ns:Fenetre ; ns:estDans ?piece ; ns:estDansEtat ?etat . FILTER(?etat = ns:Ferme)}" +
                "UNION" + "{?lampe rdf:type ns:Lampe ; ns:estDans ?piece ; ns:estDansEtat ?etat . FILTER(?etat = ns:Eteint)}}";

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

    /**
     * To update month, hour and temperature
     */
    private void updateContext(int month, int hour, int temp) {
        Calendar date = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(2019, month, 18);

        JenaEngine.updateValueOfDataTypeProperty(inferedModel, URL, "Horloge", "date", new XSDDateTime(date));
        JenaEngine.updateValueOfDataTypeProperty(inferedModel, URL, "T2", "valeur", new Integer(temp));
    }
}
