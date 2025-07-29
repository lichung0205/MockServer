package common;

import com.google.gson.Gson;

/**
 * 共用通訊協定，登入時發給server使用
 */
public class LoginInfo {

    private static final Gson gson = new Gson();

    private AuthType authType; // 身分類型
    private String id; // ID
    private String name; // 姓名

    public LoginInfo() {
    }

    public LoginInfo(AuthType authType, String id, String name) {
        this.authType = authType;
        this.id = id;
        this.name = name;
    }

    public AuthType getAuthType() {
        return this.authType;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public static LoginInfo fromJson(String json) {
        return gson.fromJson(json, LoginInfo.class);
    }

    @Override
    public String toString() {
        return String.format("LoginInfo{type=%s, target=%s, content=%s}", authType, id, name);
    }
}
