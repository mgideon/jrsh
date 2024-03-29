package com.jaspersoft.jasperserver.jrsh.core.operation.parser;

import com.jaspersoft.jasperserver.jrsh.core.operation.Operation;
import com.jaspersoft.jasperserver.jrsh.core.operation.annotation.Master;
import com.jaspersoft.jasperserver.jrsh.core.operation.annotation.Parameter;
import com.jaspersoft.jasperserver.jrsh.core.operation.annotation.Prefix;
import com.jaspersoft.jasperserver.jrsh.core.operation.annotation.Value;
import com.jaspersoft.jasperserver.jrsh.core.operation.grammar.Grammar;
import com.jaspersoft.jasperserver.jrsh.core.operation.grammar.Rule;
import com.jaspersoft.jasperserver.jrsh.core.operation.grammar.Rule.DefaultRule;
import com.jaspersoft.jasperserver.jrsh.core.operation.grammar.graph.TokenEdge;
import com.jaspersoft.jasperserver.jrsh.core.operation.grammar.graph.TokenEdgeFactory;
import com.jaspersoft.jasperserver.jrsh.core.operation.grammar.token.Token;
import com.jaspersoft.jasperserver.jrsh.core.operation.parser.exception.CannotCreateTokenException;
import com.jaspersoft.jasperserver.jrsh.core.operation.parser.exception.OperationParseException;
import com.jaspersoft.jasperserver.jrsh.core.operation.parser.exception.WrongOperationFormatException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.lang.reflect.Field;
import java.util.*;

