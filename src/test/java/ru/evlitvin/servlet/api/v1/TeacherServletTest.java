package ru.evlitvin.servlet.api.v1;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.evlitvin.dto.TeacherDto;
import ru.evlitvin.service.TeacherService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@DisplayName("Tests for TeacherServlet class")
class TeacherServletTest {

    @Mock
    private TeacherService teacherService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private TeacherServlet teacherServlet;

    private final Gson gson = new Gson();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("TeacherServlet init method test")
    void initTest() throws ServletException {
        ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
        teacherServlet.init(servletConfig);
        assertNotNull(teacherService);
    }

    @Test
    @DisplayName("TeacherServlet doPost teacher")
    void testDoPost() throws IOException, ServletException, SQLException, ClassNotFoundException {
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);

        when(reader.readLine()).thenReturn("{\"firstName\": \"Ivan\", \"lastName\": \"Ivanov\"}", (String) null);

        ArgumentCaptor<TeacherDto> postCaptorTeacherDto = ArgumentCaptor.forClass(TeacherDto.class);

        teacherServlet.doPost(request, response);

        verify(teacherService).save(postCaptorTeacherDto.capture());
        assertEquals("Ivan", postCaptorTeacherDto.getValue().getFirstName());
        assertEquals("Ivanov", postCaptorTeacherDto.getValue().getLastName());

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    @DisplayName("TeacherServlet doPost request with null teacher first name")
    void doPostNullFirstName() throws ServletException, IOException {
        String missingFirstNameJson = "{\"firstName\": \"null\",\"content\": Ivanov}";
        BufferedReader reader = new BufferedReader(new StringReader(missingFirstNameJson));
        when(request.getReader()).thenReturn(reader);
        teacherServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
    }

    @Test
    @DisplayName("TeacherServlet doPost request with empty teacher first name")
    void doPostEmptyFirstName() throws ServletException, IOException {
        String missingFirstNameJson = "{\"firstName\": \"\",\"lastName\": Ivanov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingFirstNameJson));
        when(request.getReader()).thenReturn(reader);
        teacherServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
    }

    @Test
    @DisplayName("TeacherServlet doPost request with null teacher last name")
    void doPostNullLastName() throws ServletException, IOException {
        String missingLastNameJson = "{\"firstName\": Ivan\",\"content\": null}";
        BufferedReader reader = new BufferedReader(new StringReader(missingLastNameJson));
        when(request.getReader()).thenReturn(reader);
        teacherServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
    }

    @Test
    @DisplayName("TeacherServlet doPost request with empty teacher last name")
    void doPostEmptyLastName() throws ServletException, IOException {
        String missingLastNameJson = "{\"firstName\": Ivan\", \"lastName\": \"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingLastNameJson));
        when(request.getReader()).thenReturn(reader);
        teacherServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
    }

    @Test
    @DisplayName("TeacherServlet doPost request throws SQLException test")
    void doPostThrowsSQLException() throws IOException, SQLException, ClassNotFoundException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String json = "{\"id\":1,\"firstName\":\"Ivan\",\"lastName\":\"Ivanov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(json));

        when(request.getReader()).thenReturn(reader);

        doThrow(SQLException.class).when(teacherService).save(any(TeacherDto.class));

