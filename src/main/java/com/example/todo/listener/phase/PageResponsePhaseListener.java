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

public class PageResponsePhaseListener implements PhaseListener {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LogManager.getLogger(PageResponsePhaseListener.class);

    @Inject
    private User user;

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
    
    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        UIViewRoot viewRoot = context.getViewRoot();

        if (viewRoot != null) {
        	
            String userId = "";

            if (user != null && user.getId() != null) {
                userId = String.valueOf(user.getId());
            }
            
            logger.debug("Rendering view " + viewRoot.getViewId() + " for User " + userId);
        	
        }
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        
    }
}