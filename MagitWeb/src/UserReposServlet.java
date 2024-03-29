import Models.Repository;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@WebServlet(urlPatterns = {"/getRepos"})
public class UserReposServlet extends HttpServlet {



    private MagitManager magitManager = new MagitManager();
    private static final String USER_NAME_PARAMETER = "UserName";


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            MagitManager magitManager = ServletUtils.getMagitManager(getServletContext());
            String user = request.getParameter("user");
            Set<Repository> userRepoList;
            if(user.equals("currentUser")) {
                user = (String) request.getSession().getAttribute("UserName");
            }
            userRepoList = magitManager.getUserRepos(user);
            String json = gson.toJson(userRepoList);
            out.println(json);
            out.flush();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
        }

}
