package ru.evlitvin.servlet.api.v1;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.evlitvin.dto.SchoolDto;
import ru.evlitvin.service.SchoolService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests for SchoolServlet class")
class SchoolServletTest {

    @Mock
    private SchoolService schoolService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private SchoolServlet schoolServlet;

    private final Gson gson = new Gson();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("SchoolServlet init method test")
    void initTest() {
        SchoolServlet servlet = new SchoolServlet();
        servlet.init();
        assertNotNull(servlet);
    }

    @Test
    @DisplayName("SchoolServlet doPost school")
    void doPostTest() throws IOException, SQLException {
        String schoolJson = "{\"name\":\"School One name\",\"address\":\"School One address\"}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        schoolServlet.doPost(request, response);
        verify(schoolService, times(1)).save(any(SchoolDto.class));
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    @DisplayName("SchoolServlet doPost request with null school name")
    void doPostWithNullNameTest() throws IOException {
        String schoolJson = "{\"name\":null, \"address\": \"School One address\"}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        schoolServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
    }

    @Test
    @DisplayName("SchoolServlet doPost request with empty school name")
    void doPostWithEmptyNameTest() throws IOException {
        String schoolJson = "{\"name\":\"\", \"address\": \"School One address\"}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        schoolServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
    }

    @Test
    @DisplayName("SchoolServlet doPost request with null school address")
    void doPostWithNullAddressTest() throws IOException {
        String schoolJson = "{\"name\": \"School One name\",\"address\": null}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        schoolServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
    }

    @Test
    @DisplayName("SchoolServlet doPost request with empty school address")
    void doPostWithEmptyAddressTest() throws IOException {
        String schoolJson = "{\"name\": \"School One name\",\"address\": \"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        schoolServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
    }

    @Test
    @DisplayName("SchoolServlet doGet all schools")
    void doGetAllSchoolsTest() throws ServletException, IOException, SQLException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(request.getPathInfo()).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        SchoolDto schoolDtoOne = new SchoolDto(1L, "School One name", "School One address");
        SchoolDto schoolDtoTwo = new SchoolDto(2L, "School Two name", "School Two address");
        List<SchoolDto> schoolDtos = Arrays.asList(schoolDtoOne, schoolDtoTwo);
        when(schoolService.getAll()).thenReturn(schoolDtos);
        schoolServlet.doGet(request, response);
        verify(schoolService, times(1)).getAll();
        String jsonOutput = gson.toJson(schoolDtos);
        assertEquals(jsonOutput, stringWriter.toString());
    }

    @Test
    @DisplayName("SchoolServlet doGet school by ID")
    void doGetSchoolByIdTest() throws ServletException, IOException, SQLException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(request.getPathInfo()).thenReturn("/1");
        when(response.getWriter()).thenReturn(printWriter);
        SchoolDto schoolDto = new SchoolDto(1L, "School One name", "School One address");
        when(schoolService.getById(1L)).thenReturn(schoolDto);
        schoolServlet.doGet(request, response);
        verify(schoolService, times(1)).getById(1L);
        String jsonOutput = gson.toJson(schoolDto);
        assertEquals(jsonOutput, stringWriter.toString());
    }

    @Test
    @DisplayName("SchoolServlet doPut")
    void doPutTest() throws ServletException, IOException, SQLException {
        String schoolJson = "{\"name\":\"School One name\",\"address\":\"School One address\"}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        when(request.getPathInfo()).thenReturn("/1");
        schoolServlet.doPut(request, response);
        verify(schoolService, times(1)).update(any(SchoolDto.class));
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    @DisplayName("SchoolServlet doPut request with null school name")
    void doPutWithNullNameTest() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/1");
        String schoolJson = "{\"name\":null, \"address\": \"School One address\"}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
    }

    @Test
    @DisplayName("SchoolServlet doPut request with empty school name")
    void doPutWithEmptyNameTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String schoolJson = "{\"name\":\"\", \"address\": \"School One address\"}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
    }

    @Test
    @DisplayName("SchoolServlet doPut request with null school address")
    void doPutNullNameTest() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/1");
        String schoolJson = "{\"name\": \"School One name\",\"address\": null}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
    }

    @Test
    @DisplayName("SchoolServlet doPut request with empty school address")
    void doPutEmptyNameTest() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/1");
        String schoolJson = "{\"name\": \"School One name\",\"address\": \"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(schoolJson));
        when(request.getReader()).thenReturn(reader);
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
    }

    @Test
    @DisplayName("SchoolServlet doPut request without school ID")
    void doPutSchoolWithoutIdTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "School ID required.");
    }

    @Test
    @DisplayName("SchoolServlet doPut request without school ID")
    void doPutSchoolWithIdEmptyTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/");
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "School ID required.");
    }

    @Test
    @DisplayName("SchoolServlet doPut request with invalid school ID")
    void doPutInvalidIdTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/AAA");
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid school ID.");
    }

    @Test
    @DisplayName("SchoolServlet doPut without name and address")
    void doPutWithoutNameAndAddress() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/" + 1L);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"id\":1}")));
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
    }

    @Test
    @DisplayName("SchoolServlet doPut throws SQLException")
    void doPutThroesSQLExceptionTest() throws IOException, SQLException {
        String schoolJson = "{\"name\":\"School One name\",\"address\":\"School One address\"}";
        when(request.getPathInfo()).thenReturn("/" + 1L);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(schoolJson)));
        doThrow(SQLException.class).when(schoolService).update(any(SchoolDto.class));
        assertThrows(ServletException.class, () -> schoolServlet.doPut(request, response));
    }

    @Test
    @DisplayName("SchoolServlet doPut with invalid path")
    void dooPutWithInvalidPathTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "School ID required.");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("SchoolServlet doPut with path ends slash")
    void testDoPutWithSlashPathInfo() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/");
        schoolServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "School ID required.");
        verify(response, never()).setStatus(anyInt());
    }


    @Test
    @DisplayName("SchoolServlet doDelete school by ID")
    void doDeleteTest() throws ServletException, IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/1");

        schoolServlet.doDelete(request, response);

        verify(schoolService, times(1)).delete(1L);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("SchoolServlet doDelete with invalid school ID")
    void doDeleteInvalidIdTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/AAA");

        schoolServlet.doDelete(request, response);

        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid school ID.");
    }

    @Test
    @DisplayName("SchoolServlet doDelete with empty school ID")
    void doDeleteSchoolIdEmptyTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/");

        schoolServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "School ID required.");
    }

    @Test
    @DisplayName("SchoolServlet successful doDelete return NO_CONTENT")
    void doDeleteNoContent() throws ServletException, IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/" + 1);

        schoolServlet.doDelete(request, response);

        verify(schoolService).delete(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("SchoolServlet doDelete throws ServletException")
    void doDeleteSQLExceptionTest() throws SQLException {
        when(request.getPathInfo()).thenReturn("/" + 1);
        doThrow(SQLException.class).when(schoolService).delete(1L);

        assertThrows(ServletException.class, () -> schoolServlet.doDelete(request, response));
    }

    @Test
    @DisplayName("SchoolServlet doDelete with invalid path")
    void doDeleteWithInvalidPathTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);
        schoolServlet.doDelete(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "School ID required.");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("SchoolServlet doDelete with path ands slash")
    void doDeleteWithInvalidPathEndsSlashTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/");
        schoolServlet.doDelete(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "School ID required.");
        verify(response, never()).setStatus(anyInt());
    }

}