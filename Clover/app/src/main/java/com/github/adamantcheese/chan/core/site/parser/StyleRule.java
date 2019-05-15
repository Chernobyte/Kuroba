/*
 * Clover4 - *chan browser https://github.com/Adamantcheese/Clover4/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.adamantcheese.chan.core.site.parser;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import com.github.adamantcheese.chan.core.model.Post;
import com.github.adamantcheese.chan.core.model.PostLinkable;
import com.github.adamantcheese.chan.ui.span.AbsoluteSizeSpanHashed;
import com.github.adamantcheese.chan.ui.span.ForegroundColorSpanHashed;
import com.github.adamantcheese.chan.ui.theme.Theme;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StyleRule {
    public enum Color {
        INLINE_QUOTE,
        QUOTE
    }

    private final List<String> blockElements = Arrays.asList("p", "div");

    public static StyleRule tagRule(String tag) {
        return new StyleRule().tag(tag);
    }

    private String tag;
    private List<String> classes;

    private List<Action> actions = new ArrayList<>();

    private Color color = null;
    private boolean strikeThrough;
    private boolean bold;
    private boolean italic;
    private boolean monospace;
    private int size = 0;

    private PostLinkable.Type link = null;

    private boolean nullify;
    private boolean linkify;

    private String justText = null;

    private boolean blockElement;

    public StyleRule tag(String tag) {
        this.tag = tag;

        if (blockElements.contains(tag)) {
            blockElement = true;
        }

        return this;
    }

    public String tag() {
        return tag;
    }

    public StyleRule cssClass(String cssClass) {
        if (classes == null) {
            classes = new ArrayList<>(4);
        }
        classes.add(cssClass);

        return this;
    }

    public StyleRule action(Action action) {
        actions.add(action);
        return this;
    }

    public StyleRule color(Color color) {
        this.color = color;
        return this;
    }

    public StyleRule link(PostLinkable.Type link) {
        this.link = link;
        return this;
    }

    public StyleRule strikeThrough() {
        strikeThrough = true;
        return this;
    }

    public StyleRule bold() {
        bold = true;
        return this;
    }

    public StyleRule italic() {
        italic = true;
        return this;
    }

    public StyleRule monospace() {
        monospace = true;
        return this;
    }

    public StyleRule size(int size) {
        this.size = size;
        return this;
    }

    public StyleRule nullify() {
        nullify = true;
        return this;
    }

    public StyleRule linkify() {
        linkify = true;
        return this;
    }

    public StyleRule just(String justText) {
        this.justText = justText;
        return this;
    }

    public boolean highPriority() {
        return classes != null && !classes.isEmpty();
    }

    public boolean applies(Element element) {
        if (classes == null || classes.isEmpty()) {
            return true;
        }

        for (String c : classes) {
            if (element.hasClass(c)) {
                return true;
            }
        }

        return false;
    }

    public CharSequence apply(Theme theme,
                              PostParser.Callback callback,
                              Post.Builder post,
                              CharSequence text,
                              Element element) {
        if (nullify) {
            return null;
        }

        if (justText != null) {
            return justText;
        }

        CharSequence result = text;
        for (Action action : actions) {
            result = action.execute(theme, callback, post, text, element);
        }

        List<Object> spansToApply = new ArrayList<>(2);

        if (color != null) {
            spansToApply.add(new ForegroundColorSpanHashed(getColor(theme, color)));
        }

        if (strikeThrough) {
            spansToApply.add(new StrikethroughSpan());
        }

        if (bold && italic) {
            spansToApply.add(new StyleSpan(Typeface.BOLD_ITALIC));
        } else if (bold) {
            spansToApply.add(new StyleSpan(Typeface.BOLD));
        } else if (italic) {
            spansToApply.add(new StyleSpan(Typeface.ITALIC));
        }

        if (monospace) {
            spansToApply.add(new TypefaceSpan("monospace"));
        }

        if (size != 0) {
            spansToApply.add(new AbsoluteSizeSpanHashed(size));
        }

        if (link != null) {
            PostLinkable pl = new PostLinkable(theme, result, result, link);
            post.addLinkable(pl);
            spansToApply.add(pl);
        }

        if (!spansToApply.isEmpty()) {
            result = applySpan(result, spansToApply);
        }

        // Apply break if not the last element.
        if (blockElement && element.nextSibling() != null) {
            result = TextUtils.concat(result, "\n");
        }

        if (linkify) {
            CommentParserHelper.detectLinks(theme, post, result.toString(), new SpannableString(result));
        }

        return result;
    }

    private int getColor(Theme theme, Color color) {
        switch (color) {
            case INLINE_QUOTE:
                return theme.inlineQuoteColor;
            case QUOTE:
                return theme.quoteColor;
        }
        return 0;
    }

    private SpannableString applySpan(CharSequence text, List<Object> spans) {
        SpannableString result = new SpannableString(text);
        int l = result.length();

        for (Object span : spans) {
            if (span != null) {
                result.setSpan(span, 0, l, 0);
            }
        }

        return result;
    }

    public interface Action {
        CharSequence execute(Theme theme,
                             PostParser.Callback callback,
                             Post.Builder post,
                             CharSequence text,
                             Element element);
    }
}
