/**
 * Copyright (c) 2002-2015, JWebUnit team.
 *
 * This file is part of JWebUnit.
 *
 * JWebUnit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JWebUnit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JWebUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jwebunit.htmlunit;

import junit.framework.TestCase;
import org.htmlunit.WebClient;
import org.htmlunit.util.Cookie;

import java.util.Date;
import java.util.List;

/**
 * Unit tests verifying that HtmlUnitTestingEngineImpl.getCookies() correctly maps
 * org.htmlunit.util.Cookie properties to javax.servlet.http.Cookie without going
 * through Cookie.toHttpClient() (which returns an HC5 type inaccessible in HtmlUnit 3.x).
 *
 * <p>Each test creates a real WebClient, populates its CookieManager with a real
 * org.htmlunit.util.Cookie instance, then asserts the mapped javax.servlet.http.Cookie
 * properties are correct.</p>
 */
public class HtmlUnitCookieMappingTest extends TestCase {

    private WebClient client;
    private HtmlUnitTestingEngineImpl engine;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = new WebClient();
        engine = new HtmlUnitTestingEngineImpl(client);
    }

    @Override
    protected void tearDown() throws Exception {
        client.close();
        super.tearDown();
    }

    // --- expiry mapping ---

    public void testCookieWithFutureExpiryHasPositiveMaxAge() {
        Date futureDate = new Date(System.currentTimeMillis() + 3_600_000L); // 1 hour ahead
        addCookie("example.com", "session", "abc123", "/", futureDate, false);

        List<javax.servlet.http.Cookie> result = engine.getCookies();

        assertEquals(1, result.size());
        assertTrue("Max-Age should be positive for a future expiry date",
                result.get(0).getMaxAge() > 0);
    }

    public void testCookieWithNullExpiryHasNegativeOneMaxAge() {
        addCookie("example.com", "session", "abc123", "/", null, false);

        List<javax.servlet.http.Cookie> result = engine.getCookies();

        assertEquals(1, result.size());
        assertEquals("Session cookie (no expiry) should have maxAge=-1",
                -1, result.get(0).getMaxAge());
    }

    // --- secure flag mapping ---

    public void testCookieSecureFlagMapped() {
        addCookie("example.com", "secureCookie", "val", "/", null, true);

        List<javax.servlet.http.Cookie> result = engine.getCookies();

        assertEquals(1, result.size());
        assertTrue("Secure flag should be true when HtmlUnit cookie is secure",
                result.get(0).getSecure());
    }

    public void testCookieInsecureFlagMapped() {
        addCookie("example.com", "insecureCookie", "val", "/", null, false);

        List<javax.servlet.http.Cookie> result = engine.getCookies();

        assertEquals(1, result.size());
        assertFalse("Secure flag should be false when HtmlUnit cookie is not secure",
                result.get(0).getSecure());
    }

    // --- name, value, domain, path mapping ---

    public void testCookieNameValueDomainPathMapped() {
        addCookie("my.domain.com", "cookieName", "cookieValue", "/app/path", null, false);

        List<javax.servlet.http.Cookie> result = engine.getCookies();

        assertEquals(1, result.size());
        javax.servlet.http.Cookie mapped = result.get(0);
        assertEquals("cookieName", mapped.getName());
        assertEquals("cookieValue", mapped.getValue());
        assertEquals("my.domain.com", mapped.getDomain());
        assertEquals("/app/path", mapped.getPath());
    }

    // --- multiple cookies ---

    public void testMultipleCookiesMapped() {
        addCookie("example.com", "cookie1", "val1", "/", null, false);
        addCookie("example.com", "cookie2", "val2", "/api", null, true);

        List<javax.servlet.http.Cookie> result = engine.getCookies();

        assertEquals("Both cookies should be mapped", 2, result.size());
    }

    // --- empty cookie jar ---

    public void testNoCookiesReturnsEmptyList() {
        // CookieManager is empty by default after WebClient construction
        List<javax.servlet.http.Cookie> result = engine.getCookies();

        assertNotNull("getCookies() must not return null", result);
        assertEquals("Empty cookie jar should return empty list", 0, result.size());
    }

    // --- helper ---

    private void addCookie(String domain, String name, String value,
                           String path, Date expires, boolean secure) {
        client.getCookieManager().addCookie(
                new Cookie(domain, name, value, path, expires, secure));
    }
}
