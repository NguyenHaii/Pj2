package quanlynet.validation;

public class UsernameValidator {

    public static boolean isValidUsername(String username) {
        return username != null && !username.contains(" ");
    }
}
