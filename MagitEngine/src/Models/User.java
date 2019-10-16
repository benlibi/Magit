package Models;

public class User {

    private static String name;

    public User() {
        name = "Administrator";
    }

    public User(String name) {
        this.name = name;
    }
    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        User.name = name;
    }
}