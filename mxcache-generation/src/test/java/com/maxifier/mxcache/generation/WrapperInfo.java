/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.generation;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * WrapperInfo
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class WrapperInfo {
    private final String primitive;
    private final String className;
    private final String shortName;
    private final String testValue;
    private final String troveType;
    private final String toTrove;
    private final String fromTrove;
    private final boolean object;

    private static String shortInt(String type) {
        return type.equals("Integer") ? "Int" : type;
    }

    public WrapperInfo(String primitive, String className, String testValue, boolean object) {
        this.primitive = primitive;
        this.className = className;
        this.shortName = shortInt(className);
        this.testValue = testValue;
        this.troveType = shortName;
        this.toTrove = "$1";
        this.fromTrove = "$1";
        this.object = object;
    }

    public WrapperInfo(String primitive, String className, String testValue, String troveType, String toTrove, String fromTrove, boolean object) {
        this.primitive = primitive;
        this.className = className;
        this.troveType = troveType;
        this.shortName = shortInt(className);
        this.testValue = testValue;
        this.toTrove = toTrove;
        this.fromTrove = fromTrove;
        this.object = object;
    }

    public boolean isObject() {
        return object;
    }

    private String replaceConditional(String in, Map<String, Boolean> conditions) {
        int pos = in.indexOf("#IF");
        int lastPos = 0;
        StringBuilder out = new StringBuilder(in.length());
        while (pos >= 0) {
            out.append(in.substring(lastPos, pos));
            int ifEnd = in.indexOf('#', pos + "#IF".length());
            String condition = in.substring(pos + "#IF".length(), ifEnd);
            Boolean value = conditions.get(condition);
            int elsePos = in.indexOf("#ELSE#", ifEnd);
            int endIfPos = in.indexOf("#ENDIF#", ifEnd);
            int endPos = endIfPos + "#ENDIF#".length();
            if (value == null) {
                out.append(in.substring(pos, endPos));
            } else {
                String trueV;
                String falseV;
                if (elsePos >=0 && elsePos < endIfPos) {
                    trueV = in.substring(ifEnd + 1, elsePos);
                    falseV = in.substring(elsePos + "#ELSE#".length(), endIfPos);
                } else {
                    trueV = in.substring(ifEnd + 1, endIfPos);
                    falseV = "";
                }
                out.append(value ? trueV : falseV);
            }
            pos = in.indexOf("#IF", endPos);
            lastPos = endPos;
        }
        out.append(in.substring(lastPos));
        return out.toString();
    }

    public String replaceE(String out) {
        out = replaceConditional(out, Collections.singletonMap("_E", !primitive.isEmpty()));

        out = Pattern.compile("#e#").matcher(out).replaceAll(primitive);
        out = Pattern.compile("#eg#").matcher(out).replaceAll(object ? "E" : primitive);
        out = Pattern.compile("#EG#").matcher(out).replaceAll(object ? "E" : shortName);
        out = Pattern.compile("#E#").matcher(out).replaceAll(shortName);
        out = Pattern.compile("#ET#").matcher(out).replaceAll(className);
        out = Pattern.compile("#ETEST#").matcher(out).replaceAll(testValue);
        out = Pattern.compile("#E_TROVE#").matcher(out).replaceAll(object ? "E" : troveType);
        out = Pattern.compile("#E_TROVE:([^#]*)#").matcher(out).replaceAll(toTrove);
        out = Pattern.compile("#E_FROM_TROVE:([^#]*)#").matcher(out).replaceAll(fromTrove);
        return out;
    }

    public String replaceF(String out) {
        out = replaceConditional(out, Collections.singletonMap("_F", primitive.isEmpty()));

        out = Pattern.compile("#f#").matcher(out).replaceAll(primitive);
        out = Pattern.compile("#fg#").matcher(out).replaceAll(object ? "F" : primitive);
        out = Pattern.compile("#FG#").matcher(out).replaceAll(object ? "F" : shortName);
        out = Pattern.compile("#F#").matcher(out).replaceAll(shortName);
        out = Pattern.compile("#FT#").matcher(out).replaceAll(className);
        out = Pattern.compile("#FTEST#").matcher(out).replaceAll(testValue);
        out = Pattern.compile("#F_TROVE#").matcher(out).replaceAll(object ? "F" : troveType);
        out = Pattern.compile("#F_TROVE:([^#]*)#").matcher(out).replaceAll(toTrove);
        out = Pattern.compile("#F_FROM_TROVE:([^#]*)#").matcher(out).replaceAll(fromTrove);
        return out;
    }

    public String getShortName() {
        return shortName;
    }

    public String getTroveType() {
        return troveType;
    }
}
