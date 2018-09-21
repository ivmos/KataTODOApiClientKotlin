package com.karumi.todoapiclient

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import todoapiclient.TodoApiClient
import todoapiclient.dto.TaskDto
import todoapiclient.exception.ItemNotFoundError
import todoapiclient.exception.UnknownApiError

class TodoApiClientTest : MockWebServerTest() {

    private lateinit var apiClient: TodoApiClient

    @Before
    override fun setUp() {
        super.setUp()
        val mockWebServerEndpoint = baseEndpoint
        apiClient = TodoApiClient(mockWebServerEndpoint)
    }

    @Test
    fun sendsAcceptAndContentTypeHeaders() {
        enqueueMockResponse(200, "getTasksResponse.json")

        apiClient.allTasks

        assertRequestContainsHeader("Accept", "application/json")
    }

    @Test
    fun sendsGetAllTaskRequestToTheCorrectEndpoint() {
        enqueueMockResponse(200, "getTasksResponse.json")

        apiClient.allTasks

        assertGetRequestSentTo("/todos")
    }

    @Test
    fun parsesTasksProperlyGettingAllTheTasks() {
        enqueueMockResponse(200, "getTasksResponse.json")

        val tasks = apiClient.allTasks.component2()!!

        assertEquals(200, tasks.size.toLong())
        assertTaskContainsExpectedValues(tasks[0])
    }

    @Test
    fun `Get all tasks and server returns 500`() {
        enqueueMockResponse(500, "getTasksResponse.json")

        assertTrue(apiClient.allTasks.component1() is UnknownApiError)
        //assertNull(apiClient.allTasks.component2())
        //val tasks = apiClient.allTasks.component2()!!

        //assertEquals(200, tasks.size.toLong())
        //assertTaskContainsExpectedValues(tasks[0])
    }

    @Test
    fun returnsAnUnknownApiErrorIfTheServerFails() {
        enqueueMockResponse(500)

        val error = apiClient.allTasks.component1()!!

        assertEquals(error, UnknownApiError(500))
    }

    /*
    @Test
    fun returnsAnUnknownApiErrorIfMalformedJson() {
        enqueueMockResponse(200, "malformedJson.json")

        // TODO this gives illegalstateexception, fix prod code and PR
        val error = apiClient.allTasks.component1()!!

        assertEquals(error, UnknownApiError(500))
    }
    */

    @Test
    fun `getTaskById success headers`() {
        enqueueMockResponse(200, "getTaskByIdResponse.json")

        apiClient.getTaskById("1")

        assertRequestContainsHeader("Accept", "application/json")
    }

    @Test
    fun `getTaskById success`() {
        enqueueMockResponse(200, "getTaskByIdResponse.json")

        val task = apiClient.getTaskById("1")
        assertEquals(task.component2()?.id, "1")
        assertRequestSentTo("/todos/1")
    }

    @Test
    fun `getTaskById not found`() {
        enqueueMockResponse(404)

        val task = apiClient.getTaskById("3243432")

        assertTrue(task.component1() is ItemNotFoundError)
    }

    @Test
    fun `getTaskById is performed with GET`() {
        enqueueMockResponse(200, "getTaskByIdResponse.json")

        val task = apiClient.getTaskById("1")
        assertEquals(task.component2()?.id, "1")
        assertGetRequestSentTo("/todos/1")
    }

    /*
    @Test
    fun `addTask success`() {
        enqueueMockResponse(200, "addTaskResponse")
        val taskDto = TaskDto("2", "dsds", "any title", false)
        val resultTaskDto = apiClient.addTask(taskDto)
    }
    */

    private fun assertTaskContainsExpectedValues(task: TaskDto?) {
        assertTrue(task != null)
        assertEquals(task?.id, "1")
        assertEquals(task?.userId, "1")
        assertEquals(task?.title, "delectus aut autem")
        assertFalse(task!!.isFinished)
    }
}