/**
 * This class is used for parsing the grammar of operation
 * based on the metadata of that operation.
 * <p/>
 * To parse the operation grammar we build a graph of tokens,
 * from which we obtain all possible paths. Each path forms
 * the rules of grammar. Basically, the grammar is just a
 * set of rules.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@Log4j
public class OperationGrammarParser {

    private static Graph<Token, TokenEdge<Token>> graph;
    private static Map<String, Pair<Token, String[]>> dependencies;
    private static Map<String, RuleGroup> groups;
    private static Token root;

    /**
     * Parse operation grammar
     *
     * @param operation operation instance
     * @return grammar
     * @throws OperationParseException
     */
    public static Grammar parse(final Operation operation) throws OperationParseException {
        graph = new DefaultDirectedGraph<Token, TokenEdge<Token>>(new TokenEdgeFactory());
        dependencies = new HashMap<String, Pair<Token, String[]>>();
        groups = new HashMap<String, RuleGroup>();

        Grammar grammar = new DefaultGrammar();
        Set<Rule> rules = new HashSet<Rule>();
        Class<?> clazz = operation.getClass();
        Master master = clazz.getAnnotation(Master.class);
        //
        // Read annotation metadata and parse it into dependencies
        //
        if (master != null) {
            root = createToken(master.tokenClass(), master.name(), master.name(), true, true);
            if (master.tail()) {
                Rule rule = new DefaultRule();
                rule.addToken(root);
                rules.add(rule);
            }

            dependencies.put(root.getName(), new ImmutablePair<Token, String[]>(root, new String[]{}));
            graph.addVertex(root);

            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                Prefix prefix = field.getAnnotation(Prefix.class);
                Parameter param = field.getAnnotation(Parameter.class);

                if (param != null) {
                    OperationParameter p1 = new OperationParameter();
                    p1.getTokens().add(root);

                    for (String key : groups.keySet()) {
                        groups.get(key).getParameters().add(p1);
                    }
                    boolean isMandatory = param.mandatory();
                    Value[] values = param.values();
                    OperationParameter p2 = new OperationParameter();

                    for (Value v : values) {
                        Token token = createToken(v.tokenClass(), v.tokenAlias(), v.tokenValue(), isMandatory, v.tail());
                        graph.addVertex(token);

                        if (prefix != null) {
                            Token prefixTkn = createToken(prefix.tokenClass(), prefix.value(), prefix.value(), isMandatory, false);
                            dependencies.put(prefixTkn.getName(), new ImmutablePair<Token, String[]>(prefixTkn, param.dependsOn()));
                            dependencies.put(token.getName(), new ImmutablePair<Token, String[]>(token, new String[]{
                                    prefix.value()
                            }));
                            p2.getTokens().add(prefixTkn);
                            graph.addVertex(prefixTkn);
                        } else {
                            dependencies.put(token.getName(), new ImmutablePair<Token, String[]>(token, param.dependsOn()));
                        }

                        p2.getTokens().add(token);
                        String[] ruleGroups = param.ruleGroups();

                        for (String group : ruleGroups) {
                            RuleGroup ruleGroup = groups.get(group);
                            if (ruleGroup != null) {
                                ruleGroup.getParameters().add(p2);
                            } else {
                                RuleGroup newRuleGroup = new RuleGroup();
                                newRuleGroup.getParameters().add(p1);
                                newRuleGroup.getParameters().add(p2);
                                groups.put(group, newRuleGroup);
                            }
                        }
                    }
                }
            }
        }
        //
        // Build token graph
        //
        buildEdgesInGraph();
        //
        // For tailed @Master
        //
        if (!(graph.vertexSet().size() == 1 && graph.vertexSet().contains(root))) {
            rules.addAll(buildRules());
        }
        //
        // Configure grammar
        //
        if (!rules.isEmpty()) {
            grammar.addRules(rules);
        } else {
            throw new WrongOperationFormatException();
        }

        return grammar;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    protected static Set<Rule> buildRules() {
        //
        // 2500 is a magic number
        //
        val paths = new KShortestPaths<Token, TokenEdge<Token>>(graph, root, 2500);
        Set<Token> vertexes = graph.vertexSet();
        Set<Rule> rules = new LinkedHashSet<Rule>();
        //
        // Use graph token vertexes to get all available
        // paths and convert each path into a rule
        //
        for (Token vertex : vertexes) {
            if (!vertex.equals(root)) {
                if (vertex.isTailOfRule()) {
                    //
                    // Get all available paths in graph
                    //
                    List<GraphPath<Token, TokenEdge<Token>>> ps = paths.getPaths(vertex);
                    for (GraphPath<Token, TokenEdge<Token>> path : ps) {
                        Rule rule = convertPathToRule(path);
                        //
                        // Check if rule is valid
                        //
                        if (isValidRule(rule)) {
                            rules.add(rule);
                        }
                    }
                }
            }
        }
        return rules;
    }

    protected static boolean isValidRule(final Rule rule) {
        List<Token> tokens = rule.getTokens();
        for (RuleGroup group : groups.values()) {
            if (group.getGroupTokens().containsAll(tokens)) {
                Set<OperationParameter> parameters = group.getParameters();
                for (OperationParameter parameter : parameters) {
                    Set<Token> mt = parameter.getOnlyMandatoryTokens();
                    if (mt.size() > 0) {
                        boolean notContains = true;
                        for (Token token : tokens) {
                            if (mt.contains(token)) {
                                notContains = false;
                            }
                        }
                        if (notContains) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    protected static Rule convertPathToRule(GraphPath<Token, TokenEdge<Token>> path) {
        val list = path.getEdgeList();
        Rule rule = new DefaultRule();
        Set<Token> set = new LinkedHashSet<Token>();
        for (TokenEdge<Token> edge : list) {
            set.add(edge.getSource());
            set.add(edge.getTarget());
        }
        for (Token token : set) {
            rule.addToken(token);
        }
        return rule;

    }

    protected static void buildEdgesInGraph() {
        for (val entry : dependencies.entrySet()) {
            val tokenPair = entry.getValue();
            for (String dependencyName : tokenPair.getRight()) {
                val dependency = dependencies.get(dependencyName);
                graph.addEdge(dependency.getLeft(), tokenPair.getLeft());
            }
        }
    }

    protected static Token createToken(Class<? extends Token> tokenType, String tokenName, String tokenValue, boolean mandatory, boolean tail) throws CannotCreateTokenException {
        try {
            return tokenType.getConstructor(String.class, String.class, boolean.class, boolean.class)
                    .newInstance(tokenName, tokenValue, mandatory, tail);
        } catch (Exception e) {
            throw new CannotCreateTokenException(tokenType);
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    protected static class DefaultGrammar implements Grammar {
        private List<Rule> rules = new ArrayList<Rule>();

        public DefaultGrammar(Rule... rules) {
            Collections.addAll(this.rules, rules);
        }

        @Override
        public List<Rule> getRules() {
            return rules;
        }

        @Override
        public void addRule(Rule rule) {
            rules.add(rule);
        }

        @Override
        public void addRules(Collection<Rule> rules) {
            this.rules.addAll(rules);
        }
    }

    @Data
    @EqualsAndHashCode
    protected static class RuleGroup {
        Set<OperationParameter> parameters = new HashSet<OperationParameter>();

        Set<Token> getGroupTokens() {
            Set<Token> set = new HashSet<Token>();
            for (OperationParameter parameter : parameters) {
                set.addAll(parameter.getTokens());
            }
            return set;
        }
    }

    @Data
    @EqualsAndHashCode
    protected static class OperationParameter {
        Set<Token> tokens = new HashSet<Token>();

        Set<Token> getOnlyMandatoryTokens() {
            Set<Token> mandatoryTokens = new HashSet<Token>();
            for (Token token : tokens) {
                if (token.isMandatory()) {
                    mandatoryTokens.add(token);
                }
            }
            return mandatoryTokens;
        }
    }

}
