/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen;

import com.beust.jcommander.Parameter;

import java.util.List;

/**
 * Options - JCommander-annotated configuration class for StubGen
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-08 10:41)
 */
public class Options {
    @Parameter(names = {"-e", "--examine"}, required = true, description = "Path to JARs to examine")
    List<String> examine;

    @Parameter(names = {"-l", "--lib"}, required = true, description = "Path to library JARs that StubGen should generate stubs for")
    List<String> libraries;

    @Parameter(names = {"-o", "--o"}, description = "Output path, current directory by default")
    String outputPath = ".";

    @Parameter(names = {"-c", "--comment"}, description = "Path to file with top comment for all files; " +
            "by default StubGen seeks for \"comment\" file in output path")
    String commentPath;

    @Parameter(names = {"-p", "--copyright"}, description = "Path to file with copyright comment to be added before package statement; " +
            "by default StubGen seeks for \"copyright\" file in output path")
    String copyrightPath;

    @Parameter(names = {"-i", "--indent"}, description = "Indentation")
    String indent = "     ";

    @Parameter(names = {"-r", "--redundant-modifiers"}, description = "Whether to generate redundant modifiers or not (e.g. public abstract for interface methods)")
    boolean redundantModifiers;
}
