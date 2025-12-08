package com.example.todo.service;

import com.example.todo.model.Todo;
import com.example.todo.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class TodoService {
    private static final Logger logger = LogManager.getLogger(TodoService.class);

    @PersistenceContext(unitName = "todoPU")
    private EntityManager em;

    public Todo createTodo(String title, String description, User user) {
        logger.info("Creating new todo for user: {}", user.getUsername());
        Todo todo = new Todo(title, description, user);
        em.persist(todo);
        logger.info("Todo created successfully with id: {}", todo.getId());
        return todo;
    }

    public List<Todo> findByUser(Long userId) {
        logger.debug("Finding todos for user id: {}", userId);
        TypedQuery<Todo> query = em.createNamedQuery("Todo.findByUser", Todo.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public Todo findById(Long id) {
        logger.debug("Finding todo by id: {}", id);
        return em.find(Todo.class, id);
    }

    public Todo updateTodo(Todo todo) {
        logger.info("Updating todo with id: {}", todo.getId());
        return em.merge(todo);
    }

    public void deleteTodo(Long id) {
        logger.info("Deleting todo with id: {}", id);
        Todo todo = em.find(Todo.class, id);
        if (todo != null) {
            em.remove(todo);
            logger.info("Todo deleted successfully: {}", id);
        }
    }

    public void toggleComplete(Long id) {
        logger.info("Toggling completion status for todo id: {}", id);
        Todo todo = em.find(Todo.class, id);
        if (todo != null) {
            todo.setCompleted(!todo.getCompleted());
            em.merge(todo);
            logger.info("Todo completion status updated: {}", id);
        }
    }
}

