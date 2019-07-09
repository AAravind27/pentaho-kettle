import groovy.transform.Field

@Field private buildData = null

void config(Map defaultParams = [:]) {
  buildData = config.load(defaultParams)
  config.applyToJob(defaultParams)

  log.info "Build Data loaded for: ${buildData.get('BUILD_PLAN_ID')}", buildData
}

def getJobParameters(String buildJobName) {

  // Unless overrode by RELEASE_BUILD_NUMBER, the release build number is determined from the job to be called not the
  // pipeline job running
  String releaseBuildNumber = params.RELEASE_BUILD_NUMBER ?: job.nextBuildNumber(buildJobName)

  List parameterNames = [
    'NOOP', 'BUILD_PLAN_ID', 'FIRST_JOB', 'LAST_JOB', 'CLEAN_ALL_CACHES', 'CLEAN_SCM_WORKSPACES',
    'RUN_BUILDS', 'RUN_CHECKOUTS', 'RUN_VERSIONING', 'RUN_UNIT_TESTS', 'ARCHIVE_ARTIFACTS',
    'RELEASE_BUILD_ID', 'RELEASE_VERSION', 'DEPLOYMENT_FOLDER', 'SUITE_BUILD_RESOURCES_BRANCH',
    'MAVEN_PUBLIC_RELEASE_REPO_URL', 'MAVEN_PUBLIC_SNAPSHOT_REPO_URL',
    'MAVEN_PRIVATE_RELEASE_REPO_URL', 'MAVEN_PRIVATE_SNAPSHOT_REPO_URL', 'MAVEN_RESOLVE_REPO_URL',
    'PREVIOUS_RELEASE_VERSION', 'SP_RELEASE_VERSION', 'SERVICEPACK_BRANCH', 'DO_AUTO_PATCH',
    'JENKINS_FOLDER_NAME', 'BUILD_HOSTING_ROOT', 'SLACK_CHANNEL', 'BUILD_SLACK_CHANNEL',
    'VERSION_MERGER_VERSION', 'BUILD_DATA_FILE', 'BUILD_VERSIONS_FILE', 'PIPELINE_URL',
    'PIPELINE_BRANCH', 'OVERRIDE_PARAMS']

  Map parameters = buildData.buildProperties.subMap(parameterNames)
  parameters['RELEASE_BUILD_NUMBER'] = releaseBuildNumber
  parameters['BUILD_ID_TAIL'] = "-${releaseBuildNumber}"

  return parameters
}

def notifyBuildResult(def buildStatus) {
  if (buildData.getBool('SLACK_INTEGRATION')) {
    def color
    if (buildStatus == "SUCCESS") {
      color = 'good'
    } else if (buildStatus == "UNSTABLE") {
      color = 'warning'
    } else {
      color = 'danger'
    }
    slackChannel = params.BUILD_SLACK_CHANNEL ?: ''
    slackSend(color: color, message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", channel: slackChannel)
  }
}

return this