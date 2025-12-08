package com.example.todo.listener.phase;

import java.util.List;
import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.todo.model.User;

public class UserInteractionPhaseListener implements PhaseListener {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(UserInteractionPhaseListener.class);


    @Inject
    private User user;

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
    	
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();

        if (context.isPostback()) {
            
            String userId = "";

            if (user != null && user.getId() != null) {
                userId = String.valueOf(user.getId());
            }
            
            UIViewRoot viewRoot = context.getViewRoot();
            String pageUrl = (viewRoot != null) ? viewRoot.getViewId() : "Unknown Page";

            Map<String, String> requestParams = context.getExternalContext().getRequestParameterMap();

            logTriggerSource(context, requestParams, userId, pageUrl);
            
            if (viewRoot != null) {
                scanForChanges(context, viewRoot, requestParams, userId, pageUrl);
            }
        }
    }

    private void logTriggerSource(FacesContext context, Map<String, String> params, String user, String page) {
        String sourceId = params.get("javax.faces.source");

        if (sourceId != null && !sourceId.isEmpty()) {
            UIComponent component = context.getViewRoot().findComponent(sourceId);
            if (component instanceof UICommand) {
                String type = component.getClass().getSimpleName();
                String name = getComponentLabel(component); // Get the component name/label
                
                logger.debug("ACTION: User [" + user + "] on [" + page + "] clicked " + type + 
                                   " [Name: " + name + "] [ID: " + sourceId + "]");
            }
        }
    }

    private void scanForChanges(FacesContext context, UIComponent parent, Map<String, String> params, String user, String page) {
        List<UIComponent> children = parent.getChildren();
        
        for (UIComponent child : children) {
            
            if (child instanceof UIInput) {
                UIInput input = (UIInput) child;
                String clientId = input.getClientId(context);
                
                if (params.containsKey(clientId)) {
                    
                    String submittedString = params.get(clientId);
                    Object oldValue = input.getValue();
                    
                    String oldString = (oldValue != null) ? oldValue.toString() : "";
                    
                    if (submittedString != null) {
                         
                         if (!submittedString.equals(oldString)) {
                             String type = input.getClass().getSimpleName();
                             String name = getComponentLabel(input); // Get the component name/label
                             
                             // Clean up the log: Don't print if both are empty
                             if (!submittedString.isEmpty() || !oldString.isEmpty()) {
                                logger.debug("ACTION: User [" + user + "] on [" + page + "] submitted " + type + 
                                                " [Name: " + name + "] [ID: " + clientId + "] Value: '" + submittedString + "'");
                             }
                         }
                    }
                }
            }

            scanForChanges(context, child, params, user, page);
        }
    }

    private String getComponentLabel(UIComponent component) {
        Object label = component.getAttributes().get("label");
        if (label != null) return label.toString();

        Object value = component.getAttributes().get("value");
        if (value != null) return value.toString();
        
        Object title = component.getAttributes().get("title");
        if (title != null) return title.toString();

        return "No Label";
    }
}
