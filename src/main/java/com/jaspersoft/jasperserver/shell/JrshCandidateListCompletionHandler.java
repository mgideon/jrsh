package com.jaspersoft.jasperserver.shell;

import jline.console.ConsoleReader;
import jline.console.CursorBuffer;
import jline.console.completer.CompletionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author Alexander Krasnyanskiy
 */
@Deprecated
public class JrshCandidateListCompletionHandler implements CompletionHandler {

    public boolean complete(final ConsoleReader reader, final List<CharSequence> candidates, final int pos) throws IOException {
        CursorBuffer buf = reader.getCursorBuffer();
        if (candidates.size() == 1) {
            CharSequence value = candidates.get(0);
            if (value.equals(buf.toString())) {
                return false;
            }
            setBuffer(reader, value, pos);
            return true;
        } else if (candidates.size() > 1) {
            String value = getUnambiguousCompletions(candidates);
            setBuffer(reader, value, pos);
        }
        printCandidates(reader, candidates);
        reader.drawLine();
        return true;
    }

    public static void setBuffer(final ConsoleReader reader, final CharSequence value, final int offset) throws IOException {
        while ((reader.getCursorBuffer().cursor > offset) && reader.backspace()) {
            // empty
        }
        reader.putString(value);
        reader.setCursorPosition(offset + value.length());
    }

    /**
     * Print out the candidates. If the size of the candidates is greater than the
     * {@link jline.console.ConsoleReader#getAutoprintThreshold}, they prompt with a warning.
     *
     * @param candidates the list of candidates to print
     */
    public static void printCandidates(final ConsoleReader reader, Collection<CharSequence> candidates) throws IOException {
        Set<CharSequence> distinct = new HashSet<>(candidates);
        if (distinct.size() > reader.getAutoprintThreshold()) {
            //noinspection StringConcatenation
            reader.print(Messages.DISPLAY_CANDIDATES.format(candidates.size()));
            reader.flush();
            int c;
            String noOpt = Messages.DISPLAY_CANDIDATES_NO.format();
            String yesOpt = Messages.DISPLAY_CANDIDATES_YES.format();
            char[] allowed = {yesOpt.charAt(0), noOpt.charAt(0)};
            while ((c = reader.readCharacter(allowed)) != -1) {
                String tmp = new String(new char[]{(char) c});
                if (noOpt.startsWith(tmp)) {
                    reader.println();
                    return;
                } else if (yesOpt.startsWith(tmp)) {
                    break;
                } else {
                    reader.beep();
                }
            }
        }
        // copy the values and make them distinct, without otherwise affecting the ordering. Only do it if the sizes differ.
        if (distinct.size() != candidates.size()) {
            Collection<CharSequence> copy = new ArrayList<>();
            for (CharSequence next : candidates) {
                if (!copy.contains(next)) {
                    copy.add(next);
                }
            }

            candidates = copy;
        }

        if (candidates.size() > 1) {
            reader.println();
            reader.printColumns(candidates);
        } else {
            reader.print("\r");
        }
    }

    /**
     * Returns a root that matches all the {@link String} elements of the specified {@link java.util.List},
     * or null if there are no commonalities. For example, if the list contains
     * <i>foobar</i>, <i>foobaz</i>, <i>foobuz</i>, the method will return <i>foob</i>.
     */
    private String getUnambiguousCompletions(final List<CharSequence> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        // convert to an array for speed
        String[] strings = candidates.toArray(new String[candidates.size()]);

        String first = strings[0];
        StringBuilder candidate = new StringBuilder();

        for (int i = 0; i < first.length(); i++) {
            if (startsWith(first.substring(0, i + 1), strings)) {
                candidate.append(first.charAt(i));
            } else {
                break;
            }
        }

        return candidate.toString();
    }

    /**
     * @return true is all the elements of <i>candidates</i> start with <i>starts</i>
     */
    private boolean startsWith(final String starts, final String[] candidates) {
        for (String candidate : candidates) {
            if (!candidate.startsWith(starts)) {
                return false;
            }
        }
        return true;
    }

    private static enum Messages {
        DISPLAY_CANDIDATES,
        DISPLAY_CANDIDATES_YES,
        DISPLAY_CANDIDATES_NO,;
        private static final ResourceBundle bundle = ResourceBundle.getBundle(JrshCandidateListCompletionHandler.class.getName(), Locale.getDefault());

        public String format(final Object... args) {
            if (bundle == null) {
                return "";
            } else {
                return String.format(bundle.getString(name()), args);
            }
        }
    }
}