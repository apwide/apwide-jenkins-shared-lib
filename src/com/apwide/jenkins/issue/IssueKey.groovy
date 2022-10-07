package com.apwide.jenkins.issue;

class IssueKey {
    private String value;

     IssueKey(String value) {
        this.value = value.toUpperCase();
    }

    boolean equals( Object o) {
        if (this == o) {
            return true
        }
        if (o == null || getClass() != o.getClass()) {
            return false
        }
        IssueKey issueKey = (IssueKey) o;
        return value == issueKey.value;
    }

    String getValue() {
        return value;
    }
}
