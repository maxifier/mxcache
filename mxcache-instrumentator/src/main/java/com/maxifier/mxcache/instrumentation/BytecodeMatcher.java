package com.maxifier.mxcache.instrumentation;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 10.11.2010
 * Time: 10:30:07
 */
public class BytecodeMatcher {
    private final String text;
    private final byte[] bytes;
    private final int[] prefix;

    public BytecodeMatcher(String text) {
        this.text = text;
        bytes = text.getBytes();
        prefix = getPrefix(bytes);
    }

    public boolean isContainedIn(byte[] bytecode) {
        return contains(bytecode, bytes, prefix);
    }

    public String getText() {
        return text;
    }

    private static boolean contains(@NotNull byte[] haystack, @NotNull byte[] needle, @NotNull int[] pf) {
        //Сопоставление образца
        int n = haystack.length;
        int m1 = needle.length;
        for (int i = 0, j = 0; i < n; i++) {
            while (j > 0 && needle[j] != haystack[i]) {
                j = pf[j];
            }
            if (needle[j] == haystack[i]) {
                j++;
            }
            if (j == m1) {
                //Образец обнаружен на сдвиге i - m + 1
                //Ищем следующее вхождение образца
                //j = pf[j];
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings ({ "LoopConditionNotUpdatedInsideLoop" })
    private static int[] getPrefix(@NotNull byte[] needle) {
        if (needle.length == 0) {
            throw new IllegalArgumentException("needle has zero length");
        }
        int m = needle.length;
        int[] pf = new int[m];
        pf[0] = -1;
        //Вычисление префикс-функции
        for (int i = 1; i < m; i++) {
            pf[i] = pf[i - 1] + 1;
            while (pf[i] > 0 && needle[i - 1] != needle[pf[i] - 1]) {
                pf[i] = pf[pf[i] - 1] + 1;
            }
        }
        return pf;
    }
}
