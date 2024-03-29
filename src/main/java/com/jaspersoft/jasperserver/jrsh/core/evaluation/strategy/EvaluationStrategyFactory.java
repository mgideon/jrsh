package com.jaspersoft.jasperserver.jrsh.core.evaluation.strategy;

import com.jaspersoft.jasperserver.jrsh.core.evaluation.strategy.impl.ScriptEvaluationStrategy;
import com.jaspersoft.jasperserver.jrsh.core.evaluation.strategy.impl.ShellEvaluationStrategy;
import com.jaspersoft.jasperserver.jrsh.core.evaluation.strategy.impl.ToolEvaluationStrategy;
import lombok.extern.log4j.Log4j;

import static com.jaspersoft.jasperserver.jrsh.core.operation.grammar.token.TokenPreconditions.isConnectionString;
import static com.jaspersoft.jasperserver.jrsh.core.operation.grammar.token.TokenPreconditions.isScriptFileName;

/**
 * {@link EvaluationStrategyFactory} used to provide
 * the proper {@link EvaluationStrategy}.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@Log4j
public class EvaluationStrategyFactory {

    /**
     * Factory method which defines the proper operation strategy
     * regarding to the application arguments.
     */
    public static EvaluationStrategy getStrategy(String[] args) {
        EvaluationStrategy strategy = null;
        Class<? extends EvaluationStrategy> strategyType;

        if (args.length == 1 && isConnectionString(args[0])) {
            strategyType = ShellEvaluationStrategy.class;
        } else if (args.length == 2 && "--script".equals(args[0]) && isScriptFileName(args[1])) {
            strategyType = ScriptEvaluationStrategy.class;
        } else {
            strategyType = ToolEvaluationStrategy.class;
        }

        try {
            strategy = strategyType.newInstance();
        } catch (Exception ignored) {
            log.info(String.format("Cannot create strategy instance of [%s]", strategyType));
        }

        return strategy;
    }
}