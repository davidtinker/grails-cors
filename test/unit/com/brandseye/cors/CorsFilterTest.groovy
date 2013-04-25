package com.brandseye.cors

import grails.test.mixin.TestFor

import org.junit.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockFilterConfig
import org.springframework.mock.web.MockHttpServletRequest
import grails.util.MockHttpServletResponse

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Peter Schneider-Manzell
 */
class CorsFilterTest {

    CorsFilter underTest;
    def minimalHeadersForAllAllowedRequests = ["Access-Control-Allow-Origin","Access-Control-Allow-Credentials"]
    def defaultHeadersForAllowedOptionsRequest = [
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Methods",
            "Access-Control-Max-Age"
    ]

    public void testDefaultHeadersForOptionsRequestsWithoutOriginRegexp(){
      def expectedHeadersForHttpMethods = [
              'OPTIONS':defaultHeadersForAllowedOptionsRequest,
              'GET':minimalHeadersForAllAllowedRequests,
              'PUT':minimalHeadersForAllAllowedRequests,
              'DELETE':minimalHeadersForAllAllowedRequests,
              'POST':minimalHeadersForAllAllowedRequests
      ]
      String origin = "http://www.grails.org"
      executeTest([:],expectedHeadersForHttpMethods,origin)
    }

    public void testDefaultHeadersForOptionsRequestsWithOriginRegexpAndAllowedOrigin(){
        def expectedHeadersForHttpMethods = [
                'OPTIONS':defaultHeadersForAllowedOptionsRequest,
                'GET':minimalHeadersForAllAllowedRequests,
                'PUT':minimalHeadersForAllAllowedRequests,
                'DELETE':minimalHeadersForAllAllowedRequests,
                'POST':minimalHeadersForAllAllowedRequests
        ]
        def filterInitParams = [:]
        filterInitParams['allow.origin.regex'] = '.*grails.*'
        String origin = "http://www.grails.org"
        executeTest(filterInitParams,expectedHeadersForHttpMethods,origin)
    }

    public void testDefaultHeadersForOptionsRequestsWithOriginRegexpAndNotAllowedOrigin(){
        def expectedHeadersForHttpMethods = [
                'OPTIONS':[],
                'GET':[],
                'PUT':[],
                'DELETE':[],
                'POST':[]
        ]
        def filterInitParams = [:]
        filterInitParams['allow.origin.regex'] = '.*grails.*'
        String origin = "http://www.google.com"
        executeTest(filterInitParams,expectedHeadersForHttpMethods,origin)
    }

    public void testDefaultHeadersForOptionsRequestsWithoutOriginRegexpAndNotAllowedOrigin(){
        def expectedHeadersForHttpMethods = [
                'OPTIONS':[],
                'GET':[],
                'PUT':[],
                'DELETE':[],
                'POST':[]
        ]
        def filterInitParams = [:]
        filterInitParams['header:Access-Control-Allow-Origin'] = 'http://www.grails.org'
        String origin = "http://www.google.com"
        executeTest(filterInitParams,expectedHeadersForHttpMethods,origin)
    }

    public void testExposedHeaders() {
        def headers = ["Access-Control-Allow-Origin","Access-Control-Allow-Credentials","Access-Control-Expose-Headers"]
        def expectedHeadersForHttpMethods = [
                'OPTIONS':defaultHeadersForAllowedOptionsRequest,
                'GET':headers,
                'PUT':headers,
                'DELETE':headers,
                'POST':headers
        ]
        def filterInitParams = [:]
        filterInitParams['allow.origin.regex'] = '.*grails.*'
        filterInitParams['expose.headers'] = "X-custom-header"
        String origin = "http://www.grails.org"
        executeTest(filterInitParams,expectedHeadersForHttpMethods,origin)
    }

    public void testValueOfExposedHeaders() {
        def underTest = new CorsFilter()
        def expected = 'X-custom-header1,X-custom-header2'
        MockFilterConfig filterConfig = new MockFilterConfig()
        filterConfig.addInitParameter('allow.origin.regex','.*grails.*')
        filterConfig.addInitParameter('expose.headers',expected)
        underTest.init(filterConfig)

        HttpServletRequest req = new MockHttpServletRequest()
        req.setMethod("PUT")
        req.addHeader("Origin",'http://www.grails.org')
        HttpServletResponse res = new MockHttpServletResponse()
        underTest.doFilter(req,res,new MockFilterChain())
        assert  res.getHeader('Access-Control-Expose-Headers').equals('X-custom-header1,X-custom-header2') : "Access-Control-Expose-Headers, expected ${expected}, got ${res.getHeader('Access-Control-Expose-Headers')}"

    }


    private void executeTest(Map<String,String> filterInitParams,Map<String,Set<String>> expectedHeadersForHttpMethods,String origin){
        underTest = new CorsFilter()
        MockFilterConfig filterConfig = new MockFilterConfig()
        filterInitParams.entrySet().each {filterEntry->
            filterConfig.addInitParameter(filterEntry.key,filterEntry.value)
        }
        underTest.init(filterConfig)


        expectedHeadersForHttpMethods.entrySet().each {expectedHeadersForHttpMethod->
            String httpMethod =  expectedHeadersForHttpMethod.key
            Set<String> expectedHeaders = expectedHeadersForHttpMethod.value
            HttpServletRequest req = new MockHttpServletRequest()
            req.setMethod(httpMethod)
            req.addHeader("Origin",origin)
            HttpServletResponse res = new MockHttpServletResponse()
            underTest.doFilter(req,res,new MockFilterChain())
            assert  res.getHeaderNames().containsAll(expectedHeaders) : "Not all expected headers for request $httpMethod and origin $origin could be found! (expected: $expectedHeaders, got: ${res.getHeaderNames()})"
            assert  res.getHeaderNames().size() == expectedHeaders.size() : "Header count for request $httpMethod and origin $origin not the expected header count! (expected: $expectedHeaders, got: ${res.getHeaderNames()})"

            //check that the Access-Control-Allow-Credentials is always single-valued
            if (res.getHeaderNames().contains('Access-Control-Allow-Credentials')) {
                assert res.getHeaders('Access-Control-Allow-Credentials').size() == 1
            }
            //check that the Access-Control-Allow-Origin is always single-valued,
            //since we always set Access-Control-Allow-Credentials
            if (res.getHeaderNames().contains('Access-Control-Allow-Origin')) {
                assert res.getHeaders('Access-Control-Allow-Origin').size() == 1
            }

        }
    }

}
