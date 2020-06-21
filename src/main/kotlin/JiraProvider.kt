@file:Suppress("unused")

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.BasicProject
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import java.net.URI


enum class JiraIssueType(val issueTypeId: Long) {
    Story(10001),
    Bug(10004),
    Epic(10000),
    Task(10002),
    Sub_task(10003)
}

class JiraProvider(
    val url: String,
    val userName: String,
    val password: String
) {

    val client: JiraRestClient

    init {
        client = open()!!
    }

    private fun open(): JiraRestClient? {

        return AsynchronousJiraRestClientFactory()
            .createWithBasicHttpAuthentication(URI(url), userName, password)
    }

    fun getAllProject(): List<BasicProject> {
        return client.projectClient.allProjects.claim().map { basicProject -> basicProject }
    }

    fun getAllIssue(projectKey: String, issueType: JiraIssueType? = null): List<Issue> {
        val jql = if (issueType == null) {
            "project = \"$projectKey\""
        } else if (issueType == JiraIssueType.Sub_task) {
            "project = \"$projectKey\" issuetype = \"Sub-task\" "
        } else {
            "project = \"$projectKey\" issuetype = \"${issueType.name}\" "
        }
        return client.searchClient.searchJql(jql).claim().issues.toList()
    }

    fun searchIssues(projectKey: String, jql: String): List<Issue> {
        return client.searchClient.searchJql("project = \"$projectKey\" AND $jql").claim().issues.toList()
    }

    fun getIssue(issueKey: String): Issue? {
        return client.issueClient.getIssue(issueKey).claim()
    }

    fun createIssue(issueInputBuilder: IssueInputBuilder): String? {
        val build = issueInputBuilder.build()
        return client.issueClient.createIssue(build).claim().key
    }

    fun updateIssue(issueKey: String, issueInputBuilder: IssueInputBuilder) {
        client.issueClient.updateIssue(issueKey, issueInputBuilder.build())
    }


}


fun main() {
    val client = JiraProvider(
        "https://datafirsttech.atlassian.net",
        "m_bansal10@yahoo.com",
        "nviDdThmi2eYZDYsTT1NC9C3"
    )

    //quick testing all get method
    client.getAllProject().forEach(::println)
    client.getAllIssue("TP").forEach { println(" all issue : " + it.key) }
    client.searchIssues("TP", "component = UI").forEach { println("UI issue " + it.key) }
    println("TP1 issue " + client.getIssue("TP-1")?.key)

    //create issue
    val issueInputBuilder = IssueInputBuilder("TP", JiraIssueType.Story.issueTypeId, "Issue created using java api")
    client.createIssue(issueInputBuilder)


    //quik testing all set method

}
