import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class MagitContextListener implements ServletContextListener {
    //private String rootRepo = "c:\\magit-ex3";
    private String rootRepo = "/opt/magit-ex3";
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Path index = Paths.get(rootRepo);
        if(Files.isDirectory(index)){
            try {
                Files.walk(index)
                        .sorted(Comparator.reverseOrder())  // as the file tree is traversed depth-first and that deleted dirs have to be empty
                        .forEach(t -> {
                            try {
                                Files.delete(t);
                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.createDirectory(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Path index = Paths.get(rootRepo);
        if(Files.isDirectory(index)){
            try {
                Files.walk(index)
                        .sorted(Comparator.reverseOrder())  // as the file tree is traversed depth-first and that deleted dirs have to be empty
                        .forEach(t -> {
                            try {
                                Files.delete(t);
                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Files.deleteIfExists(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
