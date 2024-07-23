package ru.evlitvin.servlet.api.v1;

import com.google.gson.Gson;
import ru.evlitvin.dao.TeacherDao;
import ru.evlitvin.dto.TeacherDto;
import ru.evlitvin.service.TeacherService;
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

@WebServlet(name = "TeacherServlet", urlPatterns = {"/api/v1/teacher/*"})
public class TeacherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TeacherService teacherService;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        this.teacherService = new TeacherService(new TeacherDao(DatabaseConnection.getDataSource()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            if (pathInfo == null || pathInfo.equals("/")) {
                List<TeacherDto> teacherDtos = teacherService.getAll();
                String jsonOutput = gson.toJson(teacherDtos);
                resp.getWriter().write(jsonOutput);
            } else {
                Long teacherId = Long.parseLong(pathInfo.split("/")[1]);
                TeacherDto teacherDto = teacherService.getById(teacherId);
                String jsonOutput = gson.toJson(teacherDto);
                resp.getWriter().write(jsonOutput);
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid teacher ID.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TeacherDto teacherDto = deserializeDTO(req);
        try {
            if (teacherDto.getFirstName() == null || teacherDto.getFirstName().isEmpty() || teacherDto.getLastName() == null || teacherDto.getLastName().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
                return;
            }
            teacherService.save(teacherDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException | ClassNotFoundException e) {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID required.");
            return;
        }
        try {
            Long teacherId = Long.parseLong(pathInfo.split("/")[1]);
            TeacherDto teacherDto = deserializeDTO(req);
            teacherDto.setId(teacherId);
            if (teacherDto.getFirstName() == null || teacherDto.getFirstName().isEmpty() || teacherDto.getLastName() == null || teacherDto.getLastName().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher first name/ last name required.");
                return;
            }
            teacherService.update(teacherDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException | ClassNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid teacher ID.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID required.");
            } else {
                Long teacherId = Long.parseLong(pathInfo.split("/")[1]);
                teacherId.hashCode(teacherId);
                teacherService.delete(teacherId);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (NumberFormatException | ClassNotFoundException | SQLException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid teacher ID.");
        }
    }


    private TeacherDto deserializeDTO(HttpServletRequest req) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = req.getReader();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return gson.fromJson(stringBuilder.toString(), TeacherDto.class);
    }
}
