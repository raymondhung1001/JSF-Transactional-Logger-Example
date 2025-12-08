package com.example.todo.service;

import com.example.todo.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @PersistenceContext(unitName = "todoPU")
    private EntityManager em;

    public User createUser(String username, String password, String email, String fullName) {
        logger.info("Creating new user: {}", username);
        User user = new User(username, password, email, fullName);
        em.persist(user);
        logger.info("User created successfully: {}", username);
        return user;
    }

    public User findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        TypedQuery<User> query = em.createNamedQuery("User.findByUsername", User.class);
        query.setParameter("username", username);
        List<User> users = query.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    public User findById(Long id) {
        logger.debug("Finding user by id: {}", id);
        return em.find(User.class, id);
    }

    public List<User> findAll() {
        logger.debug("Finding all users");
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
        return query.getResultList();
    }

    public User updateUser(User user) {
        logger.info("Updating user: {}", user.getUsername());
        return em.merge(user);
    }

    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
            logger.info("User deleted successfully: {}", id);
        }
    }
}

