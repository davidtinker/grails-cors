import com.brandseye.cors.CorsFilter

class CorsGrailsPlugin {
    def version = "1.1.0"
    def grailsVersion = "2.0 > *"
    def title = "CORS Plugin"
    def author = "David Tinker"
    def authorEmail = "david.tinker@gmail.com"
    def description = 'Installs a servlet filter to set Access-Control-Allow-Origin and other CORS related headers to enable cross site AJAX requests to your Grails application'
    def documentation = "https://github.com/davidtinker/grails-cors"

    def license = "APACHE"
    def organization = [ name: "BrandsEye", url: "http://www.brandseye.com/" ]
    def issueManagement = [ system: "Github", url: "https://github.com/davidtinker/grails-cors/issues" ]
    def scm = [ url: "https://github.com/davidtinker/grails-cors" ]

    def doWithWebDescriptor = { xml ->
        def cfg = application.config.cors

        if (cfg.containsKey('enabled') && !cfg.enabled) return

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
                if (cfg.expose.headers) {
                    'init-param' {
                        'param-name'('expose.headers')
                        'param-value'(cfg.expose.headers.toString())
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
}
