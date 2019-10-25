import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/saveBlob"})
public class SaveBlob extends HttpServlet {

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
            magitManager.setCurrentUser(user);
            String path = request.getParameter("blobPath");
            String content = request.getParameter("blobContent");
            magitManager.saveFile(path, content);

            String json = gson.toJson(path);
            out.println(json);
            out.flush();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
        }

}
