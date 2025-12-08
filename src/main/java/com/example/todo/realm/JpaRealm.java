package com.example.todo.realm;

import com.example.todo.model.User;
import com.example.todo.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

@Named
@ApplicationScoped
public class JpaRealm extends AuthorizingRealm {
    private static final Logger logger = LogManager.getLogger(JpaRealm.class);

    @Inject
    private UserService userService;

    public JpaRealm() {
        setName("JpaRealm");
        // Use simple credentials matcher for plain text passwords
        // In production, you should use HashedCredentialsMatcher with proper hashing
        setCredentialsMatcher(new org.apache.shiro.authc.credential.SimpleCredentialsMatcher());
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        logger.info("Authenticating user: {}", username);

        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            logger.warn("No account found for user: {}", username);
            throw new UnknownAccountException("No account found for user [" + username + "]");
        }

        // Return authentication info with user's password
        // Note: In production, you should hash passwords and use HashedCredentialsMatcher
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(
                user.getUsername(),  // principal
                user.getPassword(),  // credentials
                getName()            // realm name
        );

        logger.info("Authentication info retrieved for user: {}", username);
        return info;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();
        logger.debug("Getting authorization info for user: {}", username);

        User user = userService.findByUsername(username);
        if (user == null) {
            logger.warn("No user found for authorization: {}", username);
            return null;
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        
        // Add roles (you can extend User model to include roles if needed)
        Set<String> roles = new HashSet<>();
        roles.add("user"); // Default role for all authenticated users
        info.setRoles(roles);

        // Add permissions if needed
        // Set<String> permissions = new HashSet<>();
        // info.setStringPermissions(permissions);

        logger.debug("Authorization info set for user: {} with roles: {}", username, roles);
        return info;
    }
}

