import com.hp.hpl.jena.rdf.model.Model;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import tools.JenaEngine;

/**
 * Tests de l'ontologie domotic
 */
public class TestOntology {

    /**
     * Ontology
     */
    private static Model model;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        // TODO : charger l'ontologie et les règles
        model = JenaEngine.readModel("data/domotic.owl");
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        // TODO : clean all
    }

    @Test
    public void a() {
        System.out.println("AH");
    }
}
