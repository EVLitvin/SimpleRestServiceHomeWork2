package ru.evlitvin.servlet.api.v1;

import com.google.gson.Gson;
import ru.evlitvin.dao.PupilDAO;
import ru.evlitvin.dto.PupilDto;
import ru.evlitvin.service.PupilService;
import ru.evlitvin.util.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "PupilServlet", urlPatterns = "/api/v1/pupil/*")
public class PupilServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private PupilService pupilService;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        this.pupilService = new PupilService(new PupilDAO(DatabaseConnection.getDataSource()));
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            if (pathInfo == null || pathInfo.equals("/")) {
                List<PupilDto> pupilDtos = pupilService.getAll();
                String jsonOutput = gson.toJson(pupilDtos);
                resp.getWriter().write(jsonOutput);
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    Long id = Long.parseLong(pathParts[1]);
                    PupilDto pupilDto = pupilService.getById(id);
                    String jsonOutput = gson.toJson(pupilDto);
                    resp.getWriter().write(jsonOutput);
                } else if (pathParts.length == 3 && pathParts[2].equals("teachers")) {
                    Long teacherId = Long.parseLong(pathParts[1]);
                    List<PupilDto> pupilDtos = pupilService.getAllPupilsByTeacher(teacherId);
                    String jsonOutput = gson.toJson(pupilDtos);
                    resp.getWriter().write(jsonOutput);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException | ClassNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid pupil ID.");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            PupilDto pupilDto = deserializePupilDTO(req);
            try {
                if (pupilDto.getFirstName() == null || pupilDto.getFirstName().isEmpty() || pupilDto.getLastName() == null || pupilDto.getLastName().isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
                    return;
                }
                pupilService.save(pupilDto);
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } catch (SQLException | ClassNotFoundException e) {
                throw new ServletException(e);
            }
        } else if (pathInfo.equals("/addPupilToTeacher")) {
            AddPupilToTeacherRequest addPupilToTeacherRequest = deserializeAddPupilToTeacherRequest(req);
            try {
                if (addPupilToTeacherRequest.getTeacherId() == null || addPupilToTeacherRequest.getPupilId() == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID and/or Pupil ID required.");
                    return;
                }
                pupilService.addPupilToTeacher(addPupilToTeacherRequest.getPupilId(), addPupilToTeacherRequest.getTeacherId());
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (SQLException e) {
                throw new ServletException(e);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Teacher and/or Pupil ID.");
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil ID required.");
            return;
        }
        try {
            Long pupilId = Long.parseLong(pathInfo.split("/")[1]);
            PupilDto pupilDto = deserializePupilDTO(req);
            pupilDto.setId(pupilId);
            if (pupilDto.getFirstName() == null || pupilDto.getFirstName().isEmpty() || pupilDto.getLastName() == null || pupilDto.getLastName().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil first name/last name required.");
                return;
            }
            pupilService.update(pupilDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException | ClassNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid pupil ID.");
        }
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Pupil ID required.");
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    Long pupilId = Long.parseLong(pathParts[1]);
                    pupilService.delete(pupilId);
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else if (pathParts.length == 4 && pathParts[2].equals("teachers")) {
                    Long teacherId = Long.parseLong(pathParts[3]);
                    Long pupilId = Long.parseLong(pathParts[1]);
                    pupilService.removePupilFromTeacher(pupilId, teacherId);
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException | ClassNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Teacher and/or Pupil ID.");
        }
    }

    private PupilDto deserializePupilDTO(HttpServletRequest req) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = req.getReader();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return gson.fromJson(stringBuilder.toString(), PupilDto.class);
    }

    private AddPupilToTeacherRequest deserializeAddPupilToTeacherRequest(HttpServletRequest req) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = req.getReader();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return gson.fromJson(stringBuilder.toString(), AddPupilToTeacherRequest.class);
    }

    static class AddPupilToTeacherRequest {
        private Long teacherId;
        private Long pupilId;

        public Long getTeacherId() {
            return teacherId;
        }

        public Long getPupilId() {
            return pupilId;
        }
    }

}
