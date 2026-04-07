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

import net.sourceforge.jwebunit.api.IElement;
import net.sourceforge.jwebunit.tests.JWebUnitAPITestCase;
import org.junit.Test;

import static net.sourceforge.jwebunit.junit.JWebUnit.*;
import static org.junit.Assert.*;

/**
 * Integration tests that verify the HtmlUnit 3.x value-semantics fixes applied in the migration:
 *
 * <ul>
 *   <li>HtmlInput.setValue() vs setValueAttribute() — live DOM value vs HTML attribute</li>
 *   <li>HtmlInput.getValue() vs getValueAttribute() — reading live DOM value after JS mutation</li>
 *   <li>HtmlUnitElementImpl.setAttribute("value") — fires onChange and updates live DOM value</li>
 *   <li>HtmlUnitElementImpl.getAttribute("value") — reads live DOM value, not stale HTML attribute</li>
 * </ul>
 *
 * <p>These tests directly target the three test failures that were fixed during migration:
 * testSubmissionReset (FormSubmissionTest), testGreenLink (JavaScriptEventsTest),
 * and testAttributeJavascript (IElementTest).</p>
 */
public class HtmlUnitValueSemanticsTest extends JWebUnitAPITestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setBaseUrl(HOST_PATH + "/ValueSemanticsTest");
    }

    /**
     * Verifies that setTextField() uses HtmlInput.setValue() (live DOM only) rather than
     * setValueAttribute() (HTML attribute). When setValue() is used, the HTML attribute that
     * form.reset() reads as the default remains "originalDefault", so clicking Reset correctly
     * reverts the field — it does NOT reset to the value supplied to setTextField().
     */
    @Test
    public void testSetTextFieldDoesNotMutateResetDefault() {
        beginAt("/form-reset.html");

        assertTextFieldEquals("field", "originalDefault");

        setTextField("field", "newVal");
        assertTextFieldEquals("field", "newVal");

        reset();

        // reset() must revert to the original HTML default, not to the value set by setTextField()
        assertTextFieldEquals("field", "originalDefault");
    }

    /**
     * Verifies that getTextFieldValue() uses HtmlInput.getValue() (live DOM value) rather than
     * getValueAttribute() (HTML attribute). After a JavaScript onclick handler changes
     * field.value = "jsUpdated", getTextFieldValue() must return "jsUpdated" — not the stale
     * HTML attribute "initial".
     */
    @Test
    public void testGetTextFieldValueReadsLiveDomValueAfterJavaScript() {
        beginAt("/js-live-value.html");

        assertTextFieldEquals("field", "initial");

        clickElementByXPath("//input[@id='trigger']");

        // getTextFieldValue() should read the live DOM value updated by JavaScript
        assertTextFieldEquals("field", "jsUpdated");
    }

    /**
     * Verifies that IElement.setAttribute("value", ...) uses HtmlInput.setValue() so that
     * the live DOM value (this.value) is updated and the onChange handler can propagate it.
     * If setValueAttribute() were used instead, this.value would remain "initial" after
     * setAttribute, and the onChange handler would copy the wrong value to js2.
     */
    @Test
    public void testIElementSetAttributeTriggersOnChange() {
        beginAt("/onchange-propagation.html");

        IElement js1 = getElementById("js1");
        IElement js2 = getElementById("js2");

        assertEquals("initial", js1.getAttribute("value"));
        assertEquals("unchanged", js2.getAttribute("value"));

        clickElementByXPath("//input[@id='js1']");
        js1.setAttribute("value", "typed");
        clickElementByXPath("//input[@id='js2']"); // moving focus triggers onChange on js1

        js1 = getElementById("js1");
        js2 = getElementById("js2");

        assertEquals("typed", js1.getAttribute("value"));
        // onChange handler must propagate the live DOM value set by IElement.setAttribute()
        assertEquals("typed", js2.getAttribute("value"));
    }

    /**
     * Verifies that IElement.getAttribute("value") uses HtmlInput.getValue() (live DOM value)
     * rather than getValueAttribute() (HTML attribute). After a JavaScript onclick handler
     * changes field.value = "jsUpdated" directly on the DOM, getAttribute("value") must
     * return "jsUpdated" — not the original HTML attribute "initial".
     */
    @Test
    public void testIElementGetAttributeReadsLiveDomValue() {
        beginAt("/js-live-value.html");

        IElement field = getElementByXPath("//input[@id='field']");
        assertEquals("initial", field.getAttribute("value"));

        clickElementByXPath("//input[@id='trigger']");

        // IElement.getAttribute("value") should read the live DOM value updated by JavaScript
        field = getElementByXPath("//input[@id='field']");
        assertEquals("jsUpdated", field.getAttribute("value"));
    }
}
