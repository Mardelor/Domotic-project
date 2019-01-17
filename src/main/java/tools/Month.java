package tools;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.Util;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;


/**
 * Permet de récupérer le mois à partir de la date
 * @see BaseBuiltin
 */
public class Month extends BaseBuiltin {
    @Override
    public int getArgLength() {
        return 2;
    }

    @Override
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
        checkArgs(length, context);
        BindingEnvironment env = context.getEnv();
        Node node = getArg(0, args, context);
        if (node.isLiteral()) {
            Object value = node.getLiteralValue();
            if (value instanceof XSDDateTime) {
                Node month = Util.makeIntNode(((XSDDateTime) value).getMonths());
                return env.bind(args[1], month);
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "dateMonth";
    }
}
