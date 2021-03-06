package io.github.dmitrychaban.toggl2jira.controller

import io.github.dmitrychaban.java_jira_sdk_light.DefaultJiraApi
import io.github.dmitrychaban.java_jira_sdk_light.model.Issue
import io.github.dmitrychaban.java_jira_sdk_light.model.JiraProject
import io.github.dmitrychaban.java_toggl_sdk_light.TogglApiBuilder
import io.github.dmitrychaban.java_toggl_sdk_light.model.Timer
import org.joda.time.DateTime
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/summary")
class SummaryController {

    @GetMapping
    fun getSummary(): Mono<List<Pair<Issue, List<Timer>>>> {
        //TODO: Replace manual token fetching with bean
        val curRequest = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val jiraToken = curRequest.getHeader("jiraToken")
        val jiraMail = curRequest.getHeader("jiraMail")
        val jiraApi = DefaultJiraApi(jiraToken, jiraMail)
        val togglToken = curRequest.getHeader("togglToken")
        val togglApi = TogglApiBuilder.with(togglToken).build()

        return Mono.create {

            val projects: List<JiraProject> = jiraApi.getJiraProjects().flatMapMany { Flux.fromIterable(it) }.collectList().block()!!
            val workspaces = togglApi.context.block()!!.workspaces
            togglApi.currentWorkspace = workspaces.first()
            val timeReport = togglApi.getWeekReport(DateTime.now()).block()!!
            val allIssues = projects.map { jiraApi.getTasksByProject(it, System.currentTimeMillis(), 100) }.map { it.block()!!.issues }.flatten()
            val comparedObjects = allIssues
                    .map {
                        val togglTitle = "${it.key} ${it.fields.summary}"
                        val groupedElements = timeReport.data.groupBy { it.description }
                        if (groupedElements.containsKey(togglTitle)) {
                            return@map Pair(it, groupedElements[togglTitle])
                        } else {
                            return@map Pair(it, null)
                        }
                    }
                    .filter { it.second != null }.map { Pair(it.first!!, it.second!!) }
            it.success(comparedObjects)
        }

    }
}
