package Models;

public class User {

    private static String name;

    public User() {
        name = "Administrator";
    }


    public static String getName() {
        return name;
    }

    public void setName(String name) {
        User.name = name;
    }
}