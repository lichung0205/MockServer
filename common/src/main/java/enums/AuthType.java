package enums;

public enum AuthType {
    STUDENT("學員"),
    TEACHER("導師"),
    ADMIN("管理員");

    private final String displayName;

    AuthType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
