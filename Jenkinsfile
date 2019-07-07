#!groovy

@Library('visibilityLibs')
import com.liaison.jenkins.visibility.Utilities
import com.liaison.jenkins.common.testreport.TestResultsUploader
import com.liaison.jenkins.common.sonarqube.QualityGate
import com.liaison.jenkins.common.kubernetes.*
import com.liaison.jenkins.common.e2etest.*
import com.liaison.jenkins.common.servicenow.ServiceNow
import com.liaison.jenkins.common.slack.*

def deployments = new Deployments()
def k8sDocker = new Docker()
def kubectl = new Kubectl()
def serviceNow = new ServiceNow()
def slack = new Slack()

def utils = new Utilities()
def uploadUtil = new TestResultsUploader()
def qg = new QualityGate()
def e2etest = new E2eTestDeployer()
def testSessions = new TestSessions()

def deployment
def dockerImageName = "alloy/conveyor-server"
def sonarProjectName = "conveyor-server"

timestamps {

    node {

        stage('Checkout') {
            checkout scm
            env.PACKAGE_NAME = utils.runSh('sh scripts/projectname.sh')
            //env.VERSION = utils.runSh('sh scripts/projectversion.sh')
            env.VERSION = utils.runSh("awk '/^## \\[([0-9])/{ print (substr(\$2, 2, length(\$2) - 2));exit; }' CHANGELOG.md")
            env.GIT_COMMIT = utils.runSh('git rev-parse HEAD')
            env.GIT_URL = utils.runSh("git config remote.origin.url | sed -e 's/\\(.git\\)*\$//g' ")
            env.REPO_NAME = utils.runSh("basename -s .git ${env.GIT_URL}")
            env.RELEASE_NOTES = utils.runSh("awk '/## \\[${env.VERSION}\\]/{flag=1;next}/## \\[/{flag=0}flag' CHANGELOG.md")
            currentBuild.displayName = env.VERSION

            deployment = deployments.create(
                    name: 'Alloy Conveyor Server Microservice',
                    version: env.VERSION,
                    description: 'Alloy Kubernetes deployment for Conveyor Server',
                    dockerImageName: dockerImageName,
                    dockerImageTag: env.VERSION,
                    yamlFile: 'K8sfile.yaml',   // optional, defaults to 'K8sfile.yaml'
                    gitUrl: env.GIT_URL,        // optional, defaults to env.GIT_URL
                    gitCommit: env.GIT_COMMIT,   // optional, defaults to env.GIT_COMMIT
                    kubectl: kubectl
            )
        }

        stage('Build') {
            sh './gradlew build -x test'
        }

        stage('Test') {

            sh './gradlew test consolidateTestResults'
            if (utils.isMasterBuild()) {
                def unitTestInfo = uploadUtil.uploadResults(
                        "${env.REPO_NAME}",
                        "${env.VERSION}",
                        "DEVINT",
                        "Conveyor Service Unit Test Results",
                        "Unit test",
                        "UNIT",
                        ["./service-implementation/build/test-results/consolidated/TEST-G2_Conveyor_Server_Test_Results-consolidated.xml"]
                )
                UT_REPORT_URL = unitTestInfo
            }

        }

        //JENKIN jobs are failing where it couldn't download the dependency check files
        stage('Code analysis') {
            timeout(75) {
                withSonarQubeEnv('Sonarqube-k8s') {
                    sh "./gradlew sonarqube -x test --info"
                }
            }
        }

        stash name: "build", includes: "K8sfile.yaml, Dockerfile," +
                "service-implementation/build/libs/edi-service*.war," +
                "service-implementation/build/resources/main/*.properties," +
                "service-implementation/build/resources/main/log4j2*.xml," +
                "service-implementation/build/resources/main/thread-shutdown.conf," +
                "service-implementation/build/resources/main/*.sh"

        stage('Quality gate') {
            qgStatus = qg.checkQualityGate(env.REPO_NAME)
        }
    }

    stage('Build Docker image') {

        node {
            unstash name: "build"

            k8sDocker.build(imageName: dockerImageName)
            milestone label: 'Docker image built', ordinal: 100

            if (utils.isMasterBuild()) {
                k8sDocker.push(imageName: dockerImageName, imageTag: env.VERSION)
            }
        }
    }

    if (utils.isMasterBuild()) {

        stage('Deploy to Kubernetes, dev namespace') {

            try {
                deployments.deploy(
                        deployment: deployment,
                        kubectl: kubectl,
                        serviceNow: serviceNow,
                        namespace: Namespace.DEVELOPMENT,
                        rollingUpdate: true     // optional, defaults to true
                )
            } catch (err) {
                currentBuild.result = "FAILURE"
                error "${err}"
            }
        }

        /*stage('E2E tests in DEV') {
            def testSummary
            // Because of static Alloy platforms, E2E test container deployment must happen
            // in a dedicated agent having number of executors limited to 1
            node('e2e-tests-dev') {
                def testSession = testSessions.create(
                        project: "${env.REPO_NAME}",
                        version: "${env.VERSION}",
                        title: 'DEV E2E Test Results',
                        testType: 'E2E',
                        includeGroups: 'edi-service'
                )

                def e2eTestDockerImageName = "alloy-integration-e2e-tests"
                testSummary = e2etest.runTestsInKubernetes(e2eTestDockerImageName, Namespace.DEVELOPMENT, testSession)
            }

            E2E_REPORT_URL_DEV = testSummary.reportUrl
            E2E_REPORT_SUMMARY_DEV = "${testSummary.status} | Success Rate: ${testSummary.successRate}% | Tests: ${testSummary.testsCount}, Passed: ${testSummary.passedCount}, Failed: ${testSummary.failureCount}, Skipped: ${testSummary.skippedCount}, Errors: ${testSummary.errorCount}."
            // Worknote must indicate if tests failed or passed
            if (testSummary.status.contains("FAILED")) {
                serviceNow.addWorknote(
                        deployment: deployment,
                        comment: "DEV E2E tests failed",
                        testResultsUrl: "${E2E_REPORT_URL_DEV}"    // optional
                )
            }
            else if (testSummary.status.contains("PASSED")) {
                serviceNow.addWorknote(
                        deployment: deployment,
                        comment: "DEV E2E tests passed",
                        testResultsUrl: "${E2E_REPORT_URL_DEV}"    // optional
                )
            }
        }

        stage('Promote to QA') {

            def msg = """\
                @here: Approve promotion of <${env.JOB_URL}|${env.JOB_NAME} #${env.BUILD_NUMBER}> to QA?
                - version ${env.VERSION}
                - <${UT_REPORT_URL}|Unit Test Results>
                - <${E2E_REPORT_URL_DEV}|E2E Test Results in DEV>: ${E2E_REPORT_SUMMARY_DEV}
                - SonarQube <https://at4ch.liaison.dev/sonarqube/dashboard?id=com.liaison.alloy.service:${sonarProjectName}|results>
                - Release Notes for ${env.VERSION}: ``` ${env.RELEASE_NOTES} ```
                """.stripIndent()

            def approval = slack.waitForApproval(
                    channel: Slackchannel.DEV_SIGNOFF,
                    message: msg,
                    question: "Promote this build to QA?",
                    timeoutValue: 48,       // optional, defaults to 24
                    timeoutUnit: 'HOURS'    // optional, defaults to "HOURS"
            )

            if (!approval.isApproved()) {
                currentBuild.result = "ABORTED";
                slack.error(
                        channel: Slackchannel.DEV_SIGNOFF,
                        message: "@here: *${env.JOB_NAME} v${env.VERSION}* (build #${env.BUILD_NUMBER}) - QA deployment canceled by ${approval.user}"
                )
                serviceNow.cancel(
                        deployment: deployment,
                        comment: "QA deployment canceled by ${approval.user}"
                )
                error "QA deployment canceled by ${approval.user}"
            }

            serviceNow.promote(
                    deployment: deployment,
                    namespace: Namespace.QA,
                    approveUser: approval.user,
                    approveComment: ""      // optional, defaults to ""
            )

            milestone label: 'Promoted to QA by ${approval.user}', ordinal: 400
        }

        stage('Accept to QA') {

            def msg = """\
                @here: Approve promotion of <${env.JOB_URL}|${env.JOB_NAME} #${env.BUILD_NUMBER}> is waiting to be accepted to QA.
                - version ${env.VERSION}
                - <${UT_REPORT_URL}|Unit Test Results>
                - <${E2E_REPORT_URL_DEV}|E2E Test Results in DEV>: ${E2E_REPORT_SUMMARY_DEV}
                - SonarQube <https://at4ch.liaison.dev/sonarqube/dashboard?id=com.liaison.alloy.service:${sonarProjectName}|results>
                - Release Notes for ${env.VERSION}: ``` ${env.RELEASE_NOTES} ```
                """.stripIndent()

            approval = slack.waitForApproval(
                    channel: Slackchannel.QA_APPROVALS,
                    message: msg,
                    question: "Accept this build to QA?",
                    timeoutValue: 48,       // optional, defaults to 24
                    timeoutUnit: 'HOURS'    // optional, defaults to "HOURS"
            )

            if (!approval.isApproved()) {
                currentBuild.result = "ABORTED"
                slack.error(
                        channel: Slackchannel.QA_APPROVALS,
                        message: "@here: *${env.JOB_NAME} v${env.VERSION}* (build #${env.BUILD_NUMBER}) - QA deployment rejected by ${approval.user}"
                )
                serviceNow.cancel(
                        deployment: deployment,
                        comment: "QA deployment rejected by ${approval.user}"
                )
                error "QA deployment rejected by ${approval.user}"
            }

            milestone label: 'Accepted to QA ${approval.user}', ordinal: 600
        }

        stage('Deploy to Kubernetes, QA namespace') {

            serviceNow.addWorknote(
                    deployment: deployment,
                    comment: "Accepted to QA by ${approval.user}"
            )

            try {
                deployments.deploy(
                        deployment: deployment,
                        kubectl: kubectl,
                        serviceNow: serviceNow,
                        namespace: Namespace.QA,
                        rollingUpdate: true     // optional, defaults to true
                )
            } catch (err) {
                currentBuild.result = "FAILURE";
                error "${err}"
            }
        }

        stage('E2E tests in QA') {
            def testSummary
            // Because of static Alloy platforms, E2E test container deployment must happen
            // in a dedicated agent having number of executors limited to 1
            node('e2e-tests-qa') {
                def testSession = testSessions.create(
                        project: "${env.REPO_NAME}",
                        version: "${env.VERSION}",
                        title: 'QA E2E Test Results',
                        testType: 'E2E',
                        includeGroups: 'edi-service'
                )

                def e2eTestDockerImageName = "alloy-integration-e2e-tests"
                testSummary = e2etest.runTestsInKubernetes(e2eTestDockerImageName, Namespace.QA, testSession)
            }

            E2E_REPORT_URL_QA = testSummary.reportUrl
            E2E_REPORT_SUMMARY_QA = "${testSummary.status} | Success Rate: ${testSummary.successRate}% | Tests: ${testSummary.testsCount}, Passed: ${testSummary.passedCount}, Failed: ${testSummary.failureCount}, Skipped: ${testSummary.skippedCount}, Errors: ${testSummary.errorCount}."
            // Worknote must indicate if tests failed or passed
            if (testSummary.status.contains("FAILED")) {
                serviceNow.addWorknote(
                        deployment: deployment,
                        comment: "QA E2E tests failed",
                        testResultsUrl: "${E2E_REPORT_URL_QA}"    // optional
                )
            }
            else if (testSummary.status.contains("PASSED")) {
                serviceNow.addWorknote(
                        deployment: deployment,
                        comment: "QA E2E tests passed",
                        testResultsUrl: "${E2E_REPORT_URL_QA}"    // optional
                )
            }
        }*/

        /*
        TODO - Add Acceptance Tests!
        stage('Acceptance tests') {
            //Deploy Robot Framework example test.
            //NOTE: Nothing is done with the testSummary yet. This will be implemented soon.
            //This is merely to show how to run tests from a Jenkins pipeline, even if we can't get the testSummary yet.
            def testSummary
            // Because of static Alloy platforms, E2E test container deployment must happen
            // in a dedicated agent having number of executors limited to 1
            node('e2e-tests-qa') {
                def testSession = testSessions.create(
                        project: "${env.REPO_NAME}",
                        version: "${env.VERSION}",
                        title: 'Example Robot Framework E2E Test Results',
                        testType: 'E2E'
                )

                def e2eTestDockerImageName = "alloy-robotframework-tests-example"
                testSummary = e2etest.runTestsInKubernetes(e2eTestDockerImageName, Namespace.QA, testSession)
            }
        }
        */


        /*stage('Performance tests') {
        }*/

        /*stage('QA sign-off') {

            def msg = """\
                @here: <${env.JOB_URL}|${env.JOB_NAME} #${env.BUILD_NUMBER}> is waiting to be signed off to STAGING.
                - version ${env.VERSION}
                - <${UT_REPORT_URL}|Unit Test Results>
                - <${E2E_REPORT_URL_QA}|E2E Test Results in QA>: ${E2E_REPORT_SUMMARY_QA}
                - SonarQube <https://at4ch.liaison.dev/sonarqube/dashboard?id=com.liaison.alloy.service:${sonarProjectName}|results>
                """.stripIndent()

            approval = slack.waitForApproval(
                    channel: Slackchannel.QA_APPROVALS,
                    message: msg,
                    question: "Sign-off by QA and promote to STAGING?",
                    timeoutValue: 240,       // optional, defaults to 24
                    timeoutUnit: 'HOURS'    // optional, defaults to "HOURS"
            )

            if (!approval.isApproved()) {
                currentBuild.result = "ABORTED";
                slack.error(
                        channel: Slackchannel.QA_APPROVALS,
                        message: "@here: *${env.JOB_NAME} v${env.VERSION}* (build #${env.BUILD_NUMBER}) - QA sign-off rejected by ${approval.user}"
                )
                serviceNow.cancel(
                        deployment: deployment,
                        comment: "QA sign-off rejected by ${approval.user}"
                )
                error "QA sign-off rejected by ${approval.user}"
            }

            serviceNow.promote(
                    deployment: deployment,
                    namespace: Namespace.STAGING,
                    approveUser: approval.user,
                    approveComment: ""
            )

            // Label all stored test reports from the build as 'QA Sign-off' in the QA Reporter.
            // Call once from the pipeline. Either after QA sign-off has been approved OR after
            // Staging sign if there is such stage defined.
            uploadUtil.signOffReports("${env.REPO_NAME}", "${env.VERSION}")

            milestone label: 'QA sign-off by ${approval.user}', ordinal: 800
        }

        stage('deploy to Kubernetes, Staging') {

            node {
                k8sDocker.publish(
                        imageToPublish: "${deployment.dockerImageName()}:${deployment.dockerImageTag()}",
                        publishedImageName: deployment.dockerImageName(),
                        publishedImageTag: deployment.dockerImageTag()
                )
            }

            serviceNow.addWorknote(
                    deployment: deployment,
                    comment: "Docker image published to PROD registry"
            )

            try {
                deployments.deploy(
                        deployment: deployment,
                        kubectl: kubectl,
                        serviceNow: serviceNow,
                        namespace: Namespace.STAGING,
                        rollingUpdate: true     // optional, defaults to true
                )
            } catch (err) {
                currentBuild.result = "FAILURE";
                error "${err}"
            }
        }

        stage('deploy to Kubernetes, UAT') {

            def crApproved = serviceNow.waitForApproval(correlationId: deployment.gitCommit(), namespace: Namespace.UAT_TEST)

            if (crApproved) {
                try {
                    deployments.deploy(
                            deployment: deployment,
                            kubectl: kubectl,
                            serviceNow: serviceNow,
                            namespace: Namespace.UAT,
                            rollingUpdate: true     // optional, defaults to true
                    )
                } catch (err) {
                    currentBuild.result = "FAILURE";
                    error "${err}"
                }
                slack.info(
                        channel: Slackchannel.AE_APPROVALS,
                        message: "@here: *${env.JOB_NAME} v${env.VERSION}* (build #${env.BUILD_NUMBER}) was deployed to UAT."
                )
            } else {
                currentBuild.result = "ABORTED";
                slack.error(
                        channel: Slackchannel.AE_APPROVALS,
                        message: "@here: *${env.JOB_NAME} v${env.VERSION}* (build #${env.BUILD_NUMBER}) - UAT deployment rejected in ServiceNow"
                )
                error "UAT deployment rejected in ServiceNow"
            }
        }*/
    }
}
