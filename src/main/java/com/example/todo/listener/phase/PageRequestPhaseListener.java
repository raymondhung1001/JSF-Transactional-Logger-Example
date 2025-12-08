package com.example.todo.listener.phase;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.todo.model.User;

public class PageRequestPhaseListener implements PhaseListener {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LogManager.getLogger(PageRequestPhaseListener.class);

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
        UIViewRoot viewRoot = context.getViewRoot();

        if (viewRoot != null) {
        	
            String userId = "";

            if (user != null && user.getId() != null) {
                userId = String.valueOf(user.getId());
            }
            
            logger.debug("User " + userId + " is accessing view " + viewRoot.getViewId());
        	
        }
    }
}