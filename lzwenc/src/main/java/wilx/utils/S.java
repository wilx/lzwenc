/**
 *
 */
package wilx.utils;

import gnu.gettext.GettextResource;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Václav Haisman
 *
 */
public
class S
{
    public
    static
    String str_(final String s)
    {
        return GettextResource.gettext (catalog, s);
    }

    public
    static
    void
    setBundle (final String bundle)
    {
        setBundle (bundle, Locale.getDefault ());
    }

    public
    static
    void
    setBundle (final String bundle, final Locale l)
    {
        locale = l;
        catalog = ResourceBundle.getBundle (bundle, locale);
    }

    protected static Locale locale;
    protected static ResourceBundle catalog;

    static
    {
        S.setBundle ("lzwenc.lzwenc");
    }
}