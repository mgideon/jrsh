package com.jaspersoft.jasperserver.jrsh.core.completion.impl;

import com.google.common.base.Preconditions;
import jline.console.completer.Completer;
import jline.internal.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.List;

/**
 * This class is used for file name completion.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
public class FileCompleter implements Completer {
    private String root;

    public int complete(String buffer, final int cursor, final List<CharSequence> candidates) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return completeFileForWindows(buffer, candidates);
        } else {
            // OSX/Linux/etc
            return completeFileForUnix(buffer, candidates);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private int completeFileForWindows(String buffer, List<CharSequence> candidates) {
        if (buffer == null) {
            buffer = getRoot();
            candidates.add(buffer);
            return buffer.length();
        }

        String translated = buffer;
        File file = new File(translated);

        File dir = translated.endsWith(separator())
                ? file
                : file.getParentFile();

        File[] entries = (dir == null)
                ? new File[0]
                : dir.listFiles();

        return matchFiles(buffer, translated, entries, candidates);
    }

    private int completeFileForUnix(String buffer, List<CharSequence> candidates) {
        Preconditions.checkNotNull(candidates);

        if (buffer == null)
            buffer = "";

        String translated = buffer;
        File homeDir = getUserHome();

        translated = mapToUserHome(translated, homeDir);
        File file = new File(translated);

        File dir = translated.endsWith(separator())
                ? file
                : file.getParentFile();

        File[] entries = (dir == null)
                ? new File[0]
                : dir.listFiles();

        return matchFiles(buffer, translated, entries, candidates);
    }

    private String mapToUserHome(String translated, File homeDir) {
        if (translated.startsWith("~" + separator())) {
            translated = homeDir.getPath() + translated.substring(1);
        } else if (translated.startsWith("~")) {
            translated = homeDir.getParentFile().getAbsolutePath();
        } else if (!(new File(translated).isAbsolute())) {
            String cwd = getUserDir().getAbsolutePath();
            translated = cwd + separator() + translated;
        }
        return translated;
    }

    protected String separator() {
        return File.separator;
    }

    protected File getUserHome() {
        return Configuration.getUserHome();
    }

    protected File getUserDir() {
        return new File(".");
    }

    protected int matchFiles(String buffer, String translated, File[] files, List<CharSequence> candidates) {

        if (files == null) {
            return -1;
        }

        int matches = 0;

        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                matches++;
            }
        }

        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                CharSequence name;
                if (matches == 1 && file.isDirectory()) {
                    if (SystemUtils.IS_OS_WINDOWS) {
                        name = file.getName() + (separator() + separator());
                    } else {
                        name = file.getName() + separator();
                        name = addBackslashToPath(name.toString());
                    }
                } else {
                    name = file.getName() + " ";
                }
                candidates.add(render(name).toString());
            }
        }

        if (matches == 0 && candidates.isEmpty()) {
            candidates.add("");
            return buffer.length();
        }

        int idx = buffer.lastIndexOf(separator());
        return idx + separator().length();
    }

    protected CharSequence render(final CharSequence name) {
        return name;
    }

    protected String getRoot() {
        // Path root = Paths.get(System.getProperty("user.dir")).getRoot();
        // String vol = root.normalize().toString();
        // TODO check on Win
        String vol = new File(FileUtils.getUserDirectoryPath()).getParent();
        return vol.endsWith(separator())
                ? vol + separator()
                : vol + separator() + separator();
    }

    protected String addBackslashToPath(String path) {
        StringBuilder builder = new StringBuilder();
        String[] parts = path.split("\\s+");
        int length = parts.length;

        for (int i = 0; i < length; i++) {
            String part = parts[i];
            builder.append(part);
            if (i < length - 1 && !builder.toString().endsWith("\\ ")) {
                builder.append("\\ ");
            }
        }
        return builder.toString();
    }

}
