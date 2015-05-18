/*
 * Copyright 2013 BrandsEye (http://www.brandseye.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.brandseye.cors.CorsCompatibleBasicAuthenticationEntryPoint
import com.brandseye.cors.CorsFilter

class CorsGrailsPlugin {
    def version = "1.1.8"
    def grailsVersion = "2.0 > *"
    def title = "CORS Plugin"
    def author = "David Tinker"
    def authorEmail = "david.tinker@gmail.com"
    def description = 'Installs a servlet filter to set Access-Control-Allow-Origin and other CORS related headers to enable cross site AJAX requests to your Grails application'
    def documentation = "https://github.com/davidtinker/grails-cors"

    // Make sure this loads after Spring Security otherwise we'll lose our custom BasicAuthenticationEntryPoint bean
    def loadAfter = ['springSecurityCore']

    def license = "APACHE"
    def organization = [ name: "BrandsEye", url: "http://www.brandseye.com/" ]
    def issueManagement = [ system: "Github", url: "https://github.com/davidtinker/grails-cors/issues" ]
    def scm = [ url: "https://github.com/davidtinker/grails-cors" ]

    def doWithSpring = {
        // If using Spring Security's Basic Auth, swap out the BasicAuthenticationEntryPoint bean to prevent
        // authenticating on OPTIONS requests. See https://github.com/davidtinker/grails-cors/issues/12 for more info.
        if (application.config.grails.plugin.springsecurity?.useBasicAuth) {
            basicAuthenticationEntryPoint(CorsCompatibleBasicAuthenticationEntryPoint) { bean ->
                realmName = application.config.grails.plugin.springsecurity.basic.realmName
            }
        }
    }

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
                } else if (cfg.headers instanceof String) {
                    def headerMap = Eval.me(cfg.headers?.toString())
                    if(headerMap instanceof Map){
                        cfg.headers.each { k,v ->
                            'init-param' {
                                'param-name'('header:' + k)
                                'param-value'(v)
                            }
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

        def patterns = cfg.url.pattern ?: '/*'
        def urlPatterns = patterns instanceof List ? patterns : patterns.split(',').collect{ it.trim() }

        def filter = xml.'filter'
        urlPatterns.each { pattern ->
            filter[0] + {
                'filter-mapping'{
                    'filter-name'('cors-headers')
                    'url-pattern'(pattern)
                }
            }
        }
    }

    def getWebXmlFilterOrder() {
        def FilterManager = getClass().getClassLoader().loadClass('grails.plugin.webxml.FilterManager')
        // Be before the earliest Resource filter.
        ['cors-headers': FilterManager.DEFAULT_POSITION - 400]
    }
}
