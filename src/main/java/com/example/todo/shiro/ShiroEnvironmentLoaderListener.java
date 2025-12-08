package com.example.todo.shiro;

import com.example.todo.realm.JpaRealm;
import com.example.todo.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.lang.reflect.Field;

public class ShiroEnvironmentLoaderListener extends EnvironmentLoaderListener {
    private static final Logger logger = LogManager.getLogger(ShiroEnvironmentLoaderListener.class);

    @Override
    protected WebEnvironment createEnvironment(ServletContext servletContext) {
        logger.info("Creating Shiro web environment");
        
        // Use default IniWebEnvironment which will automatically load shiro.ini
        // from WEB-INF/shiro.ini (configured via shiroConfigLocations in web.xml)
        IniWebEnvironment env = new IniWebEnvironment();
        env.setServletContext(servletContext);
        // Configuration location is set via shiroConfigLocations context param in web.xml
        env.init();
        
        logger.info("Shiro web environment created");
        return env;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing Shiro environment with CDI integration");
        super.contextInitialized(sce);
        
        // Try to inject CDI realm after context is fully initialized
        ServletContext servletContext = sce.getServletContext();
        WebEnvironment env = (WebEnvironment) servletContext.getAttribute(ENVIRONMENT_ATTRIBUTE_KEY);
        
        if (env != null) {
            SecurityManager securityManager = env.getSecurityManager();
            
            if (securityManager instanceof DefaultWebSecurityManager) {
                DefaultWebSecurityManager webSecurityManager = (DefaultWebSecurityManager) securityManager;
                
                // Check if realm is already set
                if (webSecurityManager.getRealms() == null || webSecurityManager.getRealms().isEmpty()) {
                    logger.info("No realms configured, attempting to inject JpaRealm from CDI");
                    
                    // Get the realm from CDI now that context is initialized
                    // Try multiple times with delays in case CDI is not immediately available
                    JpaRealm realm = null;
                    int maxAttempts = 5;
                    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                        try {
                            realm = CDI.current().select(JpaRealm.class).get();
                            logger.info("Successfully retrieved JpaRealm from CDI on attempt {}", attempt);
                            break;
                        } catch (Exception e) {
                            logger.warn("Attempt {} to get JpaRealm from CDI failed: {}", attempt, e.getMessage());
                            if (attempt < maxAttempts) {
                                try {
                                    Thread.sleep(500); // Wait 500ms before retry
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (realm != null) {
                        webSecurityManager.setRealm(realm);
                        logger.info("Successfully injected JpaRealm from CDI after context initialization");
                    } else {
                        // Try to create realm manually with UserService from JNDI as fallback
                        logger.warn("CDI injection failed, attempting to create JpaRealm manually with JNDI lookup");
                        try {
                            UserService userService = lookupUserService();
                            if (userService != null) {
                                realm = new JpaRealm();
                                // Inject UserService via reflection as fallback
                                injectUserService(realm, userService);
                                webSecurityManager.setRealm(realm);
                                logger.info("Successfully created JpaRealm with JNDI-looked-up UserService");
                            } else {
                                throw new IllegalStateException("Could not obtain UserService via JNDI");
                            }
                        } catch (Exception e) {
                            logger.error("Failed to inject JpaRealm from CDI after {} attempts and fallback also failed", maxAttempts, e);
                            throw new IllegalStateException("Failed to configure Shiro realm. JpaRealm could not be retrieved from CDI and fallback failed.", e);
                        }
                    }
                } else {
                    logger.info("Realm already configured: {}", webSecurityManager.getRealms());
                }
            } else {
                logger.warn("SecurityManager is not an instance of DefaultWebSecurityManager, cannot inject realm");
            }
        } else {
            logger.error("WebEnvironment not found in servlet context");
        }
    }
    
    /**
     * Lookup UserService via JNDI as fallback when CDI is not available
     */
    private UserService lookupUserService() {
        try {
            InitialContext ctx = new InitialContext();
            // Try common JNDI names for EJBs
            String[] jndiNames = {
                "java:module/UserService",
                "java:app/jsf-todo-app/UserService",
                "java:comp/env/ejb/UserService",
                "java:global/jsf-todo-app/UserService"
            };
            
            for (String jndiName : jndiNames) {
                try {
                    Object obj = ctx.lookup(jndiName);
                    if (obj instanceof UserService) {
                        logger.info("Found UserService via JNDI: {}", jndiName);
                        return (UserService) obj;
                    }
                } catch (NamingException e) {
                    logger.debug("JNDI lookup failed for {}: {}", jndiName, e.getMessage());
                }
            }
            
            logger.warn("Could not find UserService via any JNDI name");
            return null;
        } catch (Exception e) {
            logger.error("Error during JNDI lookup for UserService", e);
            return null;
        }
    }
    
    /**
     * Inject UserService into JpaRealm using reflection as fallback
     */
    private void injectUserService(JpaRealm realm, UserService userService) {
        try {
            Field userServiceField = JpaRealm.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(realm, userService);
            logger.info("Successfully injected UserService into JpaRealm via reflection");
        } catch (Exception e) {
            logger.error("Failed to inject UserService into JpaRealm via reflection", e);
            throw new IllegalStateException("Could not inject UserService into JpaRealm", e);
        }
    }
}

