package com.example.todo.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.todo.model.Todo;
import com.example.todo.model.User;
import com.example.todo.service.TodoService;
import com.example.todo.service.UserService;

@ViewScoped
@Named
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
        logger.info("=== TodoBean @PostConstruct initialized ===");
        loadTodos();
    }

    public void loadTodos() {
        logger.info("=== loadTodos() CALLED ===");
        
        if (loginBean == null) {
            logger.error("LoginBean is NULL!");
            this.todos = new ArrayList<>();
            return;
        }
        
        if (!loginBean.isLoggedIn()) {
            logger.error("User is NOT logged in!");
            this.todos = new ArrayList<>();
            return;
        }
        
        User currentUser = loginBean.getCurrentUser();
        if (currentUser == null) {
            logger.error("Current user is NULL!");
            this.todos = new ArrayList<>();
            return;
        }
        
        currentUserId = currentUser.getId();
        logger.info("Current user ID: {}, username: {}", currentUserId, currentUser.getUsername());
        
        try {
            List<Todo> fetchedTodos = todoService.findByUser(currentUserId);
            logger.info("Service returned {} todos", fetchedTodos != null ? fetchedTodos.size() : "NULL");
            
            if (fetchedTodos == null) {
                this.todos = new ArrayList<>();
                logger.warn("Service returned NULL, created empty list");
            } else {
                this.todos = new ArrayList<>(fetchedTodos);
                logger.info("Created new ArrayList with {} todos", this.todos.size());
                
                for (int i = 0; i < this.todos.size(); i++) {
                    Todo t = this.todos.get(i);
                    logger.info("  [{}] Todo: id={}, title='{}', userId={}", 
                               i, t.getId(), t.getTitle(), t.getUser() != null ? t.getUser().getId() : "NULL");
                }
            }
        } catch (Exception e) {
            logger.error("ERROR loading todos: ", e);
            this.todos = new ArrayList<>();
        }
        
        logger.info("=== loadTodos() COMPLETED - Final list size: {} ===", this.todos.size());
    }

    public void addTodo() {
        logger.info("=== addTodo() CALLED ===");
        logger.info("Title: '{}'", title);
        logger.info("Description: '{}'", description);
        
        if (loginBean == null || !loginBean.isLoggedIn()) {
            logger.error("User not logged in!");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "You must be logged in"));
            return;
        }
        
        if (title == null || title.trim().isEmpty()) {
            logger.error("Title is empty!");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Title is required"));
            return;
        }
        
        try {
            User user = loginBean.getCurrentUser();
            logger.info("Creating todo for user: id={}, username={}", user.getId(), user.getUsername());
            
            Todo newTodo = todoService.createTodo(title, description, user);
            
            logger.info("Todo created by service: id={}, title='{}'", newTodo.getId(), newTodo.getTitle());
            
            // Verify the todo was actually saved
            List<Todo> verifyList = todoService.findByUser(user.getId());
            logger.info("Verification: Service now has {} todos for this user", 
                       verifyList != null ? verifyList.size() : "NULL");
            
            // Clear form
            this.title = null;
            this.description = null;
            this.selectedTodo = null;
            
            // Reload the list
            logger.info("Calling loadTodos() to refresh the list...");
            loadTodos();
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                                   "Todo added successfully. Total todos: " + (todos != null ? todos.size() : 0)));
            
        } catch (Exception e) {
            logger.error("ERROR adding todo: ", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                                   "Failed to add todo: " + e.getMessage()));
        }
        
        logger.info("=== addTodo() COMPLETED ===");
    }

    public void updateTodo() {
        logger.info("=== updateTodo() CALLED ===");
        
        if (selectedTodo == null) {
            logger.error("selectedTodo is NULL!");
            return;
        }
        
        if (title == null || title.trim().isEmpty()) {
            logger.error("Title is empty!");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Title is required"));
            return;
        }
        
        try {
            logger.info("Updating todo id={} with title='{}'", selectedTodo.getId(), title);
            
            selectedTodo.setTitle(title);
            selectedTodo.setDescription(description);
            todoService.updateTodo(selectedTodo);
            
            logger.info("Todo updated successfully");
            
            // Clear form
            this.selectedTodo = null;
            this.title = null;
            this.description = null;
            
            // Reload
            loadTodos();
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Todo updated successfully"));
            
        } catch (Exception e) {
            logger.error("ERROR updating todo: ", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                                   "Failed to update todo: " + e.getMessage()));
        }
        
        logger.info("=== updateTodo() COMPLETED ===");
    }

    public void deleteTodo(Long id) {
        logger.info("=== deleteTodo() CALLED for id={} ===", id);
        
        try {
            todoService.deleteTodo(id);
            logger.info("Todo deleted successfully");
            
            loadTodos();
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Todo deleted successfully"));
            
        } catch (Exception e) {
            logger.error("ERROR deleting todo: ", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                                   "Failed to delete todo: " + e.getMessage()));
        }
        
        logger.info("=== deleteTodo() COMPLETED ===");
    }

    public void toggleComplete(Long id) {
        logger.info("=== toggleComplete() CALLED for id={} ===", id);
        
        try {
            todoService.toggleComplete(id);
            logger.info("Todo completion toggled successfully");
            
            loadTodos();
            
        } catch (Exception e) {
            logger.error("ERROR toggling todo completion: ", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                                   "Failed to update todo: " + e.getMessage()));
        }
        
        logger.info("=== toggleComplete() COMPLETED ===");
    }

    public void selectTodo(Todo todo) {
        logger.info("=== selectTodo() CALLED for id={} ===", todo != null ? todo.getId() : "NULL");
        
        if (todo == null) {
            logger.error("todo parameter is NULL!");
            return;
        }
        
        this.selectedTodo = todo;
        this.title = todo.getTitle();
        this.description = todo.getDescription();
        
        logger.info("Selected todo: id={}, title='{}'", todo.getId(), todo.getTitle());
    }

    public void cancelEdit() {
        logger.info("=== cancelEdit() CALLED ===");
        this.selectedTodo = null;
        this.title = null;
        this.description = null;
    }

    public void prepareAddTodo() {
        logger.info("=== prepareAddTodo() CALLED ===");
        this.selectedTodo = null;
        this.title = null;
        this.description = null;
    }

    public void saveTodo() {
        logger.info("=== saveTodo() CALLED ===");
        logger.info("editMode: {}", isEditMode());
        logger.info("title: '{}'", title);
        logger.info("description: '{}'", description);
        logger.info("selectedTodo: {}", selectedTodo != null ? selectedTodo.getId() : "NULL");
        
        if (selectedTodo == null) {
            logger.info("Calling addTodo()...");
            addTodo();
        } else {
            logger.info("Calling updateTodo()...");
            updateTodo();
        }
        
        logger.info("=== saveTodo() COMPLETED ===");
    }

    public boolean isEditMode() {
        boolean editMode = selectedTodo != null;
        logger.debug("isEditMode() = {}", editMode);
        return editMode;
    }

    // Getters and Setters
    public List<Todo> getTodos() {
        logger.debug("getTodos() called - returning {} todos", todos != null ? todos.size() : "NULL");
        
        if (todos == null && loginBean != null && loginBean.isLoggedIn()) {
            logger.warn("todos is NULL but user is logged in - loading todos");
            loadTodos();
        }
        
        return todos;
    }

    public void setTodos(List<Todo> todos) {
        logger.debug("setTodos() called with {} todos", todos != null ? todos.size() : "NULL");
        this.todos = todos;
    }

    public Todo getSelectedTodo() {
        return selectedTodo;
    }

    public void setSelectedTodo(Todo selectedTodo) {
        this.selectedTodo = selectedTodo;
    }

    public String getTitle() {
        logger.debug("getTitle() = '{}'", title);
        return title;
    }

    public void setTitle(String title) {
        logger.debug("setTitle('{}') called", title);
        this.title = title;
    }

    public String getDescription() {
        logger.debug("getDescription() = '{}'", description);
        return description;
    }

    public void setDescription(String description) {
        logger.debug("setDescription('{}') called", description);
        this.description = description;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }
}