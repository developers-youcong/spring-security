package org.springframework.security.web.firewall;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default implementation which wraps requests in order to provide consistent values of the {@code servletPath} and
 * {@code pathInfo}, which do not contain path parameters (as defined in
 * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>). Different servlet containers
 * interpret the servlet spec differently as to how path parameters are treated and it is possible they might be added
 * in order to bypass particular security constraints. When using this implementation, they will be removed for all
 * requests as the request passes through the security filter chain. Note that this means that any segments in the
 * decoded path which contain a semi-colon, will have the part following the semi-colon removed for
 * request matching. Your application should not contain any valid paths which contain semi-colons.
 * <p>
 * If any un-normalized paths are found (containing directory-traversal character sequences), the request will be
 * rejected immediately. Most containers normalize the paths before performing the servlet-mapping, but again this is
 * not guaranteed by the servlet spec.
 *
 * @author Luke Taylor
 */
public class DefaultHttpFirewall implements HttpFirewall {

    public FirewalledRequest getFirewalledRequest(HttpServletRequest request) throws RequestRejectedException {
        FirewalledRequest fwr = new RequestWrapper(request);

        if (!isNormalized(fwr.getServletPath()) || !isNormalized(fwr.getPathInfo())) {
            throw new RequestRejectedException("Un-normalized paths are not supported: " + fwr.getServletPath() +
                (fwr.getPathInfo() != null ? fwr.getPathInfo() : ""));
        }

        return fwr;
    }

    public HttpServletResponse getFirewalledResponse(HttpServletResponse response) {
        return response;
    }

    /**
     * Checks whether a path is normalized (doesn't contain path traversal sequences like "./", "/../" or "/.")
     *
     * @param path the path to test
     * @return true if the path doesn't contain any path-traversal character sequences.
     */
    private boolean isNormalized(String path) {
        if (path == null) {
            return true;
        }

        for (int j = path.length(); j > 0;) {
            int i = path.lastIndexOf('/', j - 1);
            int gap = j - i;

            if (gap == 2 && path.charAt(i+1) == '.') {
                // ".", "/./" or "/."
                return false;
            } else if (gap == 3 && path.charAt(i+1) == '.'&& path.charAt(i+2) == '.') {
                return false;
            }

            j = i;
        }

        return true;
    }

}
