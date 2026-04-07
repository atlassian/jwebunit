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

import net.sourceforge.jwebunit.tests.JWebUnitAPITestCase;
import org.junit.Test;

import static net.sourceforge.jwebunit.junit.JWebUnit.*;

/**
 * Integration tests verifying that WebTester.assertCheckboxPresent() and
 * assertCheckboxNotPresent() correctly locate checkboxes regardless of the case
 * of their {@code type} attribute.
 *
 * <p>HtmlUnit 3.x uses a strict XPath 1.0 evaluator (Apache Xalan). The original
 * XPath expressions used {@code lower-case(@type)} which is an XPath 2.0 function —
 * causing a runtime "Could not find function: lower-case" error. The fix replaced
 * {@code lower-case()} with the XPath 1.0 equivalent:
 * {@code translate(@type,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')}.
 *
 * <p>The existing FormAssertionsTest only uses {@code type="checkbox"} (lowercase).
 * These tests specifically exercise {@code TYPE="CHECKBOX"} and {@code type="CheckBox"}
 * to confirm the translate() expression handles all case variants.</p>
 */
public class HtmlUnitCheckboxCaseInsensitiveTest extends JWebUnitAPITestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setBaseUrl(HOST_PATH + "/CheckboxCaseInsensitiveTest");
        beginAt("/testPage.html");
    }

    /**
     * Baseline: lowercase type="checkbox" must continue to be found (no regression).
     */
    @Test
    public void testAssertCheckboxPresentWithLowercaseType() {
        assertCheckboxPresent("cb1");
    }

    /**
     * Primary regression guard: TYPE="CHECKBOX" (all uppercase) must be found.
     * This was the case that triggered "Could not find function: lower-case" with
     * HtmlUnit 3.x's strict XPath 1.0 engine before the translate() fix.
     */
    @Test
    public void testAssertCheckboxPresentWithUppercaseType() {
        assertCheckboxPresent("cb2");
    }

    /**
     * Mixed case type="CheckBox" must also be found by the translate() expression.
     */
    @Test
    public void testAssertCheckboxPresentWithMixedCaseType() {
        assertCheckboxPresent("cb3");
    }

    /**
     * assertCheckboxNotPresent must not find a checkbox that does not exist,
     * even when other checkboxes on the page have uppercase type attributes.
     */
    @Test
    public void testAssertCheckboxNotPresentWithNonExistentName() {
        assertCheckboxNotPresent("nonExistentCheckbox");
    }

    /**
     * assertCheckboxPresent(name, value) must locate a checkbox with TYPE="CHECKBOX"
     * when both name and value are specified.
     */
    @Test
    public void testAssertCheckboxPresentWithNameAndValueUppercaseType() {
        assertCheckboxPresent("cb4", "val1");
    }
}
