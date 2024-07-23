package ru.evlitvin.servlet.api.v1;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.evlitvin.dto.PupilDto;
import ru.evlitvin.service.PupilService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests for PupilServlet class")
class PupilServletTest {

    @Mock
    private PupilService pupilService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private PupilServlet pupilServlet;

    private final Gson gson = new Gson();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("PupilServlet init method test")
    void testInit() throws ServletException {
        ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
        pupilServlet.init(servletConfig);
        assertNotNull(pupilService);
    }

    @Test
    @DisplayName("PupilServlet doPost pupil")
    void testDoPost() throws IOException, ServletException, SQLException, ClassNotFoundException {
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);

        when(reader.readLine()).thenReturn("{\"firstName\": \"Vladimir\", \"lastName\": \"Markov\"}", (String) null);

        ArgumentCaptor<PupilDto> tagCaptor = ArgumentCaptor.forClass(PupilDto.class);

        pupilServlet.doPost(request, response);

        verify(pupilService).save(tagCaptor.capture());
        assertEquals("Vladimir", tagCaptor.getValue().getFirstName());
        assertEquals("Markov", tagCaptor.getValue().getLastName());

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    @DisplayName("PupilServlet doPost request with null pupil first name")
    void doPostNullFirstName() throws ServletException, IOException {
        String missingFirstNameJson = "{\"firstName\": \"null\",\"content\": Markov}";
        BufferedReader reader = new BufferedReader(new StringReader(missingFirstNameJson));
        when(request.getReader()).thenReturn(reader);
        pupilServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
    }

    @Test
    @DisplayName("PupilServlet doPost request with empty pupil first name")
    void doPostEmptyFirstName() throws ServletException, IOException {
        String missingFirstNameJson = "{\"firstName\": \"\",\"lastName\": Ivanov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingFirstNameJson));
        when(request.getReader()).thenReturn(reader);
        pupilServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
    }

    @Test
    @DisplayName("PupilServlet doPost request with null pupil last name")
    void doPostNullLastName() throws ServletException, IOException {
        String missingLastNameJson = "{\"firstName\": Ivan\",\"content\": null}";
        BufferedReader reader = new BufferedReader(new StringReader(missingLastNameJson));
        when(request.getReader()).thenReturn(reader);
        pupilServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
    }

    @Test
    @DisplayName("PupilServlet doPost request with empty teacher last name")
    void doPostEmptyLastName() throws ServletException, IOException {
        String missingLastNameJson = "{\"firstName\": Ivan\", \"lastName\": \"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingLastNameJson));
        when(request.getReader()).thenReturn(reader);
        pupilServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
    }

    @Test
    @DisplayName("PupilServlet doPost request throws SQLException test")
    void doPostThrowsSQLException() throws IOException, SQLException, ClassNotFoundException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String json = "{\"id\":1,\"firstName\":\"Vladimir\",\"lastName\":\"Ivanov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(json));

        when(request.getReader()).thenReturn(reader);

        doThrow(SQLException.class).when(pupilService).save(any(PupilDto.class));

