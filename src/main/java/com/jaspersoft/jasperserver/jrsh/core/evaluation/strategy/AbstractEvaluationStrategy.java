package com.jaspersoft.jasperserver.jrsh.core.evaluation.strategy;

import com.jaspersoft.jasperserver.jrsh.core.operation.parser.LL1OperationParser;
import com.jaspersoft.jasperserver.jrsh.core.operation.parser.OperationParser;
import lombok.Data;

/**
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@Data
public abstract class AbstractEvaluationStrategy implements EvaluationStrategy {

    protected OperationParser parser;

    public AbstractEvaluationStrategy() {
        this.parser = new LL1OperationParser();
    }
}
