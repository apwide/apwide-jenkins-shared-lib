# AGENTS.md - Apwide Jenkins Shared Library

## Project Overview

**Name:** `apwide-jenkins-shared-lib`  
**Type:** Jenkins Shared Library (Groovy)  
**Purpose:** Integration bridge between Jenkins CI/CD pipelines, Jira, and Apwide Golive (environment & release management plugin for Jira)

This library enables Jenkins pipelines to automate environment management, deployment tracking, release information, and environment monitoring by communicating with both Jira and Apwide Golive REST APIs.

## Key Capabilities

### 1. **Deployment Tracking**
- Push deployment information (version, build number, description) to Golive
- Automatically extract Jira issue keys from Git commit history since last successful build
- Link deployed Jira tickets to environments in Golive
- Track deployment history with build numbers (critical for CI environments where same version is redeployed)

### 2. **Environment Monitoring**
- Automated health checks by calling environment URLs
- Update environment status in Golive (e.g., "Up"/"Down")
- Support for single or multiple environments/applications
- Custom check logic extensibility

### 3. **Environment & Application Management**
- CRUD operations for environments, applications, and categories
- Search and filter environments by criteria
- Self-service environment provisioning from Jira

### 4. **Jira Integration**
- Manage Jira versions and releases
- Direct REST API calls to Jira
- Support for both Jira Server/Data Center and Jira Cloud

### 5. **Golive Cloud Support**
- Native support for both on-premise and cloud deployments
- Automatic API path switching based on configuration

## Architecture

### Core Components

```
src/com/apwide/jenkins/
├── golive/           # Golive-specific business logic
│   ├── Applications.groovy
│   ├── Categories.groovy
│   ├── Deployment.groovy        # Deployment tracking & version management
│   ├── Environment.groovy
│   ├── Environments.groovy      # Environment search & retrieval
│   ├── Golive.groovy           # Main Golive facade
│   └── GoliveInfo.groovy
├── jira/             # Jira-specific business logic
│   ├── Issue.groovy
│   ├── Issues.groovy
│   ├── Project.groovy
│   ├── Release.groovy
│   └── Version.groovy
├── issue/            # Issue key extraction from Git
│   ├── ChangeLogIssueKeyExtractor.groovy
│   └── IssueKeyStringExtractor.groovy
└── util/             # Utilities & infrastructure
    ├── auth/         # Authentication handlers
    │   ├── AuthenticationContext.groovy
    │   ├── Authenticator.groovy
    │   ├── GoliveAuthenticator.groovy
    │   └── JiraAuthenticator.groovy
    ├── GoliveStatus.groovy
    ├── JsonMarshaller.groovy
    ├── Parameters.groovy         # Configuration & parameter handling
    ├── RestClient.groovy        # HTTP client wrapper
    ├── ScriptWrapper.groovy
    ├── Utilities.groovy
    └── Version.groovy

vars/                 # Jenkins Pipeline Global Variables (user-facing API)
├── apwSendDeploymentInfo.groovy
├── apwSetDeployedVersion.groovy
├── apwCheckEnvironmentStatus.groovy
├── apwCheckEnvironmentsStatus.groovy
├── apwCreateEnvironment.groovy
├── apwGetEnvironments.groovy
├── apwSearchEnvironments.groovy
├── apwCallJira.groovy
├── apwJiraCreateVersion.groovy
└── [30+ pipeline steps...]
```

### Key Design Patterns

1. **ScriptWrapper Pattern**: Wraps Jenkins pipeline script context for testability
2. **Parameters Class**: Centralized configuration with environment variable fallbacks
3. **Authenticator Pattern**: Pluggable authentication for Jira Server/Cloud and Golive Server/Cloud
4. **RestClient**: Unified HTTP client with error handling and retry logic
5. **Global Variables**: Each `vars/*.groovy` file exposes a pipeline step to users

## Configuration Strategy

### Precedence Hierarchy (highest to lowest)
1. **Inline step parameters** (e.g., `apwSendDeploymentInfo(jiraBaseUrl: '...')`)
2. **Pipeline environment variables** (set in `pipeline { environment { } }`)
3. **Jenkins global environment variables**
4. **Library defaults**

### Critical Environment Variables

#### Jira Server/Data Center
- `APW_JIRA_BASE_URL` - Jira base URL (e.g., `http://mycompany.com/jira`)
- `APW_JIRA_CREDENTIALS_ID` - Jenkins credentials ID (default: `jira-credentials`)

