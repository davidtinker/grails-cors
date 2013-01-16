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
@TestFor(CorsFilter)
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

        }
    }

}
