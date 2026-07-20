## Introduction ##
- This custom OSGi module is a ‘proof of concept’ using a filter to restrict access to the Headless API Explorer GUI and the JSONWS API Explorer GUI.
- It **should not** have any impact on access to the actual web service endpoints themselves.

## Notes ##
- This is a ‘proof of concept’ that is being provided ‘as is’ without any support coverage or warranty.
- It was smoke tested in a local Liferay DXP 7.4 U92 with JDK 11 at compile time and runtime.
- The current simple user logic checks if the users screenName is 'test'. This should be replaced with the required custom logic.
- Ensure the module and any changes are fully tested in a non-production environment for all required use cases, before considering deploying to a production environment.
- All logging is INFO for testing purposes. Consider changing before deploying in production, otherwise it may generate significant amounts of logging.

## JSONWS Explorer ##
- The JSONWS explorer and the JSONWS endpoints are both accessed from /api/jsonws e.g.
  - Explorer URL: http://localhost:8080/api/jsonws or http://localhost:8080/api/jsonws?contextName=audit
  - A sample Endpoint URL: http://localhost:8080/api/jsonws/audit.auditevent/get-audit-events-count?companyId=20096
- The custom component will block requests where the requestURI matches **/api/jsonws** or **/api/jsonws/** (both case insensitive) or has a request parameter named **contextPath**.
  - It will allow requests to for example /api/jsonws/audit.auditevent/ as that is a legitimate invocation of the web service endpoint.
  - The solution intentionally doesn't restrict access to **/api/jsonws/invoke** as it is used by Liferay.Service JavaScript API to invoke endpoints.
- The current logic was smoke tested with cURL and Liferay.Service JavaScript API to ensure that the endpoints are still accessible to a user whose access to the API Explorer is blocked.
```
curl -u "michael:password" "http://localhost:8080/api/jsonws/audit.auditevent/get-audit-events-count?companyId=20096"
```
```
Liferay.Service(
'/audit.auditevent/get-audit-events-count',
{
    companyId: 20096
},
function(obj) {
    console.log(obj);
}
);
```

## Headless Explorer ##
- Calling http://localhost:8080/o/api or similar may trigger 2 requests, both of which are matched by the filter url pattern /o/api/*:
  - /o/api
  - /o/api/main.css
- The /o/api/main.css request is always allowed (silently) to avoid unnecessary duplicate user checks.
- The current logic was smoke tested with cURL to ensure that the endpoints are still accessible to a user whose access to the API Explorer is blocked. For example:
```
curl -u "michael:password" "http://localhost:8080/o/headless-admin-user/v1.0/my-user-account"
```