#### Jira Cloud
- `APW_JIRA_CLOUD_BASE_URL` - Cloud instance URL (e.g., `https://mycompany.atlassian.net`)
- `APW_JIRA_CLOUD_CREDENTIALS_ID` - Jenkins credentials ID (username + API token)

#### Golive Cloud
- `APW_GOLIVE_CLOUD_CREDENTIALS_ID` - Jenkins credentials ID for Golive API token
- `APW_GOLIVE_CLOUD_URL` - Defaults to `https://golive.apwide.net/api`

#### Common Variables
- `APW_ENVIRONMENT_ID` - Target environment ID
- `APW_APPLICATION` - Application name filter
- `APW_CATEGORY` - Category name filter (e.g., 'Dev', 'Staging', 'Production')
- `APW_AVAILABLE_STATUS` / `APW_UNAVAILABLE_STATUS` - Status names for monitoring
- `APW_BUILD_FAIL_ON_ERROR` - Whether to fail build on API errors (default: true)

## Common Use Cases & Examples

### 1. Push Deployment Info
```groovy
apwSendDeploymentInfo(
    environmentId: 23,
    version: '1.2.3',
    buildNumber: env.BUILD_NUMBER
)
// Automatically extracts Jira issues from git commits!
```

### 2. Monitor All Environments
```groovy
environment {
    APW_UNAVAILABLE_STATUS = 'Down'
    APW_AVAILABLE_STATUS = 'Up'
}
apwCheckEnvironmentsStatus()  // Checks all environments
```

### 3. Generic Jira API Call
```groovy
apwCallJira(
    httpMode: 'GET',
    path: '/rest/api/2/project/10000'
)
```

### 4. Search Environments
```groovy
def envs = apwSearchEnvironments(
    application: 'eCommerce',
    category: 'Production'
)
```

## Testing

- **Framework**: Spock (Groovy testing framework)
- **Location**: `test/com/apwide/jenkins/`
- **Mocking**: Custom mock implementations for Jenkins pipeline steps
  - `MockPipelineScript.groovy`
  - `MockHttpRequestPlugin.groovy`
  - `HttpClient.groovy`

## Dependencies

### Required Jenkins Plugins
- **Pipeline Utility Steps Plugin** - For JSON parsing (`readJSON`)
- **HTTP Request Plugin** - For REST API calls (`httpRequest`)

### Build Dependencies (Gradle)
- Groovy 2.5.4
- Spock Framework 1.3
- Jenkins Hudson Core 3.3.0
- Groovy CPS (Continuation Passing Style) 1.28

## Cloud vs Server Detection Logic

The library automatically detects which APIs to use:

1. **Golive Cloud**: If `APW_GOLIVE_CLOUD_CREDENTIALS_ID` is set
2. **Jira Cloud**: If both `APW_JIRA_CLOUD_CREDENTIALS_ID` AND `APW_JIRA_CLOUD_BASE_URL` are set
3. **Server/Data Center**: Default fallback

**Force Server Mode**: Use `APW_FORCE_GOLIVE_SERVER=true` to override cloud detection

## API Endpoints

### Golive Server/Data Center
- Base: `{jiraBaseUrl}/rest/apwide/tem/1.1`
- Private: `{jiraBaseUrl}/rest/apwide/golive/1`

### Golive Cloud
- Base: `https://golive.apwide.net/api`
- Private: `https://golive.apwide.net/private`

### Jira
- Server: `/rest/api/2`
- Cloud: `/rest/api/3`

## Special Features

### Automatic Issue Key Extraction
`ChangeLogIssueKeyExtractor` parses Git commit messages between current and last successful build to find Jira issue keys (e.g., PROJ-123). This enables automatic deployment tracking without manual configuration.

### Flexible Environment Identification
Environments can be referenced by:
- **ID**: `environmentId: 23`
- **Name composite**: `application: 'eCommerce', category: 'Production'`

### Error Handling
- Configurable fail-on-error behavior
- Automatic build status restoration on API errors
- Debug logging at multiple levels

## Project Structure Notes

### Examples Directory
Contains complete Jenkinsfile examples organized by use case:
- `examples/deployment/` - Deployment tracking scenarios
- `examples/monitoring/` - Environment monitoring patterns
- `examples/self-service/` - User-triggered provisioning
- `examples/discovery/` - Environment discovery automation
- `examples/others/` - Miscellaneous utilities

Each example includes:
- `Jenkinsfile` - Working pipeline code
- `README.md` - Explanation and context

