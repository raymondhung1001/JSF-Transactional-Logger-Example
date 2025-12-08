package com.example.todo.bean;

import com.example.todo.model.Todo;
import com.example.todo.model.User;
import com.example.todo.service.TodoService;
import com.example.todo.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class TodoBean implements Serializable {
    private static final Logger logger = LogManager.getLogger(TodoBean.class);
    private static final long serialVersionUID = 1L;

    @Inject
    private TodoService todoService;

    @Inject
    private UserService userService;

    @Inject
    private LoginBean loginBean;

    private List<Todo> todos;
    private Todo selectedTodo;
    private String title;
    private String description;
    private Long currentUserId;

    @PostConstruct
    public void init() {
        loadTodos();
    }

    public void loadTodos() {
        if (loginBean.isLoggedIn()) {
            currentUserId = loginBean.getCurrentUser().getId();
            // Create a new list instance to ensure JSF detects the change
            List<Todo> newTodos = todoService.findByUser(currentUserId);
            todos = newTodos != null ? new java.util.ArrayList<>(newTodos) : new java.util.ArrayList<>();
            logger.info("Loaded {} todos for user id: {}", todos.size(), currentUserId);
            if (!todos.isEmpty()) {
                for (Todo todo : todos) {
                    logger.debug("Todo in list: id={}, title={}", todo.getId(), todo.getTitle());
                }
            }
        } else {
            todos = new java.util.ArrayList<>();
            logger.warn("Cannot load todos - user not logged in");
        }
    }

    public void addTodo() {
        if (loginBean.isLoggedIn()) {
            try {
                User user = loginBean.getCurrentUser();
                logger.info("Adding todo with title: '{}' for user: {}", title, user.getUsername());
                Todo todo = todoService.createTodo(title, description, user);
                logger.info("Todo added successfully: id={}, title={}", todo.getId(), todo.getTitle());
                
                // Force refresh the list by setting it to null first
                todos = null;
                // Then reload
                loadTodos();
                
                logger.info("After loadTodos(), list size is: {}", todos != null ? todos.size() : 0);
                
                title = "";
                description = "";
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Todo added successfully"));
            } catch (Exception e) {
                logger.error("Error adding todo", e);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to add todo"));
            }
        } else {
            logger.warn("Cannot add todo - user not logged in");
        }
    }

    public void updateTodo() {
        if (selectedTodo != null) {
            try {
                selectedTodo.setTitle(title);
                selectedTodo.setDescription(description);
                todoService.updateTodo(selectedTodo);
                logger.info("Todo updated: {}", selectedTodo.getId());
                loadTodos();
                selectedTodo = null;
                title = "";
                description = "";
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Todo updated successfully"));
            } catch (Exception e) {
                logger.error("Error updating todo", e);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update todo"));
            }
        }
    }

    public void deleteTodo(Long id) {
        try {
            todoService.deleteTodo(id);
            logger.info("Todo deleted: {}", id);
            loadTodos();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Todo deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting todo", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete todo"));
        }
    }

    public void toggleComplete(Long id) {
        try {
            todoService.toggleComplete(id);
            logger.info("Todo completion toggled: {}", id);
            loadTodos();
        } catch (Exception e) {
            logger.error("Error toggling todo completion", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update todo"));
        }
    }

    public void selectTodo(Todo todo) {
        this.selectedTodo = todo;
        this.title = todo.getTitle();
        this.description = todo.getDescription();
    }

    public void cancelEdit() {
        selectedTodo = null;
        title = "";
        description = "";
    }

    // Getters and Setters
    public List<Todo> getTodos() {
        // Ensure list is loaded if it's null or empty and user is logged in
        if (todos == null && loginBean != null && loginBean.isLoggedIn()) {
            logger.debug("Todos list is null, loading todos...");
            loadTodos();
        }
        logger.debug("getTodos() called, returning {} todos", todos != null ? todos.size() : 0);
        return todos;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
    }

    public Todo getSelectedTodo() {
        return selectedTodo;
    }

    public void setSelectedTodo(Todo selectedTodo) {
        this.selectedTodo = selectedTodo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }
}

