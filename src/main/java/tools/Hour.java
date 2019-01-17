package tools;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.Util;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;

public class Hour extends BaseBuiltin {
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
                Node hour = Util.makeIntNode(((XSDDateTime) value).getHours());
                return env.bind(args[1], hour);
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "dateHour";
    }
}