        assertThrows(ServletException.class, () -> teacherServlet.doPost(request, response));
    }

    @Test
    @DisplayName("TeacherServlet doGet teacher by ID")
    void doGetTeacherTest() throws IOException, SQLException, ClassNotFoundException, ServletException {
        TeacherDto teacherDto = new TeacherDto();
        teacherDto.setId(1L);
        teacherDto.setFirstName("Ivan");
        teacherDto.setLastName("Ivanov");
        teacherDto.setSchoolId(1L);

        when(teacherService.getById(1L)).thenReturn(teacherDto);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("/1");

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        teacherServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(writer).write(gson.toJson(teacherDto));
    }

    @Test
    @DisplayName("TeacherServlet doGet all teachers")
    void doGetTeachersTest() throws ServletException, IOException, SQLException, ClassNotFoundException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getPathInfo()).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        TeacherDto teacherDtoOne = new TeacherDto(1L, "Ivan", "Ivanov", 1L);
        TeacherDto teacherDtoTwo = new TeacherDto(2L, "Petr", "Kuznecov", 1L);
        List<TeacherDto> teacherDtos = Arrays.asList(teacherDtoOne, teacherDtoTwo);

        when(teacherService.getAll()).thenReturn(teacherDtos);

        teacherServlet.doGet(request, response);

        verify(teacherService, times(1)).getAll();

        String jsonOutput = gson.toJson(teacherDtos);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals(jsonOutput, stringWriter.toString().trim());
    }

    @Test
    @DisplayName("TeacherServlet doPut teacher")
    void doPutTest() throws IOException, ServletException, SQLException, ClassNotFoundException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);

        when(request.getPathInfo()).thenReturn("/1");
        when(reader.readLine()).thenReturn("{\"firstName\": \"Ivan\", \"lastName\": \"Ivanov\"}", (String) null);

        ArgumentCaptor<TeacherDto> postCaptor = ArgumentCaptor.forClass(TeacherDto.class);

        teacherServlet.doPut(request, response);

        verify(teacherService).update(postCaptor.capture());
        assertEquals(1L, postCaptor.getValue().getId());
        assertEquals("Ivan", postCaptor.getValue().getFirstName());
        assertEquals("Ivanov", postCaptor.getValue().getLastName());

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    @DisplayName("TeacherServlet doPut teacher with null path info test")
    void doPutWithNullPathTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);

        teacherServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID required.");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("TeacherServlet doPut teacher with slash path info test")
    void doPutWithSlashPathTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/");

        teacherServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID required.");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("TeacherServlet doPut teacher without Teacher ID")
    void doPutWithoutTeacherId() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);

        teacherServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID required.");
    }

    @Test
    @DisplayName("TeacherServlet doPut request with invalid teacher ID")
    void testDoPutInvalidPostIdFormat() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/AAA");

        teacherServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid teacher ID.");
    }

    @Test
    @DisplayName("TeacherServlet doPut request with null teacher first name test")
    void doPutWithoutTeacherFirstName() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"firstName\": null,\"lastName\": Ivanov}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        teacherServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
    }

    @Test
    @DisplayName("TeacherServlet doPut request with empty teacher first name test")
    void doPutWithEmptyTeacherFirstName() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"firstName\": \"\",\"lastName\": \"Ivanov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        teacherServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
    }

    @Test
    @DisplayName("TeacherServlet doPut request with null teacher last name test")
    void doPutWithoutTeacherLastName() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"firstName\": \"Ivan\",\"lastName\": null}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        teacherServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
    }

    @Test
    @DisplayName("TeacherServlet doPut request with empty teacher last name test")
    void doPutWithEmptyTeacherLastName() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"firstName\": \"Ivan\",\"lastName\": \"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        teacherServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
    }

    @Test
    @DisplayName("TeacherServlet doPut request return status CREATED test")
    void doPutSuccessStatus() throws ServletException, IOException, SQLException, ClassNotFoundException {
        when(request.getPathInfo()).thenReturn("/1");
        String validPostJson = "{\"firstName\": \"Ivan\", \"lastName\": \"Ivanov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(validPostJson));
        when(request.getReader()).thenReturn(reader);

        teacherServlet.doPut(request, response);

        verify(teacherService).update(argThat(teacher -> teacher.getId().equals(1L)
                && teacher.getFirstName().equals("Ivan")
                && teacher.getLastName().equals("Ivanov")));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    @DisplayName("TeacherServlet doPut throws SQLException test")
    void doPutSQLException() throws IOException, SQLException, ClassNotFoundException {
        when(request.getPathInfo()).thenReturn("/1");
        String json = "{\"firstName\": \"Ivan\", \"lastName\": \"Ivanov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(json));
        when(request.getReader()).thenReturn(reader);
        doThrow(new SQLException()).when(teacherService).update(any(TeacherDto.class));

        assertThrows(ServletException.class, () -> teacherServlet.doPut(request, response));
    }

    @Test
    @DisplayName("TeacherServlet doDelete teacher by ID test")
    void doDeleteTest() throws IOException, SQLException, ClassNotFoundException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("/1");

        teacherServlet.doDelete(request, response);

        verify(teacherService).delete(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("TeacherServlet doDelete teacher without path info test")
    void doDeleteWithoutPath() throws IOException {
        when(request.getPathInfo()).thenReturn(null);

        teacherServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID required.");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("TeacherServlet doDelete with path ends slash test")
    void doDeleteWithSlashPathInfo() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        teacherServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID required.");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("TeacherServlet doDelete without teacher ID test")
    void doDeleteWithoutId() throws IOException {
        when(request.getPathInfo()).thenReturn(null);

        teacherServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID required.");
    }

    @Test
    @DisplayName("TeacherServlet doDelete with invalid teacher ID test")
    void doDeleteInvalidTeacherIdTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/AAA");

        teacherServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid teacher ID.");
    }

    @Test
    @DisplayName("TeacherServlet doDelete teacher return NO_CONTENT status test")
    void doDeleteSuccess() throws IOException, SQLException, ClassNotFoundException {
        when(request.getPathInfo()).thenReturn("/1");

        teacherServlet.doDelete(request, response);

        verify(teacherService).delete(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

}