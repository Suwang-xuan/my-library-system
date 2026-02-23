import cn.hutool.crypto.digest.BCrypt;

public class GenerateBCrypt {
    public static void main(String[] args) {
        String password = "123456";
        String hash = BCrypt.hashpw(password);
        System.out.println("BCrypt hash for '123456': " + hash);
    }
}