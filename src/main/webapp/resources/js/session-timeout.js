/**
 * Session Timeout Management
 * Tracks user activity and manages 30-minute inactivity logout
 */
(function() {
    'use strict';
    
    // Configuration
    const SESSION_TIMEOUT_MINUTES = 30;
    const WARNING_TIME_MINUTES = 5; // Show warning 5 minutes before timeout
    const CHECK_INTERVAL_SECONDS = 60; // Check every minute
    const ACTIVITY_EVENTS = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
    
    let lastActivityTime = Date.now();
    let warningShown = false;
    let countdownInterval = null;
    let checkInterval = null;
    
    /**
     * Initialize session timeout tracking
     */
    function init() {
        // Track user activity
        ACTIVITY_EVENTS.forEach(function(event) {
            document.addEventListener(event, updateActivity, true);
        });
        
        // Start checking for timeout
        startTimeoutCheck();
        
        // Keep session alive on activity
        setInterval(keepSessionAlive, 60000); // Every minute
    }
    
    /**
     * Update last activity time
     */
    function updateActivity() {
        lastActivityTime = Date.now();
        if (warningShown) {
            hideWarning();
        }
    }
    
    /**
     * Start checking for session timeout
     */
    function startTimeoutCheck() {
        checkInterval = setInterval(function() {
            const now = Date.now();
            const inactiveTime = (now - lastActivityTime) / 1000 / 60; // minutes
            const timeUntilTimeout = SESSION_TIMEOUT_MINUTES - inactiveTime;
            
            // Show warning 5 minutes before timeout
            if (timeUntilTimeout <= WARNING_TIME_MINUTES && !warningShown) {
                showWarning(timeUntilTimeout);
            }
            
            // Logout if timeout reached
            if (inactiveTime >= SESSION_TIMEOUT_MINUTES) {
                logout();
            }
        }, CHECK_INTERVAL_SECONDS * 1000);
    }
    
    /**
     * Show warning dialog
     */
    function showWarning(minutesRemaining) {
        warningShown = true;
        
        const countdownElement = document.getElementById('sessionCountdown');
        let remainingSeconds = Math.floor(minutesRemaining * 60);
        
        // Update countdown
        if (countdownInterval) {
            clearInterval(countdownInterval);
        }
        
        // Update countdown display immediately
        if (countdownElement) {
            const mins = Math.floor(remainingSeconds / 60);
            const secs = remainingSeconds % 60;
            countdownElement.textContent = mins + ':' + (secs < 10 ? '0' : '') + secs;
        }
        
        countdownInterval = setInterval(function() {
            remainingSeconds--;
            if (countdownElement) {
                const mins = Math.floor(remainingSeconds / 60);
                const secs = remainingSeconds % 60;
                countdownElement.textContent = mins + ':' + (secs < 10 ? '0' : '') + secs;
            }
            
            if (remainingSeconds <= 0) {
                clearInterval(countdownInterval);
                logout();
            }
        }, 1000);
        
        // Show dialog (using PrimeFaces if available, otherwise plain JS)
        if (typeof PF !== 'undefined') {
            try {
                PF('sessionTimeoutDialog').show();
            } catch (e) {
                // Fallback to plain JS dialog
                showPlainDialog();
            }
        } else {
            showPlainDialog();
        }
    }
    
    /**
     * Show plain JavaScript dialog as fallback
     */
    function showPlainDialog() {
        let dialog = document.getElementById('sessionTimeoutDialog');
        if (!dialog) {
            dialog = createWarningDialog();
        }
        dialog.style.display = 'block';
    }
    
    /**
     * Hide warning dialog
     */
    function hideWarning() {
        warningShown = false;
        if (countdownInterval) {
            clearInterval(countdownInterval);
            countdownInterval = null;
        }
        
        // Hide PrimeFaces dialog if available
        if (typeof PF !== 'undefined') {
            try {
                PF('sessionTimeoutDialog').hide();
            } catch (e) {
                // Fallback to plain JS
                const dialog = document.getElementById('sessionTimeoutDialog');
                if (dialog) {
                    dialog.style.display = 'none';
                }
            }
        } else {
            const dialog = document.getElementById('sessionTimeoutDialog');
            if (dialog) {
                dialog.style.display = 'none';
            }
        }
    }
    
    /**
     * Create warning dialog element
     */
    function createWarningDialog() {
        const dialog = document.createElement('div');
        dialog.id = 'sessionTimeoutDialog';
        dialog.style.cssText = 'display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 10000;';
        
        const content = document.createElement('div');
        content.style.cssText = 'position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.3); max-width: 400px; width: 90%;';
        
        content.innerHTML = `
            <h3 style="margin-top: 0; color: #dc3545;">Session Timeout Warning</h3>
            <p>Your session will expire due to inactivity in <strong id="sessionCountdown">5:00</strong> minutes.</p>
            <p>Click "Stay Logged In" to continue your session.</p>
            <div style="text-align: right; margin-top: 1.5rem;">
                <button id="stayLoggedInBtn" class="btn btn-primary" style="margin-right: 0.5rem;">Stay Logged In</button>
                <button id="logoutNowBtn" class="btn btn-secondary">Logout Now</button>
            </div>
        `;
        
        dialog.appendChild(content);
        document.body.appendChild(dialog);
        
        // Add event listeners
        document.getElementById('stayLoggedInBtn').addEventListener('click', function() {
            updateActivity();
            hideWarning();
        });
        
        document.getElementById('logoutNowBtn').addEventListener('click', function() {
            logout();
        });
        
        return dialog;
    }
    
    /**
     * Keep session alive by pinging server
     */
    function keepSessionAlive() {
        // Only keep alive if user has been active recently (within last 25 minutes)
        const inactiveTime = (Date.now() - lastActivityTime) / 1000 / 60;
        if (inactiveTime < 25) {
            // The PrimeFaces poll component handles the actual keep-alive
            // This function is just a placeholder for compatibility
            // The poll component will call the keepSessionAlive method on the server
        }
    }
    
    /**
     * Logout and redirect to login page
     */
    function logout() {
        clearInterval(checkInterval);
        if (countdownInterval) {
            clearInterval(countdownInterval);
        }
        
        // Hide dialog if shown
        hideWarning();
        
        // Redirect to login page with timeout parameter
        const basePath = window.location.pathname.replace(/\/todo\/.*/, '');
        window.location.href = basePath + '/login.xhtml?timeout=true';
    }
    
    /**
     * Detect browser back/forward button navigation
     * When user clicks back button, treat it as logout
     */
    function initBackButtonDetection() {
        // Only monitor on protected pages
        if (window.location.pathname.indexOf('/todo/') === -1) {
            return;
        }
        
        // Track if history tracking has been initialized to prevent duplicate listeners
        let historyTrackingInitialized = false;
        
        // Primary method: Listen for pageshow event which fires on back/forward navigation
        // This is the most reliable way to detect when a page is loaded from cache
        window.addEventListener('pageshow', function(event) {
            // event.persisted is true when page is loaded from browser cache (back/forward button)
            if (event.persisted) {
                console.log('Browser back/forward button detected (page loaded from cache) - logging out');
                // Call logout immediately
                logoutOnBackButton();
                return;
            }
        });
        
        // Secondary method: Detect back navigation even when page is not cached
        // Wait for page to fully load before initializing to avoid interfering with login redirect
        function initHistoryTracking() {
            // Prevent multiple initializations
            if (historyTrackingInitialized) {
                return;
            }
            historyTrackingInitialized = true;
            
            // Use History API to track navigation
            if (window.history && window.history.pushState) {
                // Push a state entry to track navigation
                // This allows us to detect when user navigates back
                window.history.pushState({ 
                    backButtonDetection: true,
                    timestamp: Date.now()
                }, '', window.location.href);
                
                // Listen for popstate (back/forward navigation)
                // Note: popstate only fires on user navigation, not when we call pushState
                window.addEventListener('popstate', function(event) {
                    // When popstate fires, user navigated in history
                    // Check if we're still on a protected page
                    if (window.location.pathname.indexOf('/todo/') !== -1) {
                        console.log('Back/forward navigation detected on protected page - logging out');
                        logoutOnBackButton();
                    }
                });
            }
        }
        
        // Initialize history tracking after page is fully loaded
        // Use a longer delay to ensure login redirect and any JSF navigation has completed
        // This prevents interference with normal page loads
        if (document.readyState === 'complete') {
            setTimeout(initHistoryTracking, 2000);
        } else {
            window.addEventListener('load', function() {
                setTimeout(initHistoryTracking, 2000);
            });
        }
    }
    
    /**
     * Logout when back button is detected
     * This will invalidate the session and redirect to login
     */
    function logoutOnBackButton() {
        // Try to call logout via AJAX if PrimeFaces is available
        if (typeof PrimeFaces !== 'undefined') {
            try {
                // Use PrimeFaces remote command to call logout
                // The source ID should include form prefix if needed
                const buttonId = 'backButtonLogoutForm:backButtonLogoutBtn';
                PrimeFaces.ab({
                    source: buttonId,
                    event: 'click',
                    process: '@this',
                    update: '@none',
                    oncomplete: function() {
                        // Redirect to login after logout
                        const basePath = window.location.pathname.replace(/\/todo\/.*/, '');
                        window.location.href = basePath + '/login.xhtml?backButton=true';
                    },
                    onerror: function() {
                        // If AJAX fails, redirect directly
                        const basePath = window.location.pathname.replace(/\/todo\/.*/, '');
                        window.location.href = basePath + '/login.xhtml?backButton=true';
                    }
                });
            } catch (e) {
                console.error('Error calling logout via PrimeFaces', e);
                // Fallback: direct redirect
                const basePath = window.location.pathname.replace(/\/todo\/.*/, '');
                window.location.href = basePath + '/login.xhtml?backButton=true';
            }
        } else {
            // Fallback: direct redirect (session will be checked by Shiro)
            const basePath = window.location.pathname.replace(/\/todo\/.*/, '');
            window.location.href = basePath + '/login.xhtml?backButton=true';
        }
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            init();
            initBackButtonDetection();
        });
    } else {
        init();
        initBackButtonDetection();
    }
    
    // Expose functions for external use
    window.sessionTimeoutManager = {
        updateActivity: updateActivity,
        logout: logout,
        logoutOnBackButton: logoutOnBackButton
    };
})();

