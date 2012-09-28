import org.springframework.web.filter.DelegatingFilterProxy
import com.brandseye.cors.CorsFilter

class CorsGrailsPlugin {
    // the plugin version
    def version = "1.0.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "CORS Plugin" // Headline display name of the plugin
    def author = "David Tinker"
    def authorEmail = "david.tinker@gmail.com"
    def description = '''\
Installs a servlet filter to set Access-Control-Allow-Origin and other CORS related headers to enable cross site
AJAX requests to your Grails application
'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/davidtinker/grails-cors"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "BrandsEye", url: "http://www.brandseye.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "Github", url: "https://github.com/davidtinker/grails-cors/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/davidtinker/grails-cors" ]

    def doWithWebDescriptor = { xml ->
        def cfg = application.config.cors

        def contextParam = xml.'context-param'
        contextParam[contextParam.size() - 1] + {
            'filter' {
                'filter-name'('cors-headers')
                'filter-class'(CorsFilter.name)
                if (cfg.allow.origin.regex) {
                    'init-param' {
                        'param-name'('allow.origin.regex')
                        'param-value'(cfg.allow.origin.regex.toString())
                    }
                }
                if (cfg.headers instanceof Map) {
                    cfg.headers.each { k,v ->
                        'init-param' {
                            'param-name'('header:' + k)
                            'param-value'(v)
                        }
                    }
                }
            }
        }

        def urlPattern = cfg.url.pattern ?: '/*'
        List list = urlPattern instanceof List ? urlPattern : [urlPattern]

        def filter = xml.'filter'
        list.each { pattern ->
            filter[0] + {
                'filter-mapping'{
                    'filter-name'('cors-headers')
                    'url-pattern'(pattern)
                }
            }
        }
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
