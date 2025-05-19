package Services; // Or your correct package for SessionManager

import model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void clearSession() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return this.currentUser != null;
    }

    public String getCurrentUserRole() {
        return isLoggedIn() ? this.currentUser.getRole() : null;
    }

    // MODIFIED
    public String getCurrentUsername() {
        return isLoggedIn() ? this.currentUser.getUsername() : null;
    }

    public Integer getCurrentUserId() {
        return isLoggedIn() ? this.currentUser.getId() : null;
    }
}