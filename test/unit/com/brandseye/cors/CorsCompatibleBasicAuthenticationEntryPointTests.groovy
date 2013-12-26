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

package com.brandseye.cors

import groovy.mock.interceptor.MockFor
import org.springframework.security.core.AuthenticationException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CorsCompatibleBasicAuthenticationEntryPointTests extends GroovyTestCase {


    public void testOptionsPreflight() {
        // given
        def baEntryPoint = new CorsCompatibleBasicAuthenticationEntryPoint()
        def mockRequestContext = new MockFor(HttpServletRequest)
        mockRequestContext.demand.getMethod { "OPTIONS" }
        def mockRequest = mockRequestContext.proxyDelegateInstance()
        def mockResponseContext = new MockFor(HttpServletResponse)
        mockResponseContext.demand.sendError(0)
        def mockResponse = mockResponseContext.proxyDelegateInstance()

        // when
        baEntryPoint.commence(mockRequest, mockResponse, null)

        // then
        mockRequestContext.verify mockRequest
        mockResponseContext.verify mockResponse
    }

    public void testAllOthersNeedAuth() {
        methodTester("GET")
        methodTester("PUT")
        methodTester("DELETE")
        methodTester("POST")
    }

    private void methodTester(String method) {
        // given
        def baEntryPoint = new CorsCompatibleBasicAuthenticationEntryPoint()
        baEntryPoint.setRealmName("Test Realm")
        def mockRequestContext = new MockFor(HttpServletRequest)
        mockRequestContext.demand.getMethod { method }
        def mockRequest = mockRequestContext.proxyDelegateInstance()
        def mockResponseContext = new MockFor(HttpServletResponse)
        mockResponseContext.demand.addHeader(1) { String name, String value -> }
        mockResponseContext.demand.sendError(1) { int error, String msg ->
            assert error == HttpServletResponse.SC_UNAUTHORIZED
        }
        def mockResponse = mockResponseContext.proxyDelegateInstance()
        def authExceptionContext = new MockFor(MockedAuthException)
        def mockAuthException = authExceptionContext.proxyDelegateInstance()

        // when
        baEntryPoint.commence(mockRequest, mockResponse, mockAuthException)

        // then
        mockRequestContext.verify mockRequest
        mockResponseContext.verify mockResponse
    }

}

class MockedAuthException extends AuthenticationException {
    public MockedAuthException() {
        super("MockedAuthException")
    }
}
