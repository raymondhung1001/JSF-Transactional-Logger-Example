package com.example.todo.bean;

import com.example.todo.model.User;
import com.example.todo.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;

@Named
@SessionScoped
public class LoginBean implements Serializable {
    private static final Logger logger = LogManager.getLogger(LoginBean.class);
    private static final long serialVersionUID = 1L;

    @Inject
    private UserService userService;

    private String username;
    private String password;
    private User currentUser;

    public String login() {
        logger.info("Attempting login for user: {}", username);
        
        Subject currentUser = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        
        try {
            // Perform authentication using Shiro
            currentUser.login(token);
            logger.info("Login successful for user: {}", username);
            
            // Load user details and store in bean
            this.currentUser = userService.findByUsername(username);
            this.username = username;
            
            // Also store in session for backward compatibility
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute("currentUser", this.currentUser);
                session.setAttribute("username", username);
            }
            
            return "/todo/list.xhtml?faces-redirect=true";
        } catch (UnknownAccountException e) {
            logger.warn("Login failed - unknown account: {}", username);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", 
                            "Unknown account"));
        } catch (IncorrectCredentialsException e) {
            logger.warn("Login failed - incorrect credentials for user: {}", username);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", 
                            "Invalid username or password"));
        } catch (LockedAccountException e) {
            logger.warn("Login failed - locked account: {}", username);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", 
                            "Account is locked"));
        } catch (AuthenticationException e) {
            logger.warn("Login failed for user: {}", username, e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", 
                            "Authentication failed: " + e.getMessage()));
        }
        
        return null;
    }

    public String logout() {
        logger.info("Logging out user: {}", username);
        
        Subject currentSubject = SecurityUtils.getSubject();
        if (currentSubject != null && currentSubject.isAuthenticated()) {
            // Logout using Shiro
            currentSubject.logout();
            logger.info("Shiro logout successful");
        }
        
        // Clear bean state
        currentUser = null;
        username = null;
        password = null;
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            // Invalidate session
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            // Use redirect for reliable navigation after session invalidation
            try {
                String contextPath = facesContext.getExternalContext().getRequestContextPath();
                facesContext.getExternalContext().redirect(contextPath + "/login.xhtml");
                facesContext.responseComplete();
            } catch (IOException e) {
                logger.error("Error redirecting after logout", e);
                // Fallback to navigation outcome
                return "/login.xhtml?faces-redirect=true";
            }
        }
        
        logger.info("User logged out successfully");
        return null;
    }

    public boolean isLoggedIn() {
        Subject currentSubject = SecurityUtils.getSubject();
        boolean authenticated = currentSubject != null && currentSubject.isAuthenticated();
        
        if (authenticated && currentUser == null) {
            // Load user if authenticated but not loaded in bean
            String currentUsername = (String) currentSubject.getPrincipal();
            if (currentUsername != null) {
                currentUser = userService.findByUsername(currentUsername);
                username = currentUsername;
            }
        }
        
        return authenticated;
    }

    /**
     * Returns the navigation outcome for the initial page based on login status.
     * Used by f:viewAction in index.xhtml
     */
    public String getInitialPage() {
        if (isLoggedIn()) {
            return "/todo/list.xhtml?faces-redirect=true";
        } else {
            return "/login.xhtml?faces-redirect=true";
        }
    }

    /**
     * Keep session alive - called by AJAX to prevent timeout
     */
    public void keepSessionAlive() {
        Subject currentSubject = SecurityUtils.getSubject();
        if (currentSubject != null && currentSubject.isAuthenticated()) {
            // Touch the session by accessing the subject
            currentSubject.getPrincipal();
            
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
                if (session != null) {
                    session.setAttribute("lastActivity", System.currentTimeMillis());
                }
            }
            logger.debug("Session kept alive for user: {}", username);
        }
    }

    /**
     * Logout via AJAX call - used for back button detection
     * This method invalidates the session and can be called from JavaScript
     */
    public void logoutAjax() {
        logger.info("Logging out user via AJAX (back button detected): {}", username);
        
        Subject currentSubject = SecurityUtils.getSubject();
        if (currentSubject != null && currentSubject.isAuthenticated()) {
            // Logout using Shiro
            currentSubject.logout();
        }
        
        // Clear bean state
        currentUser = null;
        username = null;
        password = null;
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
        
        logger.info("User logged out successfully via AJAX");
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}

