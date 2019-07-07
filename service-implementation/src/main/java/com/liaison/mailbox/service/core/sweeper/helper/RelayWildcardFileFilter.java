/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.sweeper.helper;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AbstractFileFilter;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class RelayWildcardFileFilter  extends AbstractFileFilter implements Serializable {

    /** The wildcards that will be used to match filenames. */
    private final String[] wildcards;
    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

    /**
     * Construct a new case-sensitive wildcard filter for a single wildcard.
     *
     * @param wildcard  the wildcard to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public RelayWildcardFileFilter(final String wildcard) {
        this(wildcard, IOCase.SENSITIVE);
    }

    /**
     * Construct a new wildcard filter for a single wildcard specifying case-sensitivity.
     *
     * @param wildcard  the wildcard to match, not null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the pattern is null
     */
    public RelayWildcardFileFilter(final String wildcard, final IOCase caseSensitivity) {
        if (wildcard == null) {
            throw new IllegalArgumentException("The wildcard must not be null");
        }
        this.wildcards = new String[] { wildcard };
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Construct a new case-sensitive wildcard filter for an array of wildcards.
     * <p>
     *
     * @param wildcards  the array of wildcards to match
     * @throws IllegalArgumentException if the pattern array is null
     */
    public RelayWildcardFileFilter(final String[] wildcards) {
        this(wildcards, IOCase.SENSITIVE);
    }

    /**
     * Construct a new wildcard filter for an array of wildcards specifying case-sensitivity.
     * <p>
     *
     * @param wildcards  the array of wildcards to match, not null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the pattern array is null
     */
    public RelayWildcardFileFilter(final String[] wildcards, final IOCase caseSensitivity) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard array must not be null");
        }
        this.wildcards = new String[wildcards.length];
        System.arraycopy(wildcards, 0, this.wildcards, 0, wildcards.length);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Construct a new case-sensitive wildcard filter for a list of wildcards.
     *
     * @param wildcards  the list of wildcards to match, not null
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public RelayWildcardFileFilter(final List<String> wildcards) {
        this(wildcards, IOCase.SENSITIVE);
    }

    /**
     * Construct a new wildcard filter for a list of wildcards specifying case-sensitivity.
     *
     * @param wildcards  the list of wildcards to match, not null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public RelayWildcardFileFilter(final List<String> wildcards, final IOCase caseSensitivity) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard list must not be null");
        }
        this.wildcards = wildcards.toArray(new String[wildcards.size()]);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Checks to see if the filename matches with all the wildcards.
     *
     * @param file  the file to check
     * @return true if the filename matches with all the wildcards
     */
    @Override
    public boolean accept(final File file) {
        
        final String name = file.getName();
        boolean anyFalse = false;
        for (final String wildcard : wildcards) {
            if (!FilenameUtils.wildcardMatch(name, wildcard, caseSensitivity)) {
                anyFalse = true;
            }
        }
        return !anyFalse;
    }

}
