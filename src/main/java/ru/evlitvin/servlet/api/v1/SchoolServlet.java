package ru.evlitvin.servlet.api.v1;

import com.google.gson.Gson;

import ru.evlitvin.dao.SchoolDao;
import ru.evlitvin.dto.SchoolDto;
import ru.evlitvin.service.SchoolService;
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

@WebServlet(name = "SchoolServlet", urlPatterns = {"/api/v1/school/*"})
public class SchoolServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private SchoolService schoolService;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        this.schoolService = new SchoolService(new SchoolDao(DatabaseConnection.getDataSource()));
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            if (pathInfo == null || pathInfo.equals("/")) {
                List<SchoolDto> schoolDtos = schoolService.getAll();
                String jsonOutput = gson.toJson(schoolDtos);
                resp.getWriter().write(jsonOutput);
            } else {
                Long schoolId = Long.parseLong(pathInfo.split("/")[1]);
                SchoolDto schoolDto = schoolService.getById(schoolId);
                String jsonOutput = gson.toJson(schoolDto);
                resp.getWriter().write(jsonOutput);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid school ID.");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SchoolDto schoolDto = deserializeSchoolDto(req);
        try {
            if (schoolDto.getName() == null || schoolDto.getName().isEmpty() || schoolDto.getAddress() == null || schoolDto.getAddress().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
                return;
            }
            schoolService.save(schoolDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "School ID required.");
            return;
        }
        try {
            Long id = Long.parseLong(pathInfo.split("/")[1]);
            SchoolDto schoolDto = deserializeSchoolDto(req);
            schoolDto.setId(id);
            if (schoolDto.getName() == null || schoolDto.getName().isEmpty() || schoolDto.getAddress() == null || schoolDto.getAddress().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and/or address required.");
                return;
            }
            schoolService.update(schoolDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid school ID.");
        }
    }


    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "School ID required.");
            } else {
                Long schoolId = Long.parseLong(pathInfo.substring(1));
                schoolService.delete(schoolId);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid school ID.");
        }
    }

    private SchoolDto deserializeSchoolDto(HttpServletRequest req) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = req.getReader();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return gson.fromJson(stringBuilder.toString(), SchoolDto.class);
    }

}
