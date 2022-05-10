package com.apwide.jenkins.issue
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils

/**
 * Extracts issue keys (eg. TEST-123) of any number of instances from a given string. Input can be a
 * commit message or a branch name.
 */
class IssueKeyStringExtractor {

    private  final String SEPARATOR = "[\\s\\p{Punct}]"
    // zero-width positive lookbehind
    private  final String KEY_PREFIX_REGEX = "(?:(?<=" + SEPARATOR + ")|^)"
    // max of 256 chars in Issue Key project name and 100 for the issue number
    private  final String KEY_BODY_REGEX =
            "(\\p{Lu}[\\p{Lu}\\p{Digit}_]{1,255}-\\p{Digit}{1,100})"
    // zero-width positive lookahead
    private  final String KEY_POSTFIX_REGEX = "(?:(?=" + SEPARATOR + ')|$)'

    private  final String ISSUE_KEY_REGEX =
            KEY_PREFIX_REGEX + KEY_BODY_REGEX + KEY_POSTFIX_REGEX
    private  final Pattern PROJECT_KEY_PATTERN = Pattern.compile(ISSUE_KEY_REGEX)

    Collection<IssueKey> extractIssueKeys(String text) {
        final List<IssueKey> matches = new ArrayList<>()

        if (StringUtils.isBlank(text)) {
            return Collections.emptyList()
        }

        final Matcher match = PROJECT_KEY_PATTERN.matcher(text)

        while (match.find()) {
            for (int i = 1 ;i <= match.groupCount() ;i++) {
                final String issueKey = match.group(i)
                matches.add(new IssueKey(issueKey))

                if (matches.size() >= IssueKeyExtractor.ISSUE_KEY_MAX_LIMIT) {
                    return matches
                }
            }
        }

        return matches
    }
}
