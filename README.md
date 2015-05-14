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
        runtime ":cors:1.1.4"
        ...
    }

The default configuration installs a servlet filter that adds the following headers to all OPTIONS requests:

    Access-Control-Allow-Origin: <value of Origin header>
    Access-Control-Allow-Credentials: true
    Access-Control-Allow-Headers: origin, authorization, accept, content-type, x-requested-with
    Access-Control-Allow-Methods: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS
    Access-Control-Max-Age: 3600

The 'Access-Control-Allow-Origin' and 'Access-Control-Allow-Credentials' headers are also added to non-OPTIONS
requests (GET, POST et al.).  If the plugin is configured to produce an 'Access-Control-Expose-Headers' header,
it will be added to non-OPTIONS requests as well.

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

Due to the 'Stringy' nature of external properties files, url patterns can be configured using a comma seperated string such as:

    cors.url.pattern = /api/*, /api-docs/*

You can override the default values used for the headers by supplying a headers map:

    cors.headers = [
        'Access-Control-Allow-Origin': 'http://app.example.com',
        'My-Custom-Header': 'some value']

Due to the 'Stringy' nature of external properties files, headers can be configured using a single line 'string' map:

    cors.headers = ['Access-Control-Allow-Origin': 'http://app.example.com','My-Custom-Header': 'some value']

Note that if you want to specify more than one host for 'Access-Control-Allow-Origin' there are issues with
browser support. The recommended approach is to check the 'Origin' header of the request and echo it back
if you are happy with it. The CORS plugin implements this using a regex to match allowed origins:

    cors.allow.origin.regex = '.*\\.example\\.com'

If 'Origin' header matches the regex then it is echoed back as 'Access-Control-Allow-Origin' otherwise no CORS
headers are sent back to the client and the browser will deny the request.

Note that you can always send back '*' instead of echoing the 'Origin' header by including:

    cors.headers = ['Access-Control-Allow-Origin': '*']

This can be combined with cors.allow.origin.regex to limit allowed domains.

You can specify a comma-delimited list of response headers that should be exposed to the client:

    cors.expose.headers = 'X-app-header1,X-app-header2'

You can disable the filter if needed. Note that the filter defaults to enabled.

    cors.enabled = false

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

License
-------

Copyright 2013 BrandsEye (http://www.brandseye.com/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Changelog
---------
1.1.7:
- Merged PR for Spring Security 2 plugin (thanks neoecos). This plugin will no longer work with Spring Security < 2.0.

1.1.6:
- Reverted to building plugin with Grails 2.2.1. Version 1.1.5 wasn't working with a Grails 2.0.3 app

1.1.5:
- Got rid of deprecated ConfigHolder so plugin works with Grails 2.4
- Removed css and other junk (was causing issues with asset pipeline)

1.1.4:
- Fixed issue with Access-Control-Allow-Origin in cors.headers being ignored. If cors.headers does not contain
  Access-Control-Allow-Origin then any Origin accepted (any or those matching cors.allow.origin.regex) is echoed
  back. If cors.headers does contain Access-Control-Allow-Origin then this value is returned for accepted Origin's
  (i.e. you can use this in combination with cors.allow.origin.regex or set it to '*' to always send back '*' instead
  of the Origin header, useful if the result is cached by a CDN and the Origin varies).

1.1.3:
- Fixed issue with getWebXmlFilterOrder not working in some circumstances (thanks Danilo Tuler)

1.1.2:
- The CORS servlet filter now processes requests ahead of the resources filters (thanks Steve Loeppky)
- OPTIONS requests are no longer passed down the filter chain (thanks Marcus Krantz)

1.1.1: Now works with Spring Security basic authentication (thanks James Hardwick)

1.1.0:
- If 'Access-Control-Allow-Origin' is '*' (the default) then the 'Origin' header is echoed back instead of '*'. This
  is potentially a breaking change but is theoretically "more compliant" with the spec
- No CORS headers are sent back if the client does not supply an 'Origin' header
- Added 'Access-Control-Expose-Headers' option via 'cors.expose.headers' Config.groovy setting
- Added 'cors.enabled' Config.groovy setting to explicitly enable/disabled the filter (filter is enabled by default)

1.0.4: Added Access-Control-Allow-Credentials: true

1.0.3: Bumped version no. to workaround plugin publishing issue

1.0.2: Added Access-Control-Allow-Methods header to OPTIONS request

1.0.1: Added Content-Type to default Access-Control-Allow-Headers
