/******************************************************************************
 * jWebUnit project (http://jwebunit.sourceforge.net)                         *
 * Distributed open-source, see full license under LICENCE.txt                *
 ******************************************************************************/
package net.sourceforge.jwebunit.locator;

/**
 * A Html table locator is a way to locate one &lt;table&gt;
 * in an HTML page.
 * 
 * @author Julien Henry
 */
public class HtmlTableLocator extends HtmlElementLocator {

    public HtmlTableLocator() {
        super("table");
    }
    
    public HtmlTableLocator(String id) {
        super("table", id);
    }

}