import Models.Blob;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/getWorkingDirectory"})
public class GetWorkingDir extends HttpServlet {

    private MagitManager magitManager = new MagitManager();
    private static final String USER_NAME_PARAMETER = "UserName";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            String user = (String) request.getSession().getAttribute("UserName");
            String repo = (String) request.getSession().getAttribute("repoName");
            Gson gson = new Gson();
            MagitManager magitManager = ServletUtils.getMagitManager(getServletContext());
            magitManager.setRepo(repo, user);

            boolean isInit = Boolean.parseBoolean(request.getParameter("init"));
            if (isInit) {

                Map<String, Blob> workingChanges = magitManager.initWcFilesMap();

                String json = gson.toJson(workingChanges.entrySet().toArray());
                out.println(json);
                out.flush();
            } else {

                Map<String, List<Blob>> workingChanges = magitManager.getWcFilesMap();

                String json = gson.toJson(workingChanges.entrySet().toArray());
                out.println(json);
                out.flush();
            }

        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

}