### Documentation Pattern
Each `vars/*.groovy` has a matching `vars/*.txt` file containing help documentation visible in Jenkins UI after first successful run.

## Development Best Practices

### When Adding New Features
1. Add business logic in `src/com/apwide/jenkins/`
2. Create pipeline step in `vars/`
3. Add help documentation in `vars/*.txt`
4. Include example in `examples/`
5. Write Spock tests in `test/`

### Jenkins Pipeline CPS (Continuation Passing Style) Rules

**Critical Rule**: You **CANNOT** call CPS-transformed methods from within `@NonCPS` methods.

#### What are CPS-transformed methods?
- Jenkins Pipeline steps (e.g., `echo`, `sh`, `readFile`)
- Jenkins API calls on build objects (e.g., `build.getPreviousBuild()`, `build.getResult()`)
- Methods from `ScriptWrapper` that delegate to Jenkins (e.g., `script.getCurrentBuildFullDisplayName()`)

#### The "Collect in CPS, Process in @NonCPS" Pattern

**✅ CORRECT Pattern:**
```groovy
// Regular method (CPS context) - can call Jenkins API
Collection<String> extract() {
    // Collect all data from Jenkins API first
    def buildDataList = []
    def previousBuild = script.getPreviousBuild()
    
    while (previousBuild != null) {
        buildDataList << [
            displayName: previousBuild.getFullDisplayName(),  // ✅ OK here
            changeSets: previousBuild.getChangeSets()
        ]
        previousBuild = previousBuild.getPreviousBuild()  // ✅ OK here
    }
    
    // Pass plain data to @NonCPS method
    return processBuilds(buildDataList)
}

@NonCPS
private Collection<String> processBuilds(List<Map> buildDataList) {
    // Only work with plain Java objects (Map, List, String)
    buildDataList.each { buildData ->
        process(buildData.displayName, buildData.changeSets)  // ✅ OK - plain objects
    }
}
```

**❌ WRONG Pattern:**
```groovy
@NonCPS
private Collection<String> processBuilds(def previousBuild) {
    while (previousBuild != null) {
        previousBuild.getPreviousBuild()  // ❌ FAILS - CPS call in @NonCPS
    }
}
```

#### When to Use @NonCPS
- **Performance**: Complex loops/iterations over plain data structures
- **Serialization**: When you need non-serializable objects (temporary use only)
- **Standard Java APIs**: Working with collections, strings, regex, etc.

#### When NOT to Use @NonCPS
- When you need to call ANY Jenkins API
- When you need to call pipeline steps
- When you need to access Jenkins build objects directly

### Authentication Flow
1. Parameters object determines cloud vs server
2. Appropriate Authenticator is selected
3. RestClient wraps authentication in closure
4. AuthenticationContext provides credentials to HTTP request

### Debugging
Enable debug logging:
```groovy
environment {
    APW_LOG_LEVEL = 'DEBUG'
}
```

## Future Collaboration Context

### Common Tasks
- **Adding new pipeline step**: Create in `vars/`, wire to existing service class
- **Extending Golive API support**: Add methods to relevant class in `golive/`
- **Adding Jira functionality**: Extend classes in `jira/`
- **Fixing authentication issues**: Check `util/auth/` authenticators
- **API compatibility**: Check `Parameters.groovy` for URL construction logic

### Important Files for Modifications
- `Parameters.groovy` - All configuration logic
- `RestClient.groovy` - All HTTP communication
- `Deployment.groovy` - Deployment tracking core logic
- `Environments.groovy` - Environment search/retrieval

### Testing Strategy
- Mock Jenkins pipeline context with `MockPipelineScript`
- Use `HttpClient` for HTTP response simulation
- Spock specifications in `test/` mirror `src/` structure

## Tips for AI Agents

1. **Configuration is hierarchical** - Always check Parameters.groovy for precedence
2. **Cloud detection is automatic** - Based on credential IDs and URLs
3. **Each vars/*.groovy is a pipeline step** - This is the public API
4. **ScriptWrapper bridges testing** - Allows unit testing of pipeline code
5. **Issue key extraction is regex-based** - Check IssueKeyStringExtractor for patterns
6. **Examples are the best documentation** - Reference examples/ for real-world usage
7. **Gradle build** - Use `./gradlew test` for running tests
8. **Jenkins shared library conventions** - vars/ directory has special meaning

## Version & Compatibility
- **Current Version**: 1.0-SNAPSHOT
- **Java Compatibility**: 1.8+
- **Groovy Version**: 2.5.4
- **Jenkins**: Requires Pipeline-compatible version
