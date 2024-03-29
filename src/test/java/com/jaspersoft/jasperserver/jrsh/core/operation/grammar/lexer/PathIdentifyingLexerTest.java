package com.jaspersoft.jasperserver.jrsh.core.operation.grammar.lexer;

import org.junit.Assert;
import org.junit.Test;

public class PathIdentifyingLexerTest {

    public static final String OPERATION_OF_THREE_TOKENS = "" +
            "import " +
                "~/Downloads" +
                "/folder" +
                "/New\\ Folder" +
                "/file.zip " +
            "with-include-audit-events ";

    public static final String OPERATION_OF_TWO_TOKENS = "" +
            "import " +
                "~/Downloads" +
                "/folder" +
                "/New\\ Folder" +
                "/My\\ Another\\ Cool\\ Folder" +
                "/file.zip";

    private Lexer lexer = new PathIdentifyingLexer();

    @Test
    public void shouldRecognizePathWithOneSpaceAsSingleToken() {
        // When
        int tokensAmount = lexer.convert(OPERATION_OF_THREE_TOKENS).size();
        // Then
        Assert.assertSame(tokensAmount, 3);
    }

    @Test
    public void shouldRecognizePathWithFourSpacesAsSingleToken() {
        // When
        int tokensAmount = lexer.convert(OPERATION_OF_TWO_TOKENS).size();
        // Then
        Assert.assertSame(tokensAmount, 2);
    }
}