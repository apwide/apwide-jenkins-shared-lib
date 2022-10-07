package com.apwide.jenkins.issue

import com.apwide.jenkins.util.ScriptWrapper

interface IssueKeyExtractor {
    Integer ISSUE_KEY_MAX_LIMIT = 100;
    Collection<String> extractIssueKeys(ScriptWrapper script);
}

