import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/pull"})
public class DoPull extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            String user = (String) request.getSession().getAttribute("UserName");
            String repo = (String) request.getSession().getAttribute("repoName");
            MagitManager magitManager = ServletUtils.getMagitManager(getServletContext());
            magitManager.setRepo(repo, user);
            String msg = magitManager.pull();
            Gson gson = new Gson();
            String json = gson.toJson(msg);
            out.println(json);
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
        }

}
