/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import javax.xml.bind.annotation.XmlElement;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class Selector {
    @XmlElement (name = "class")
    private String className;

    @XmlElement
    private String group;

    @XmlElement(name = "tag")
    private Set<String> tags;

    @XmlElement(name = "annotated")
    private Set<String> annotations;

    public boolean matches(Class className, String group, String[] tags) {
        return matchClassName(className) && matchGroup(group) && matchTagsAndAnnotations(tags);
    }

    private boolean matchTagsAndAnnotations(String[] tags) {
        int matchedTagCount = 0;
        int matchedAnnotationCount = 0;
        if (tags != null) {
            for (String tag : tags) {
                if (isAnnotationTag(tag)) {
                    if (hasAnnotation(tag.substring(1))) {
                        matchedAnnotationCount++;
                    }
                } else {
                    if (hasTag(tag)) {
                        matchedTagCount++;
                    }
                }
            }
        }
        return isAllAnnotationsMatched(matchedAnnotationCount) && isAllTagsMatched(matchedTagCount);
    }

    private boolean isAllTagsMatched(int matchedTagCount) {
        return tags == null || matchedTagCount == tags.size();
    }

    private boolean isAllAnnotationsMatched(int matchedAnnotationCount) {
        return annotations == null || matchedAnnotationCount == annotations.size();
    }

    private boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    private boolean hasAnnotation(String annotation) {
        return annotations != null && annotations.contains(annotation);
    }

    private static boolean isAnnotationTag(String tag) {
        return tag.charAt(0) == '@';
    }

    private boolean matchGroup(String group) {
        if (this.group != null && !this.group.equals(group)) {
            if (this.group.endsWith("*")) {
                String groupStart = this.group.substring(0, this.group.length() - 1);
                if (!group.startsWith(groupStart)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean matchClassName(Class className) {
        if (this.className != null) {
            if (this.className.endsWith("*")) {
                String classNameStart = this.className.substring(0, this.className.length()-1);
                if (!className.getCanonicalName().startsWith(classNameStart)) {
                    return false;
                }
            } else if (!this.className.equals(className.getCanonicalName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean start = true;

        if (className != null) {
            if (start) {
                start = false;
            } else {
                builder.append(", ");
            }
            builder.append("class:").append(className);
        }

        if (group != null) {
            if (start) {
                start = false;
            } else {
                builder.append(", ");
            }
            builder.append("group:").append(group);
        }

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                if (start) {
                    start = false;
                } else {
                    builder.append(", ");
                }
                builder.append("tag:").append(tag);
            }
        }
        if (annotations != null && !annotations.isEmpty()) {
            for (String annotation : annotations) {
                if (start) {
                    start = false;
                } else {
                    builder.append(", ");
                }
                builder.append("@").append(annotation);
            }
        }
        return builder.toString();
    }
}