        assertThrows(ServletException.class, () -> pupilServlet.doPost(request, response));
    }

    @Test
    @DisplayName("PupilServlet doGet pupil by ID")
    public void doGetPupilTest() throws IOException, ServletException, SQLException, ClassNotFoundException {
        PupilDto pupilDto = new PupilDto(1L, "Vladimir", "Markov");
        when(pupilService.getById(1L)).thenReturn(pupilDto);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("/1");

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        pupilServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(writer).write(gson.toJson(pupilDto));
    }

    @Test
    @DisplayName("PupilServlet doGet all pupils")
    void doGetPupilsTest() throws ServletException, IOException, SQLException, ClassNotFoundException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getPathInfo()).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        PupilDto pupilDtoOne = new PupilDto(1L, "Vladimir", "Markov");
        PupilDto pupilDtoTwo = new PupilDto(2L, "Masha", "Sidorova");

        List<PupilDto> pupilDtos = Arrays.asList(pupilDtoOne, pupilDtoTwo);

        when(pupilService.getAll()).thenReturn(pupilDtos);

        pupilServlet.doGet(request, response);

        verify(pupilService, times(1)).getAll();

        String jsonOutput = gson.toJson(pupilDtos);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals(jsonOutput, stringWriter.toString().trim());
    }

    @Test
    @DisplayName("PupilServlet doPut pupil")
    void doPutTest() throws IOException, ServletException, SQLException, ClassNotFoundException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);

        when(request.getPathInfo()).thenReturn("/1");
        when(reader.readLine()).thenReturn("{\"firstName\": \"Vladimir\", \"lastName\": \"Markov\"}", (String) null);

        ArgumentCaptor<PupilDto> postCaptor = ArgumentCaptor.forClass(PupilDto.class);

        pupilServlet.doPut(request, response);

        verify(pupilService).update(postCaptor.capture());
        assertEquals(1L, postCaptor.getValue().getId());
        assertEquals("Vladimir", postCaptor.getValue().getFirstName());
        assertEquals("Markov", postCaptor.getValue().getLastName());

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    @DisplayName("PupilServlet doPut pupil with null path info test")
    void doPutWithNullPathTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);

        pupilServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil ID required.");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("PupilServlet doPut pupil with slash path info test")
    void doPutWithSlashPathTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/");

        pupilServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil ID required.");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("PupilServlet doPut pupil without Teacher ID")
    void doPutWithoutTeacherId() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);

        pupilServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil ID required.");
    }

    @Test
    @DisplayName("PupilServlet doPut request with invalid pupil ID")
    void testDoPutInvalidPostIdFormat() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/AAA");

        pupilServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid pupil ID.");
    }

    @Test
    @DisplayName("PupilServlet doPut request with null pupil first name test")
    void doPutWithoutTeacherFirstName() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"firstName\": null,\"lastName\": Markov}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        pupilServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
    }

    @Test
    @DisplayName("PupilServlet doPut request with empty pupil first name test")
    void doPutWithEmptyTeacherFirstName() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"firstName\": \"\",\"lastName\": \"Markov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        pupilServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
    }

    @Test
    @DisplayName("PupilServlet doPut request with null pupil last name test")
    void doPutWithoutTeacherLastName() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"firstName\": \"Vladimir\",\"lastName\": null}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        pupilServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
    }

    @Test
    @DisplayName("PupilServlet doPut request with empty pupil last name test")
    void doPutWithEmptyTeacherLastName() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"firstName\": \"Vladimir\",\"lastName\": \"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        pupilServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
    }

    @Test
    @DisplayName("PupilServlet doPut request return status CREATED test")
    void doPutSuccessStatus() throws ServletException, IOException, SQLException, ClassNotFoundException {
        when(request.getPathInfo()).thenReturn("/1");
        String validPostJson = "{\"firstName\": \"Vladimir\", \"lastName\": \"Markov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(validPostJson));
        when(request.getReader()).thenReturn(reader);

        pupilServlet.doPut(request, response);

        verify(pupilService).update(argThat(pupil -> pupil.getId().equals(1L)
                && pupil.getFirstName().equals("Vladimir")
                && pupil.getLastName().equals("Markov")));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    @DisplayName("PupilServlet doPut throws SQLException test")
    void doPutSQLException() throws IOException, SQLException, ClassNotFoundException {
        when(request.getPathInfo()).thenReturn("/1");
        String json = "{\"firstName\": \"Vladimir\", \"lastName\": \"Markov\"}";
        BufferedReader reader = new BufferedReader(new StringReader(json));
        when(request.getReader()).thenReturn(reader);
        doThrow(new SQLException()).when(pupilService).update(any(PupilDto.class));

        assertThrows(ServletException.class, () -> pupilServlet.doPut(request, response));
    }

    @Test
    @DisplayName("PupilServlet doDelete pupil by ID test")
    void doDeleteTest() throws IOException, ServletException, SQLException, ClassNotFoundException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("/1");

        pupilServlet.doDelete(request, response);

        verify(pupilService).delete(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("PupilServlet doDelete pupil without path info test")
    void doDeleteWithoutPath() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);

        pupilServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil ID required.");

        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("PupilServlet doDelete with path ends slash test")
    void doDeleteWithSlashPathInfo() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/");

        pupilServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil ID required.");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("PupilServlet doDelete without pupil ID test")
    void doDeleteWithoutId() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn(null);

        pupilServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil ID required.");
    }

    @Test
    @DisplayName("PupilServlet doDelete with invalid pupil ID test")
    void doDeleteInvalidTeacherIdTest() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/AAA");

        pupilServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Teacher and/or Pupil ID.");
    }

    @Test
    @DisplayName("PupilServlet doDelete pupil return NO_CONTENT status test")
    void doDeleteSuccess() throws IOException, SQLException, ClassNotFoundException, ServletException {
        when(request.getPathInfo()).thenReturn("/1");

        pupilServlet.doDelete(request, response);

        verify(pupilService).delete(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("PupilServlet doDelete pupil throws exception")
    void doDeleteSQLException() throws SQLException, ClassNotFoundException {
        when(request.getPathInfo()).thenReturn("/1");
        doThrow(new SQLException()).when(pupilService).delete(1L);

        assertThrows(ServletException.class, () -> pupilServlet.doDelete(request, response));
    }

    @Test
    @DisplayName("PupilServlet doGet pupils by teacher ID")
    void doGetFindPupilsByTeacherId() throws ServletException, IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/1/teachers");

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        PupilDto pupilDtoOne = new PupilDto();
        pupilDtoOne.setId(1L);
        pupilDtoOne.setFirstName("Vladimir");
        pupilDtoOne.setLastName("Markov");

        PupilDto pupilDtoTwo = new PupilDto();
        pupilDtoTwo.setId(2L);
        pupilDtoTwo.setFirstName("Masha");
        pupilDtoTwo.setLastName("Sidorova");

        List<PupilDto> pupilDtos = Arrays.asList(pupilDtoOne, pupilDtoTwo);
        when(pupilService.getAllPupilsByTeacher(1L)).thenReturn(pupilDtos);

        pupilServlet.doGet(request, response);

        verify(pupilService, times(1)).getAllPupilsByTeacher(1L);
        verify(writer, times(1)).write(gson.toJson(pupilDtos));
    }

    @Test
    void doGetNumberFormatException() throws ServletException, IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/AAA/teachers");

        pupilServlet.doGet(request, response);

        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid pupil ID.");
        verify(pupilService, never()).getAllPupilsByTeacher(anyLong());
    }

    private void mockRequestReader(String json) throws IOException {
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);
        when(reader.readLine()).thenReturn(json, (String) null);
    }

    @Test
    void addPupilToTeacherWithTeacherIdNull() throws IOException, ServletException, SQLException {
        String jsonOutput = "{\"pupilId\": 1}";
        mockRequestReader(jsonOutput);
        when(request.getPathInfo()).thenReturn("/addPupilToTeacher");

        pupilServlet.doPost(request, response);

        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID and/or Pupil ID required.");
        verify(pupilService, never()).addPupilToTeacher(anyLong(), anyLong());
    }

    @Test
    void addPupilToTeacherWithPupilIdNull() throws IOException, ServletException, SQLException {
        String jsonOutput = "{\"teacherId\": 1}";
        mockRequestReader(jsonOutput);
        when(request.getPathInfo()).thenReturn("/addPupilToTeacher");

        pupilServlet.doPost(request, response);

        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID and/or Pupil ID required.");
        verify(pupilService, never()).addPupilToTeacher(anyLong(), anyLong());
    }

    @Test
    void addPupilToTeacherWithIds() throws IOException, ServletException, SQLException {
        String jsonOutput = "{\"pupilId\": 1, \"teacherId\": 1}";
        mockRequestReader(jsonOutput);
        when(request.getPathInfo()).thenReturn("/addPupilToTeacher");

        pupilServlet.doPost(request, response);

        verify(pupilService, times(1)).addPupilToTeacher(1L, 1L);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void mockRequestPathInfo(String pathInfo) {
        when(request.getPathInfo()).thenReturn(pathInfo);
    }

    @Test
    void doDeleteRemovePupilFromTeacherList() throws IOException, ServletException, SQLException {
        String pathInfo = "/1/teachers/2";
        mockRequestPathInfo(pathInfo);

        doNothing().when(pupilService).removePupilFromTeacher(anyLong(), anyLong());

        pupilServlet.doDelete(request, response);

        verify(pupilService, times(1)).removePupilFromTeacher(1L, 2L);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doDeleteWithNullPath() throws IOException, ServletException {
        mockRequestPathInfo(null);

        pupilServlet.doDelete(request, response);

        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil ID required.");
    }

    private void mockRequestBody(String body) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(body));
        when(request.getReader()).thenReturn(reader);
    }

}