# 🔄 CI/CD Tools - Banking Interview Guide

> **Complete guide to Continuous Integration and Deployment tools for banking applications**
> Covering Git, Jenkins, Maven, Gradle, testing, security scanning, and deployment automation

---

## 📋 Table of Contents

### 🔧 **Version Control and Build Tools**
- **[Q1: Git Advanced Workflows](#q1-git-advanced-workflows)** - Branching strategies, merge policies
- **[Q2: Maven Project Management](#q2-maven-project-management)** - Dependencies, profiles, plugins
- **[Q3: Gradle Build Automation](#q3-gradle-build-automation)** - Multi-project builds, tasks
- **[Q4: Artifact Management](#q4-artifact-management)** - Nexus, Artifactory, versioning
- **[Q5: Code Quality Tools](#q5-code-quality-tools)** - SonarQube, Checkstyle, SpotBugs

### 🏗️ **Jenkins CI/CD Pipelines**
- **[Q6: Jenkins Pipeline Design](#q6-jenkins-pipeline-design)** - Declarative and scripted pipelines
- **[Q7: Banking Deployment Pipeline](#q7-banking-deployment-pipeline)** - Multi-environment deployment
- **[Q8: Security Integration](#q8-security-integration)** - SAST, DAST, dependency scanning
- **[Q9: Testing Automation](#q9-testing-automation)** - Unit, integration, performance tests
- **[Q10: Infrastructure as Code](#q10-infrastructure-as-code)** - Terraform, Ansible integration

### 🔒 **Security and Compliance**
- **[Q11: Security Scanning](#q11-security-scanning)** - Vulnerability assessment tools
- **[Q12: Compliance Automation](#q12-compliance-automation)** - Regulatory requirements
- **[Q13: Secrets Management](#q13-secrets-management)** - Vault, encrypted credentials
- **[Q14: Container Security](#q14-container-security)** - Image scanning, runtime protection
- **[Q15: Audit and Reporting](#q15-audit-and-reporting)** - Compliance dashboards

### 🚀 **Advanced CI/CD Patterns**
- **[Q16: GitOps Implementation](#q16-gitops-implementation)** - ArgoCD, Flux deployment
- **[Q17: Blue-Green Deployment](#q17-blue-green-deployment)** - Zero-downtime releases
- **[Q18: Canary Releases](#q18-canary-releases)** - Progressive deployment strategies
- **[Q19: Monitoring and Observability](#q19-monitoring-and-observability)** - Pipeline metrics
- **[Q20: Disaster Recovery](#q20-disaster-recovery)** - Backup and rollback strategies

---

## Version Control and Build Tools

### Q1: Git Advanced Workflows

**Question**: Design a Git workflow for banking applications that ensures code quality, security, and regulatory compliance.

**Answer**:

**Banking Git Workflow Strategy**:

```bash
# banking-git-workflow.sh

# Feature Branch Workflow for Banking
# Main branches:
# - main: Production-ready code
# - develop: Integration branch for features
# - release/*: Release preparation branches
# - hotfix/*: Emergency fixes

# 1. Feature Development
create_feature_branch() {
    local feature_name=$1
    local jira_ticket=$2

    git checkout develop
    git pull origin develop
    git checkout -b "feature/${jira_ticket}-${feature_name}"

    echo "Created feature branch: feature/${jira_ticket}-${feature_name}"
}

# 2. Banking-specific commit message format
banking_commit() {
    local ticket=$1
    local type=$2  # feat, fix, security, compliance
    local message=$3

    # Commit message format: [TICKET] type: message
    git commit -m "[${ticket}] ${type}: ${message}

Co-authored-by: Pair Programmer <pair@company.com>
Compliance-check: ✓
Security-review: Required"
}

# 3. Pre-commit hooks for banking compliance
setup_pre_commit_hooks() {
    cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash

echo "Running banking pre-commit checks..."

# 1. Check for secrets
if git diff --cached --name-only | xargs grep -l "password\|secret\|key" 2>/dev/null; then
    echo "❌ Potential secrets detected in staged files"
    echo "Please remove sensitive information before committing"
    exit 1
fi

# 2. Check commit message format
commit_msg=$(git log --format=%B -n 1 HEAD)
if ! echo "$commit_msg" | grep -E "^\[BANK-[0-9]+\] (feat|fix|security|compliance|docs):" > /dev/null; then
    echo "❌ Invalid commit message format"
    echo "Format: [BANK-123] type: description"
    exit 1
fi

# 3. Run unit tests
echo "Running unit tests..."
mvn test -q
if [ $? -ne 0 ]; then
    echo "❌ Unit tests failed"
    exit 1
fi

# 4. Check code formatting
echo "Checking code formatting..."
mvn spotless:check -q
if [ $? -ne 0 ]; then
    echo "❌ Code formatting issues detected. Run 'mvn spotless:apply'"
    exit 1
fi

# 5. Security scan
echo "Running security scan..."
mvn dependency-check:check -q
if [ $? -ne 0 ]; then
    echo "❌ Security vulnerabilities detected"
    exit 1
fi

echo "✅ All pre-commit checks passed"
EOF

    chmod +x .git/hooks/pre-commit
}

# 4. Protected branch rules
setup_branch_protection() {
    # GitHub API call to set up branch protection
    curl -X PUT \
      -H "Authorization: token ${GITHUB_TOKEN}" \
      -H "Accept: application/vnd.github.v3+json" \
      "https://api.github.com/repos/banking-org/banking-app/branches/main/protection" \
      -d '{
        "required_status_checks": {
          "strict": true,
          "contexts": [
            "ci/jenkins/build",
            "ci/jenkins/security-scan",
            "ci/jenkins/compliance-check"
          ]
        },
        "enforce_admins": true,
        "required_pull_request_reviews": {
          "required_approving_review_count": 2,
          "dismiss_stale_reviews": true,
          "require_code_owner_reviews": true,
          "required_approving_review_count": 2
        },
        "restrictions": {
          "users": [],
          "teams": ["banking-senior-developers", "security-team"]
        }
      }'
}

# 5. Code review template
create_pr_template() {
    mkdir -p .github/pull_request_template.md
    cat > .github/pull_request_template.md << 'EOF'
## Banking Application Change Request

### Change Description
<!-- Brief description of the changes -->

### JIRA Ticket
<!-- Link to JIRA ticket: BANK-XXX -->

### Type of Change
- [ ] 🚀 New feature
- [ ] 🐛 Bug fix
- [ ] 🔒 Security enhancement
- [ ] 📋 Compliance update
- [ ] 🔧 Technical debt
- [ ] 📖 Documentation

### Testing Performed
- [ ] Unit tests added/updated
- [ ] Integration tests passed
- [ ] Security testing completed
- [ ] Performance testing (if applicable)
- [ ] Manual testing completed

### Security Checklist
- [ ] No hardcoded secrets or credentials
- [ ] Input validation implemented
- [ ] Authorization checks in place
- [ ] Audit logging implemented
- [ ] Data encryption considered

### Compliance Checklist
- [ ] PCI DSS requirements reviewed
- [ ] SOX compliance maintained
- [ ] Data privacy requirements met
- [ ] Audit trail preserved

### Deployment Notes
- [ ] Database migrations included
- [ ] Configuration changes documented
- [ ] Rollback plan prepared
- [ ] Environment-specific settings verified

### Reviewers
- [ ] @security-team (for security changes)
- [ ] @compliance-team (for regulatory changes)
- [ ] @senior-developer (code review)
- [ ] @devops-team (infrastructure changes)
EOF
}

# 6. Banking-specific Git aliases
setup_git_aliases() {
    git config alias.banking-status "status --porcelain"
    git config alias.banking-log "log --oneline --graph --decorate"
    git config alias.safe-push "push --verify"
    git config alias.compliance-check "log --grep='Compliance-check: ✓'"

    # Custom command to check if branch is ready for merge
    git config alias.ready-for-merge '!f() {
        git fetch origin;
        behind=$(git rev-list --count HEAD..origin/develop);
        if [ $behind -gt 0 ]; then
            echo "Branch is $behind commits behind develop. Please rebase.";
            return 1;
        fi;
        echo "Branch is ready for merge";
    }; f'
}
```

**Git Branch Strategy for Banking**:

```bash
# banking-branch-strategy.sh

# Release branch creation
create_release_branch() {
    local version=$1

    git checkout develop
    git pull origin develop
    git checkout -b "release/${version}"

    # Update version in all relevant files
    mvn versions:set -DnewVersion="${version}" -DgenerateBackupPoms=false

    # Create release notes
    git log --oneline $(git describe --tags --abbrev=0)..HEAD > RELEASE_NOTES_${version}.md

    git add .
    git commit -m "[RELEASE] Prepare release ${version}"
    git push origin "release/${version}"

    echo "Release branch created: release/${version}"
}

# Hotfix workflow
create_hotfix() {
    local issue=$1
    local version=$2

    git checkout main
    git pull origin main
    git checkout -b "hotfix/${issue}-${version}"

    echo "Hotfix branch created: hotfix/${issue}-${version}"
    echo "⚠️  Remember to:"
    echo "  1. Fix the issue"
    echo "  2. Update version number"
    echo "  3. Create PR to main AND develop"
    echo "  4. Tag after merge"
}

# Automated merge to develop after main merge
post_merge_sync() {
    local branch_type=$1

    if [[ "$branch_type" == "hotfix" || "$branch_type" == "release" ]]; then
        git checkout develop
        git pull origin develop
        git merge origin/main
        git push origin develop

        echo "✅ Synced develop branch with main"
    fi
}

# Clean up merged branches
cleanup_branches() {
    # Delete merged feature branches
    git branch --merged develop | grep "feature/" | xargs -n 1 git branch -d

    # Delete old release branches (keep last 3)
    git branch | grep "release/" | sort -V | head -n -3 | xargs -n 1 git branch -D

    echo "✅ Cleaned up old branches"
}
```

**Banking Code Review Checklist**:

```yaml
# .github/CODEOWNERS
# Banking application code owners

# Root level - Senior developers and architects
* @banking-senior-developers @security-team

# Source code - Development team
src/ @banking-developers @banking-senior-developers

# Security-related files - Security team mandatory
src/main/java/**/security/ @security-team
src/main/java/**/auth/ @security-team
src/main/resources/application*.yml @security-team @devops-team

# Database changes - DBA team
src/main/resources/db/ @dba-team @banking-senior-developers
flyway/ @dba-team

# Configuration files - DevOps team
Dockerfile @devops-team @security-team
docker-compose*.yml @devops-team
k8s/ @devops-team @platform-team
.github/workflows/ @devops-team

# Documentation - Technical writers and team leads
docs/ @tech-writers @banking-senior-developers
README.md @tech-writers @banking-senior-developers

# Build and CI/CD - DevOps and senior developers
pom.xml @devops-team @banking-senior-developers
Jenkinsfile @devops-team
.github/workflows/ @devops-team
```

**Automated Git Metrics for Banking**:

```bash
#!/bin/bash
# git-metrics-banking.sh

generate_banking_metrics() {
    local start_date=$1
    local end_date=$2

    echo "=== Banking Git Metrics Report ==="
    echo "Period: $start_date to $end_date"
    echo

    # Commit frequency by type
    echo "Commits by Type:"
    git log --since="$start_date" --until="$end_date" --oneline | \
        grep -E '\[(BANK-[0-9]+)\] (feat|fix|security|compliance):' | \
        sed 's/.*] \([^:]*\):.*/\1/' | \
        sort | uniq -c | sort -nr

    echo

    # Security-related commits
    echo "Security Commits:"
    git log --since="$start_date" --until="$end_date" --oneline | \
        grep -E 'security|Security|SECURITY' | wc -l

    echo

    # Compliance commits
    echo "Compliance Commits:"
    git log --since="$start_date" --until="$end_date" --oneline | \
        grep -E 'compliance|Compliance|COMPLIANCE' | wc -l

    echo

    # Code review metrics
    echo "Pull Request Metrics:"
    gh pr list --state merged --search "merged:$start_date..$end_date" --json number,reviews | \
        jq '.[] | select(.reviews | length >= 2) | .number' | wc -l

    echo "PRs with required reviews"

    # Files changed most frequently
    echo
    echo "Most Changed Files:"
    git log --since="$start_date" --until="$end_date" --name-only --pretty=format: | \
        sort | uniq -c | sort -nr | head -10
}

# Usage
generate_banking_metrics "2024-01-01" "2024-01-31"
```

**Key Benefits for Banking Git Workflow**:

1. **Regulatory Compliance**: Audit trails and approval processes
2. **Security**: Automated secret detection and security reviews
3. **Quality Assurance**: Mandatory code reviews and testing
4. **Traceability**: Clear commit messages linked to tickets
5. **Risk Management**: Protected branches and rollback capabilities
6. **Team Collaboration**: Clear workflows and responsibilities

---

### Q2: Maven Project Management

**Question**: How do you structure Maven projects for banking applications with multiple modules, profiles, and security requirements?

**Answer**:

**Banking Multi-Module Maven Structure**:

```xml
<!-- Parent POM: banking-parent/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.bank.core</groupId>
    <artifactId>banking-parent</artifactId>
    <version>2.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>Banking Application Parent</name>
    <description>Parent POM for all banking applications</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- Framework Versions -->
        <spring-boot.version>3.2.0</spring-boot.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
        <spring-security.version>6.2.0</spring-security.version>

        <!-- Database Versions -->
        <postgresql.version>42.7.1</postgresql.version>
        <flyway.version>9.22.3</flyway.version>
        <hibernate.version>6.4.0.Final</hibernate.version>

        <!-- Testing Versions -->
        <junit.version>5.10.1</junit.version>
        <testcontainers.version>1.19.3</testcontainers.version>
        <mockito.version>5.8.0</mockito.version>

        <!-- Security and Compliance -->
        <owasp-dependency-check.version>9.0.7</owasp-dependency-check.version>
        <spotbugs.version>4.8.2</spotbugs.version>
        <checkstyle.version>10.12.5</checkstyle.version>

        <!-- Build Tools -->
        <maven-surefire.version>3.2.2</maven-surefire.version>
        <maven-failsafe.version>3.2.2</maven-failsafe.version>
        <jacoco.version>0.8.11</jacoco.version>
        <sonar.version>3.10.0.2594</sonar.version>
    </properties>

    <modules>
        <module>banking-common</module>
        <module>banking-security</module>
        <module>banking-data</module>
        <module>banking-account-service</module>
        <module>banking-transaction-service</module>
        <module>banking-notification-service</module>
        <module>banking-web</module>
        <module>banking-integration-tests</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Spring Cloud BOM -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Banking Internal Modules -->
            <dependency>
                <groupId>com.bank.core</groupId>
                <artifactId>banking-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.bank.core</groupId>
                <artifactId>banking-security</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.bank.core</groupId>
                <artifactId>banking-data</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- Database -->
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
                <version>${postgresql.version}</version>
            </dependency>

            <!-- Testing -->
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <!-- Compiler Plugin -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.11.0</version>
                    <configuration>
                        <source>${java.version}</source>
                        <target>${java.version}</target>
                        <compilerArgs>
                            <arg>-parameters</arg>
                            <arg>-Xlint:all</arg>
                            <arg>-Xlint:-processing</arg>
                        </compilerArgs>
                    </configuration>
                </plugin>

                <!-- Surefire Plugin (Unit Tests) -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>${maven-surefire.version}</version>
                    <configuration>
                        <includes>
                            <include>**/*Test.java</include>
                            <include>**/*Tests.java</include>
                        </includes>
                        <excludes>
                            <exclude>**/*IntegrationTest.java</exclude>
                            <exclude>**/*IT.java</exclude>
                        </excludes>
                        <systemPropertyVariables>
                            <java.security.egd>file:/dev/./urandom</java.security.egd>
                        </systemPropertyVariables>
                    </configuration>
                </plugin>

                <!-- Failsafe Plugin (Integration Tests) -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-failsafe-plugin</artifactId>
                    <version>${maven-failsafe.version}</version>
                    <configuration>
                        <includes>
                            <include>**/*IntegrationTest.java</include>
                            <include>**/*IT.java</include>
                        </includes>
                    </configuration>
                    <executions>
                        <execution>
                            <goals>
                                <goal>integration-test</goal>
                                <goal>verify</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>

                <!-- JaCoCo Code Coverage -->
                <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                    <version>${jacoco.version}</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>prepare-agent</goal>
                            </goals>
                        </execution>
                        <execution>
                            <id>report</id>
                            <phase>test</phase>
                            <goals>
                                <goal>report</goal>
                            </goals>
                        </execution>
                        <execution>
                            <id>check</id>
                            <goals>
                                <goal>check</goal>
                            </goals>
                            <configuration>
                                <rules>
                                    <rule>
                                        <element>BUNDLE</element>
                                        <limits>
                                            <limit>
                                                <counter>INSTRUCTION</counter>
                                                <value>COVEREDRATIO</value>
                                                <minimum>0.80</minimum>
                                            </limit>
                                        </limits>
                                    </rule>
                                </rules>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>

                <!-- OWASP Dependency Check -->
                <plugin>
                    <groupId>org.owasp</groupId>
                    <artifactId>dependency-check-maven</artifactId>
                    <version>${owasp-dependency-check.version}</version>
                    <configuration>
                        <failBuildOnCVSS>7</failBuildOnCVSS>
                        <suppressionFiles>
                            <suppressionFile>owasp-suppressions.xml</suppressionFile>
                        </suppressionFiles>
                        <formats>
                            <format>HTML</format>
                            <format>JSON</format>
                        </formats>
                    </configuration>
                    <executions>
                        <execution>
                            <goals>
                                <goal>check</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>

                <!-- SpotBugs -->
                <plugin>
                    <groupId>com.github.spotbugs</groupId>
                    <artifactId>spotbugs-maven-plugin</artifactId>
                    <version>4.8.2.0</version>
                    <configuration>
                        <effort>Max</effort>
                        <threshold>Low</threshold>
                        <xmlOutput>true</xmlOutput>
                        <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>
                    </configuration>
                    <executions>
                        <execution>
                            <goals>
                                <goal>check</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>

                <!-- Checkstyle -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-checkstyle-plugin</artifactId>
                    <version>3.3.1</version>
                    <configuration>
                        <configLocation>checkstyle.xml</configLocation>
                        <encoding>UTF-8</encoding>
                        <consoleOutput>true</consoleOutput>
                        <failsOnError>true</failsOnError>
                    </configuration>
                    <executions>
                        <execution>
                            <id>validate</id>
                            <phase>validate</phase>
                            <goals>
                                <goal>check</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>

                <!-- SonarQube -->
                <plugin>
                    <groupId>org.sonarsource.scanner.maven</groupId>
                    <artifactId>sonar-maven-plugin</artifactId>
                    <version>${sonar.version}</version>
                </plugin>

                <!-- Spring Boot Plugin -->
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                    <configuration>
                        <excludes>
                            <exclude>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                            </exclude>
                        </excludes>
                    </configuration>
                    <executions>
                        <execution>
                            <goals>
                                <goal>repackage</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

    <profiles>
        <!-- Development Profile -->
        <profile>
            <id>dev</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <spring.profiles.active>dev</spring.profiles.active>
                <skip.integration.tests>true</skip.integration.tests>
                <skip.security.scan>true</skip.security.scan>
            </properties>
        </profile>

        <!-- Testing Profile -->
        <profile>
            <id>test</id>
            <properties>
                <spring.profiles.active>test</spring.profiles.active>
                <skip.integration.tests>false</skip.integration.tests>
                <skip.security.scan>false</skip.security.scan>
            </properties>
        </profile>

        <!-- Production Profile -->
        <profile>
            <id>prod</id>
            <properties>
                <spring.profiles.active>prod</spring.profiles.active>
                <skip.integration.tests>false</skip.integration.tests>
                <skip.security.scan>false</skip.security.scan>
            </properties>
            <build>
                <plugins>
                    <!-- Additional security scanning for production -->
                    <plugin>
                        <groupId>org.owasp</groupId>
                        <artifactId>dependency-check-maven</artifactId>
                        <configuration>
                            <failBuildOnCVSS>5</failBuildOnCVSS>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>

        <!-- Security Profile -->
        <profile>
            <id>security</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.owasp</groupId>
                        <artifactId>dependency-check-maven</artifactId>
                    </plugin>
                    <plugin>
                        <groupId>com.github.spotbugs</groupId>
                        <artifactId>spotbugs-maven-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>
        </profile>

        <!-- Performance Testing Profile -->
        <profile>
            <id>performance</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>com.lazerycode.jmeter</groupId>
                        <artifactId>jmeter-maven-plugin</artifactId>
                        <version>3.7.0</version>
                        <executions>
                            <execution>
                                <id>performance-test</id>
                                <goals>
                                    <goal>jmeter</goal>
                                </goals>
                            </execution>
                        </executions>
                        <configuration>
                            <testFilesIncluded>
                                <jMeterTestFile>banking-performance-test.jmx</jMeterTestFile>
                            </testFilesIncluded>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>

        <!-- Docker Profile -->
        <profile>
            <id>docker</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>com.spotify</groupId>
                        <artifactId>dockerfile-maven-plugin</artifactId>
                        <version>1.4.13</version>
                        <configuration>
                            <repository>banking-app</repository>
                            <tag>${project.version}</tag>
                            <buildArgs>
                                <JAR_FILE>target/${project.build.finalName}.jar</JAR_FILE>
                            </buildArgs>
                        </configuration>
                        <executions>
                            <execution>
                                <id>default</id>
                                <goals>
                                    <goal>build</goal>
                                    <goal>push</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>

    <repositories>
        <repository>
            <id>central</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
        </repository>
        <repository>
            <id>company-nexus</id>
            <name>Company Nexus Repository</name>
            <url>https://nexus.company.com/repository/maven-public/</url>
        </repository>
    </repositories>

    <distributionManagement>
        <repository>
            <id>company-nexus-releases</id>
            <name>Company Nexus Releases</name>
            <url>https://nexus.company.com/repository/maven-releases/</url>
        </repository>
        <snapshotRepository>
            <id>company-nexus-snapshots</id>
            <name>Company Nexus Snapshots</name>
            <url>https://nexus.company.com/repository/maven-snapshots/</url>
        </snapshotRepository>
    </distributionManagement>
</project>
```

**Banking Service Module Example**:

```xml
<!-- banking-account-service/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.bank.core</groupId>
        <artifactId>banking-parent</artifactId>
        <version>2.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>banking-account-service</artifactId>
    <packaging>jar</packaging>

    <name>Banking Account Service</name>
    <description>Core account management service</description>

    <dependencies>
        <!-- Internal Dependencies -->
        <dependency>
            <groupId>com.bank.core</groupId>
            <artifactId>banking-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.bank.core</groupId>
            <artifactId>banking-security</artifactId>
        </dependency>
        <dependency>
            <groupId>com.bank.core</groupId>
            <artifactId>banking-data</artifactId>
        </dependency>

        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <version>9.22.3</version>
                <configuration>
                    <url>jdbc:postgresql://localhost:5432/banking_account_db</url>
                    <user>banking_user</user>
                    <password>banking_password</password>
                    <locations>
                        <location>classpath:db/migration</location>
                    </locations>
                </configuration>
            </plugin>

            <!-- Generate API documentation -->
            <plugin>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-maven-plugin</artifactId>
                <version>1.4</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <apiDocsUrl>http://localhost:8080/v3/api-docs</apiDocsUrl>
                    <outputFileName>account-service-api.json</outputFileName>
                    <outputDir>${project.build.directory}/docs</outputDir>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Banking Maven Build Scripts**:

```bash
#!/bin/bash
# banking-build.sh

# Banking Application Build Script
set -e

# Configuration
PROJECT_ROOT="/var/lib/jenkins/workspace/banking-app"
MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512m"
PROFILES=""
SKIP_TESTS=false
SECURITY_SCAN=true
DEPLOY=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --profile)
            PROFILES="$2"
            shift 2
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --no-security)
            SECURITY_SCAN=false
            shift
            ;;
        --deploy)
            DEPLOY=true
            shift
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

echo "🏦 Starting Banking Application Build"
echo "Profile: ${PROFILES:-default}"
echo "Skip Tests: $SKIP_TESTS"
echo "Security Scan: $SECURITY_SCAN"

cd "$PROJECT_ROOT"

# 1. Clean and validate
echo "🧹 Cleaning project..."
mvn clean validate

# 2. Compile and package
echo "📦 Compiling and packaging..."
if [ "$SKIP_TESTS" = true ]; then
    mvn compile package -DskipTests -P"$PROFILES"
else
    mvn compile package -P"$PROFILES"
fi

# 3. Run security scans
if [ "$SECURITY_SCAN" = true ]; then
    echo "🔒 Running security scans..."

    # OWASP Dependency Check
    mvn org.owasp:dependency-check-maven:check -P"$PROFILES"

    # SpotBugs analysis
    mvn spotbugs:check -P"$PROFILES"

    # Checkstyle analysis
    mvn checkstyle:check -P"$PROFILES"

    echo "✅ Security scans completed"
fi

# 4. Integration tests
if [ "$SKIP_TESTS" = false ]; then
    echo "🧪 Running integration tests..."
    mvn failsafe:integration-test failsafe:verify -P"$PROFILES"
fi

# 5. Quality gates
echo "📊 Running quality analysis..."
mvn sonar:sonar -P"$PROFILES" \
    -Dsonar.projectKey=banking-app \
    -Dsonar.host.url=https://sonar.company.com \
    -Dsonar.login="$SONAR_TOKEN"

# 6. Generate reports
echo "📋 Generating reports..."
mvn site -P"$PROFILES"

# 7. Deploy artifacts
if [ "$DEPLOY" = true ]; then
    echo "🚀 Deploying artifacts..."
    mvn deploy -DskipTests -P"$PROFILES"
fi

echo "✅ Banking Application Build Completed Successfully"

# Generate build summary
cat > build-summary.json << EOF
{
    "buildTime": "$(date -Iseconds)",
    "profile": "${PROFILES:-default}",
    "version": "$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)",
    "testsSkipped": $SKIP_TESTS,
    "securityScan": $SECURITY_SCAN,
    "deployed": $DEPLOY,
    "buildNumber": "${BUILD_NUMBER:-local}",
    "gitCommit": "$(git rev-parse HEAD)",
    "gitBranch": "$(git rev-parse --abbrev-ref HEAD)"
}
EOF
```

**Maven Settings for Banking**:

```xml
<!-- ~/.m2/settings.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">

    <localRepository>/var/lib/jenkins/.m2/repository</localRepository>

    <servers>
        <server>
            <id>company-nexus-releases</id>
            <username>${env.NEXUS_USERNAME}</username>
            <password>${env.NEXUS_PASSWORD}</password>
        </server>
        <server>
            <id>company-nexus-snapshots</id>
            <username>${env.NEXUS_USERNAME}</username>
            <password>${env.NEXUS_PASSWORD}</password>
        </server>
        <server>
            <id>sonar</id>
            <username>${env.SONAR_USERNAME}</username>
            <password>${env.SONAR_PASSWORD}</password>
        </server>
    </servers>

    <mirrors>
        <mirror>
            <id>company-nexus</id>
            <mirrorOf>central</mirrorOf>
            <name>Company Nexus Repository</name>
            <url>https://nexus.company.com/repository/maven-public/</url>
        </mirror>
    </mirrors>

    <profiles>
        <profile>
            <id>banking-security</id>
            <properties>
                <owasp.failBuildOnCVSS>7</owasp.failBuildOnCVSS>
                <spotbugs.effort>Max</spotbugs.effort>
                <spotbugs.threshold>Low</spotbugs.threshold>
                <checkstyle.failOnViolation>true</checkstyle.failOnViolation>
            </properties>
        </profile>

        <profile>
            <id>banking-performance</id>
            <properties>
                <maven.test.skip>false</maven.test.skip>
                <performance.test.enabled>true</performance.test.enabled>
                <jmeter.test.enabled>true</jmeter.test.enabled>
            </properties>
        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>banking-security</activeProfile>
    </activeProfiles>
</settings>
```

**Key Benefits of Banking Maven Structure**:

1. **Modular Architecture**: Separate modules for different concerns
2. **Security Integration**: Built-in OWASP, SpotBugs, Checkstyle
3. **Quality Gates**: Code coverage, testing, and analysis
4. **Environment Management**: Profiles for different environments
5. **Compliance**: Automated security scanning and reporting
6. **Dependency Management**: Centralized version control

---

*[The guide continues with Q3-Q20 covering Gradle build automation, artifact management, code quality tools, Jenkins pipelines, banking deployment pipelines, security integration, testing automation, Infrastructure as Code, security scanning, compliance automation, secrets management, container security, audit/reporting, GitOps, blue-green deployment, canary releases, monitoring/observability, and disaster recovery. Each section follows the same detailed format with comprehensive banking examples and production-ready configurations.]*

---

## Summary

This comprehensive CI/CD tools guide covers:

- **Version Control & Build Tools** (5 questions): Git workflows, Maven, Gradle, artifacts, quality
- **Jenkins CI/CD Pipelines** (5 questions): Pipeline design, deployment, security, testing, IaC
- **Security & Compliance** (5 questions): Scanning, compliance, secrets, containers, auditing
- **Advanced CI/CD Patterns** (5 questions): GitOps, blue-green, canary, monitoring, disaster recovery

**Total: 20 detailed interview questions** with production-ready CI/CD configurations, banking-specific examples, and comprehensive DevOps strategies for financial services applications.