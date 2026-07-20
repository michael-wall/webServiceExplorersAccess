package com.mw;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.PortalUtil;

import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
    property = {
    	"servlet-context-name=", 
    	"servlet-filter-name=Headless Explorer Filter",
        "url-pattern=/o/api/*",
        "url-pattern=/api/jsonws/*"
    },
    service = Filter.class
)
public class APIExplorerServletFilter extends BaseFilter {
	
	@Activate
	protected void activate(Map<String, Object> properties)  throws Exception {
		_log.info("activated");
	}	

    @Override
    protected void processFilter(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws Exception {
    	
    	HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        boolean restrictedHeadlessEndpoint = false;
        boolean restrictedJsonwsEndpoint = false;
        
        _log.info("requestURI: " + request.getRequestURI());
        _log.info("queryString: " + request.getQueryString());
        
        if (request.getRequestURI().toLowerCase().startsWith("/o/api")) { // Explorer called
        	restrictedHeadlessEndpoint = true;
        }
        
        if (!restrictedHeadlessEndpoint) {
            // Means they are accessing the Explorer
        	// We most not block /api/jsonws/audit.auditevent/get-audit-events-count for example...
            if (request.getRequestURI().equalsIgnoreCase("/api/jsonws") || request.getRequestURI().equalsIgnoreCase("/api/jsonws/")) {
            	restrictedJsonwsEndpoint = true;
            } else if (request.getQueryString().toLowerCase().contains("contentPath".toLowerCase())) {
            	restrictedJsonwsEndpoint = true;
            }
        }
        
        if (restrictedJsonwsEndpoint || restrictedHeadlessEndpoint) {
            long userId = PortalUtil.getUserId(httpRequest);
            User user = UserLocalServiceUtil.fetchUser(userId);
        	
            if (user == null || !user.getScreenName().equalsIgnoreCase("test")) {
            	_log.info("Blocking...");
            	
            	//new ServletException("Blocked");	
            	response.sendError(HttpServletResponse.SC_FORBIDDEN);
            	return;
            } else {
            	_log.info("Allowing...");
            	
            	filterChain.doFilter(request, response);  
            }	
        } else {
        	_log.info("Bypassing Access check...");
        	
        	filterChain.doFilter(request, response);        	
        }
    }

	@Override
	protected Log getLog() {
		return _log;
	}
	
	private static final Log _log = LogFactoryUtil.getLog(APIExplorerServletFilter.class);
}