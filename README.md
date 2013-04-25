CORS Plugin
===========

Grails plugin to add Cross-Origin Resource Sharing (CORS) headers for Grails applications.
These headers make it possible for Javascript code served from a different host to easily make calls to your
application.

 * http://en.wikipedia.org/wiki/Cross-origin_resource_sharing
 * http://www.w3.org/TR/access-control/

It is not easy to do this in a Grails application due to the following bug: http://jira.grails.org/browse/GRAILS-5531

Using
-----

Add a dependency to BuildConfig.groovy:

    plugins {
        runtime ":cors:1.0.4"
        ...
    }

The default configuration installs a servlet filter that adds the following headers to all OPTIONS requests:

    Access-Control-Allow-Origin: <value of Origin header>
    Access-Control-Allow-Credentials: true
    Access-Control-Allow-Headers: origin, authorization, accept, content-type, x-requested-with
    Access-Control-Allow-Methods: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS
    Access-Control-Max-Age: 3600

The 'Access-Control-Allow-Origin' and 'Access-Control-Allow-Credentials' headers are also added to non-OPTIONS
requests (GET, POST et al.).  If the plugin is configured to produce an 'Access-Control-Expose-Headers' header, it will be added to non-OPTIONS requests as well.

These headers permit Javascript code loaded from anywhere to make AJAX calls to your application including specifying
an 'Authorization' header (e.g. for Basic authentication). The browser will cache the results of the OPTIONS request
for 1 hour (3600 seconds).

Configuration
-------------

The CORS plugin is configured through Config.groovy.

You can limit the URL patterns the filter is applied to:

    cors.url.pattern = '/rest/*'

This parameter also accepts a list of patterns to match:

    cors.url.pattern = ['/service1/*', '/service2/*']

You can override the default values used for the headers by supplying a headers map:

    cors.headers = [
        'Access-Control-Allow-Origin': 'http://app.example.com',
        'My-Custom-Header': 'some value']

Note that if you want to specify more than one host for 'Access-Control-Allow-Origin' there are issues with
browser support. The recommended approach is to check the 'Origin' header of the request and echo it back
if you are happy with it. The CORS plugin implements this using a regex to match allowed origins:

    cors.allow.origin.regex = '.*\\.example\\.com'

If 'Origin' header matches the regex then it is echoed back as 'Access-Control-Allow-Origin' otherwise no CORS
headers are sent back to the client and the browser will deny the request.

You can specify a comma-delimited list of response headers that should be exposed to the client:

    cors.expose.headers = 'X-app-header1,X-app-header2'

Client Performance Notes
------------------------

The browser only has to make an extra OPTIONS request for a cross-site AJAX GET if additional headers are specified.
So this cross site jQuery AJAX call does not result in an OPTIONS request:

    $.ajax("https://api.notmyserver.com/rest/stuff/123", {
        data: data,
        dataType: 'json',
        type: 'get',
        success: callback,
    });

Whereas this one does (at least the first time):

    $.ajax("https://api.notmyserver.com/rest/stuff/123", {
        data: data,
        dataType: 'json',
        type: 'get',
        success: callback,
        headers: {Authorization: "Basic ..."}
    });

So if you can authenticate or whatever using query parameters instead of headers it can reduce latency.

Changelog
---------
1.0.5: Added Access-Control-Expose-Headers

1.0.4: Added Access-Control-Allow-Credentials: true

1.0.3: Bumped version no. to workaround plugin publishing issue

1.0.2: Added Access-Control-Allow-Methods header to OPTIONS request

1.0.1: Added Content-Type to default Access-Control-Allow-Headers
