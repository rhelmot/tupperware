import com.googlecode.lanterna.gui2.*;

import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.input.KeyStroke;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WordWrapTextBox extends AbstractInteractableComponent<WordWrapTextBox> {

    /**
     * Enum value to force a {@code TextBox} to be either single line or multi line. This is usually auto-detected if
     * the text box has some initial content by scanning that content for \n characters.
     */
    public enum Style {
        /**
         * The {@code TextBox} contains a single line of text and is typically drawn on one row
         */
        SINGLE_LINE,
        /**
         * The {@code TextBox} contains a none, one or many lines of text and is normally drawn over multiple lines
         */
        MULTI_LINE,
        ;
    }

    private final List<String> lines;
    private final Style style;

    private boolean caretWarp;
    private boolean horizontalFocusSwitching;
    private boolean verticalFocusSwitching;
    private final int maxLineLength;
    private Character mask;
    private Pattern validationPattern;

    private final List<Runnable> onTopHandlers;

    /**
     * Default constructor, this creates a single-line {@code TextBox} of size 10 which is initially empty
     */
    public WordWrapTextBox() {
        this(new TerminalSize(10, 1), "", Style.SINGLE_LINE);
    }

    /**
     * Constructor that creates a {@code TextBox} with an initial content and attempting to be big enough to display
     * the whole text at once without scrollbars
     * @param initialContent Initial content of the {@code TextBox}
     */
    public WordWrapTextBox(String initialContent) {
        this(null, initialContent, initialContent.contains("\n") ? Style.MULTI_LINE : Style.SINGLE_LINE);
    }

    /**
     * Creates a {@code TextBox} that has an initial content and attempting to be big enough to display the whole text
     * at once without scrollbars.
     *
     * @param initialContent Initial content of the {@code TextBox}
     * @param style Forced style instead of auto-detecting
     */
    public WordWrapTextBox(String initialContent, Style style) {
        this(null, initialContent, style);
    }

    /**
     * Creates a new empty {@code TextBox} with a specific size
     * @param preferredSize Size of the {@code TextBox}
     */
    public WordWrapTextBox(TerminalSize preferredSize) {
        this(preferredSize, (preferredSize != null && preferredSize.getRows() > 1) ? Style.MULTI_LINE : Style.SINGLE_LINE);
    }

    /**
     * Creates a new empty {@code TextBox} with a specific size and style
     * @param preferredSize Size of the {@code TextBox}
     * @param style Style to use
     */
    public WordWrapTextBox(TerminalSize preferredSize, Style style) {
        this(preferredSize, "", style);
    }

    /**
     * Creates a new empty {@code TextBox} with a specific size and initial content
     * @param preferredSize Size of the {@code TextBox}
     * @param initialContent Initial content of the {@code TextBox}
     */
    public WordWrapTextBox(TerminalSize preferredSize, String initialContent) {
        this(preferredSize, initialContent, (preferredSize != null && preferredSize.getRows() > 1) || initialContent.contains("\n") ? Style.MULTI_LINE : Style.SINGLE_LINE);
    }

    /**
     * Main constructor of the {@code TextBox} which decides size, initial content and style
     * @param preferredSize Size of the {@code TextBox}
     * @param initialContent Initial content of the {@code TextBox}
     * @param style Style to use for this {@code TextBox}, instead of auto-detecting
     */
    public WordWrapTextBox(TerminalSize preferredSize, String initialContent, Style style) {
        this.lines = new ArrayList<String>();
        this.style = style;
        this.caretWarp = false;
        this.verticalFocusSwitching = true;
        this.horizontalFocusSwitching = (style == Style.SINGLE_LINE);
        this.maxLineLength = -1;
        this.mask = null;
        this.validationPattern = null;
        this.onTopHandlers = new ArrayList<Runnable>();
        setText(initialContent);

        setPreferredSize(preferredSize);
    }

    /**
     * Sets a pattern on which the content of the text box is to be validated. For multi-line TextBox:s, the pattern is
     * checked against each line individually, not the content as a whole. Partial matchings will not be allowed, the
     * whole pattern must match, however, empty lines will always be allowed. When the user tried to modify the content
     * of the TextBox in a way that does not match the pattern, the operation will be silently ignored. If you set this
     * pattern to {@code null}, all validation is turned off.
     * @param validationPattern Pattern to validate the lines in this TextBox against, or {@code null} to disable
     * @return itself
     */
    public synchronized WordWrapTextBox setValidationPattern(Pattern validationPattern) {
        if(validationPattern != null) {
            for(String line: lines) {
                if(!validated(line)) {
                    throw new IllegalStateException("TextBox validation pattern " + validationPattern + " does not match existing content");
                }
            }
        }
        this.validationPattern = validationPattern;
        return this;
    }

    /**
     * Updates the text content of the {@code TextBox} to the supplied string.
     * @param text New text to assign to the {@code TextBox}
     * @return Itself
     */
    public synchronized WordWrapTextBox setText(String text) {
        String[] split = text.split("\n");
        lines.clear();
        for(String line : split) {
            addLine(line);
        }
        invalidate();
        return this;
    }

    @Override
    public WordWrapTextBoxRenderer getRenderer() {
        return (WordWrapTextBoxRenderer)super.getRenderer();
    }

    /**
     * Adds a single line to the {@code TextBox} at the end, this only works when in multi-line mode
     * @param line Line to add at the end of the content in this {@code TextBox}
     * @return Itself
     */
    public synchronized WordWrapTextBox addLine(String line) {
        return addLine(line, lines.size());
    }

    public synchronized WordWrapTextBox addLine(String line, int index) {
        StringBuilder bob = new StringBuilder();
        for(int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if(c == '\n' && style == Style.MULTI_LINE) {
                String string = bob.toString();
                lines.add(index, string);
                addLine(line.substring(i + 1), index + 1);
                return this;
            }
            else if(Character.isISOControl(c)) {
                continue;
            }

            bob.append(c);
        }
        String string = bob.toString();
        if(!validated(string)) {
            throw new IllegalStateException("TextBox validation pattern " + validationPattern + " does not match the supplied text");
        }
        lines.add(index, string);
        invalidate();
        return this;
    }

    public synchronized WordWrapTextBox setLine(String line, int index) {
        lines.set(index, line);
        return this;
    }

    public synchronized WordWrapTextBox removeLine(int index) {
        lines.remove(index);
        return this;
    }

    /**
     * Returns the text in this {@code TextBox}, for multi-line mode all lines will be concatenated together with \n as
     * separator.
     * @return The text inside this {@code TextBox}
     */
    public synchronized String getText() {
        StringBuilder bob = new StringBuilder(lines.get(0));
        for(int i = 1; i < lines.size(); i++) {
            bob.append("\n").append(lines.get(i));
        }
        return bob.toString();
    }

    /**
     * Helper method, it will return the content of the {@code TextBox} unless it's empty in which case it will return
     * the supplied default value
     * @param defaultValueIfEmpty Value to return if the {@code TextBox} is empty
     * @return Text in the {@code TextBox} or {@code defaultValueIfEmpty} is the {@code TextBox} is empty
     */
    public String getTextOrDefault(String defaultValueIfEmpty) {
        String text = getText();
        if(text.isEmpty()) {
            return defaultValueIfEmpty;
        }
        return text;
    }

    /**
     * Returns the current text mask, meaning the substitute to draw instead of the text inside the {@code TextBox}.
     * This is normally used for password input fields so the password isn't shown
     * @return Current text mask or {@code null} if there is no mask
     */
    public Character getMask() {
        return mask;
    }

    /**
     * Sets the current text mask, meaning the substitute to draw instead of the text inside the {@code TextBox}.
     * This is normally used for password input fields so the password isn't shown
     * @param mask New text mask or {@code null} if there is no mask
     * @return Itself
     */
    public WordWrapTextBox setMask(Character mask) {
        if(mask != null && TerminalTextUtils.isCharCJK(mask)) {
            throw new IllegalArgumentException("Cannot use a CJK character as a mask");
        }
        this.mask = mask;
        invalidate();
        return this;
    }

    /**
     * Returns {@code true} if this {@code TextBox} is in read-only mode, meaning text input from the user through the
     * keyboard is prevented
     * @return {@code true} if this {@code TextBox} is in read-only mode
     */
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Sets the read-only mode of the {@code TextBox}, meaning text input from the user through the keyboard is
     * prevented. The user can still focus and scroll through the text in this mode.
     * @param readOnly If {@code true} then the {@code TextBox} will switch to read-only mode
     * @return Itself
     */
    public WordWrapTextBox setReadOnly(boolean readOnly) {
        return this;
    }

    /**
     * If {@code true}, the component will switch to the next available component above if the cursor is at the top of
     * the TextBox and the user presses the 'up' array key, or switch to the next available component below if the
     * cursor is at the bottom of the TextBox and the user presses the 'down' array key. The means that for single-line
     * TextBox:es, pressing up and down will always switch focus.
     * @return {@code true} if vertical focus switching is enabled
     */
    public boolean isVerticalFocusSwitching() {
        return verticalFocusSwitching;
    }

    /**
     * If set to {@code true}, the component will switch to the next available component above if the cursor is at the
     * top of the TextBox and the user presses the 'up' array key, or switch to the next available component below if
     * the cursor is at the bottom of the TextBox and the user presses the 'down' array key. The means that for
     * single-line TextBox:es, pressing up and down will always switch focus with this mode enabled.
     * @param verticalFocusSwitching If called with true, vertical focus switching will be enabled
     * @return Itself
     */
    public WordWrapTextBox setVerticalFocusSwitching(boolean verticalFocusSwitching) {
        this.verticalFocusSwitching = verticalFocusSwitching;
        return this;
    }

    /**
     * If {@code true}, the TextBox will switch focus to the next available component to the left if the cursor in the
     * TextBox is at the left-most position (index 0) on the row and the user pressed the 'left' arrow key, or vice
     * versa for pressing the 'right' arrow key when the cursor in at the right-most position of the current row.
     * @return {@code true} if horizontal focus switching is enabled
     */
    public boolean isHorizontalFocusSwitching() {
        return horizontalFocusSwitching;
    }

    /**
     * If set to {@code true}, the TextBox will switch focus to the next available component to the left if the cursor
     * in the TextBox is at the left-most position (index 0) on the row and the user pressed the 'left' arrow key, or
     * vice versa for pressing the 'right' arrow key when the cursor in at the right-most position of the current row.
     * @param horizontalFocusSwitching If called with true, horizontal focus switching will be enabled
     * @return Itself
     */
    public WordWrapTextBox setHorizontalFocusSwitching(boolean horizontalFocusSwitching) {
        this.horizontalFocusSwitching = horizontalFocusSwitching;
        return this;
    }

    /**
     * Returns the line on the specific row. For non-multiline TextBox:es, calling this with index set to 0 will return
     * the same as calling {@code getText()}. If the row index is invalid (less than zero or equals or larger than the
     * number of rows), this method will throw IndexOutOfBoundsException.
     * @param index Index of the row to return the contents from
     * @return The line at the specified index, as a String
     * @throws IndexOutOfBoundsException if the row index is less than zero or too large
     */
    public synchronized String getLine(int index) {
        return lines.get(index);
    }

    /**
     * Returns the number of lines currently in this TextBox. For single-line TextBox:es, this will always return 1.
     * @return Number of lines of text currently in this TextBox
     */
    public synchronized int getLineCount() {
        return lines.size();
    }

    @Override
    protected WordWrapTextBoxRenderer createDefaultRenderer() {
        return new DefaultWordWrapTextBoxRenderer();
    }

    private boolean validated(String line) {
        return validationPattern == null || line.isEmpty() || validationPattern.matcher(line).matches();
    }

    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case ArrowLeft:
                if(horizontalFocusSwitching) {
                    return Result.MOVE_FOCUS_LEFT;
                }
                return Result.UNHANDLED;
            case ArrowRight:
                if(horizontalFocusSwitching) {
                    return Result.MOVE_FOCUS_RIGHT;
                }
                return Result.UNHANDLED;
            case ArrowUp:
                if(getRenderer().isAtTop(this) && verticalFocusSwitching) {
                    return Result.MOVE_FOCUS_UP;
                }
                getRenderer().moveUp(this);
                fireOnTop();
                return Result.HANDLED;
            case ArrowDown:
                if(getRenderer().isAtBottom(this) && verticalFocusSwitching) {
                    return Result.MOVE_FOCUS_DOWN;
                }
                getRenderer().moveDown(this);
                return Result.HANDLED;
            case Home:
                getRenderer().moveToTop(this);
                fireOnTop();
                return Result.HANDLED;
            case End:
                getRenderer().moveToBottom(this);
                return Result.HANDLED;
            case PageDown:
                getRenderer().movePageDown(this);
                return Result.HANDLED;
            case PageUp:
                getRenderer().movePageUp(this);
                fireOnTop();
                return Result.HANDLED;
            default:
        }
        return super.handleKeyStroke(keyStroke);
    }

    public void moveToTop() {
        getRenderer().moveToTop(this);
    }

    public void moveToBottom() {
        getRenderer().moveToBottom(this);
    }

    private boolean pendingMoveToBottom;
    public void pendingMoveToBottom() {
        this.pendingMoveToBottom = true;
    }

    @Override
    public void onBeforeDrawing() {
        super.onBeforeDrawing();
        if (pendingMoveToBottom) {
            moveToBottom();
            pendingMoveToBottom = false;
        }
    }

    public void moveToLine(int line) {
        getRenderer().moveToLine(line);
    }

    public int totalLineHeight() {
        return getWrappedLines().size();
    }

    public List<String> getWrappedLines() {
        return TerminalTextUtils.getWordWrappedText(getSize().getColumns() - 1, lines.toArray(new String[lines.size()]));
    }

    public WordWrapTextBox addTopListener(Runnable r) {
        if (r != null && !onTopHandlers.contains(r)) {
            onTopHandlers.add(r);
        }
        return this;
    }

    public WordWrapTextBox removeTopListener(Runnable r) {
        onTopHandlers.remove(r);
        return this;
    }

    private void fireOnTop() {
        if (getRenderer().isAtTop(this)) {
            for (Runnable r : onTopHandlers) {
                r.run();
            }
        }
    }

    /**
     * Helper interface that doesn't add any new methods but makes coding new text box renderers a little bit more clear
     */
    public interface WordWrapTextBoxRenderer extends InteractableRenderer<WordWrapTextBox> {
        public int getViewTopLine();
        public void setViewTopLine(int lineNo);
        public int getViewTopLineOffset();
        public void setViewTopLineOffset(int lineOffset);

        public boolean isAtTop(WordWrapTextBox component);
        public boolean isAtBottom(WordWrapTextBox component);

        public void moveToLine(int line);
        public void moveToTop(WordWrapTextBox component);
        public void moveToBottom(WordWrapTextBox component);
        public void moveUp(WordWrapTextBox component);
        public void moveDown(WordWrapTextBox component);
        public void movePageUp(WordWrapTextBox component);
        public void movePageDown(WordWrapTextBox component);
    }

    public static class DefaultWordWrapTextBoxRenderer implements WordWrapTextBoxRenderer {
        protected int viewTopLine;
        protected int viewTopLineOffset;
        protected final ScrollBar verticalScrollBar;
        protected boolean hideScrollBars;
        protected Character unusedSpaceCharacter;

        public DefaultWordWrapTextBoxRenderer() {
            verticalScrollBar = new ScrollBar(Direction.VERTICAL);
            hideScrollBars = false;
            unusedSpaceCharacter = null;

            viewTopLine = 0;
            viewTopLineOffset = 0;
        }

        public int getViewTopLine() {
            return viewTopLine;
        }

        public void setViewTopLine(int l) {
            viewTopLine = l;
        }

        public int getViewTopLineOffset() {
            return viewTopLineOffset;
        }

        public void setViewTopLineOffset(int l) {
            viewTopLineOffset = l;
        }

        private int heightOfLine(WordWrapTextBox component, int line) {
            return heightOfLine(component.getLine(line), component.getSize().getColumns());
        }

        private int heightOfLine(String line, int width) {
            return TerminalTextUtils.getWordWrappedText(width, line).size();
        }

        public void moveToLine(int line) {
            viewTopLine = line;
            viewTopLineOffset = 0;
        }

        public boolean isAtTop(WordWrapTextBox component) {
            return viewTopLine == 0 && viewTopLineOffset == 0;
        }

        // algorithm: at each step, keep track of the current line and the amount of
        // space between its top and the bottom of the view
        // return true if we exhaust all the lines and have not gone beyond the bottom
        // of the view (we pretend there is an imaginary "final line" with index lines.size(),
        // so its top has the same position as the bottom of the real last line)
        public boolean isAtBottom(WordWrapTextBox component) {
            int remaining = component.getSize().getRows() + viewTopLineOffset;
            int curLine = viewTopLine;
            while (remaining > 0 && curLine < component.lines.size()) {
                remaining -= heightOfLine(component, curLine);
                curLine++;
            }
            return curLine == component.lines.size() && remaining >= 0;
        }

        public void moveToTop(WordWrapTextBox component) {
            viewTopLine = 0;
            viewTopLineOffset = 0;
        }

        // Algorithm: move up from the bottom, keeping track of at each step what the current
        // line is and how tall the stack of lines we have accumulated is.
        // at the end, we chop n rows off the current line and that becomes the top of the view,
        // where n is the difference between the stack we have accumulated and the height of the
        // view. edge case, if we have not accumulated a stack taller than the view, cut at
        // offset 0 to handle the case that the whole of the lines fit in the view
        public void moveToBottom(WordWrapTextBox component) {
            int curLine = component.lines.size();
            int curHeight = 0;
            while (curHeight < component.getSize().getRows() && curLine > 0) {
                curLine--;
                curHeight += heightOfLine(component, curLine);
            }

            if (curHeight > component.getSize().getRows()) {
                viewTopLine = curLine;
                viewTopLineOffset = curHeight - component.getSize().getRows();
            } else {
                viewTopLine = curLine;
                viewTopLineOffset = 0;
            }
        }

        public void moveUp(WordWrapTextBox component) {
            if (isAtTop(component)) {
                return;
            }

            if (viewTopLineOffset > 0) {
                viewTopLineOffset--;
            } else if (viewTopLine > 0) {
                viewTopLine--;
                viewTopLineOffset = heightOfLine(component, viewTopLine) - 1;
            }
        }

        public void moveDown(WordWrapTextBox component) {
            if (isAtBottom(component)) {
                return;
            }

            if (viewTopLineOffset + 1 < heightOfLine(component, viewTopLine)) {
                viewTopLineOffset++;
            } else if (viewTopLine + 1 < component.lines.size()) {
                viewTopLineOffset = 0;
                viewTopLine++;
            }
        }

        public void movePageUp(WordWrapTextBox component) {
            // lazy!!!!!!!!
            for (int i = 0; i < component.getSize().getRows(); i++) {
                moveUp(component);
            }
        }

        public void movePageDown(WordWrapTextBox component) {
            // lazy!!!!!!!!
            for (int i = 0; i < component.getSize().getRows(); i++) {
                moveDown(component);
            }
        }

        public void setUnusedSpaceCharacter(char unusedSpaceCharacter) {
            if(TerminalTextUtils.isCharDoubleWidth(unusedSpaceCharacter)) {
                throw new IllegalArgumentException("Cannot use a double-width character as the unused space character in a TextBox");
            }
            this.unusedSpaceCharacter = unusedSpaceCharacter;
        }

        @Override
        public TerminalPosition getCursorLocation(WordWrapTextBox component) {
            return null;
        }

        @Override
        public TerminalSize getPreferredSize(WordWrapTextBox component) {
            int max = 1;
            for (String line : component.lines) {
                max = line.length() > max ? line.length() : max;
            }
            TerminalSize out = new TerminalSize(max + 1, component.totalLineHeight());
            return out;
        }

        public void setHideScrollBars(boolean hideScrollBars) {
            this.hideScrollBars = hideScrollBars;
        }

        @Override
        public void drawComponent(TextGUIGraphics graphics, WordWrapTextBox component) {
            TerminalSize realTextArea = graphics.getSize();
            if(realTextArea.getRows() == 0 || realTextArea.getColumns() == 0) {
                return;
            }

            realTextArea = realTextArea.withRelativeColumns(-1);
            boolean drawVerticalScrollBar = !(isAtTop(component) && isAtBottom(component));

            drawTextArea(graphics.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, realTextArea), component);

            //Draw scrollbars, if any
            if(drawVerticalScrollBar) {
                verticalScrollBar.onAdded(component.getParent());
                verticalScrollBar.setViewSize(realTextArea.getRows());
                verticalScrollBar.setScrollMaximum(component.lines.size());
                verticalScrollBar.setScrollPosition(viewTopLine);
                verticalScrollBar.draw(graphics.newTextGraphics(
                        new TerminalPosition(graphics.getSize().getColumns() - 1, 0),
                        new TerminalSize(1, graphics.getSize().getRows())));
            }
        }

        private void drawTextArea(TextGUIGraphics graphics, WordWrapTextBox component) {
            TerminalSize textAreaSize = graphics.getSize();
            ThemeDefinition themeDefinition = component.getThemeDefinition();
            graphics.applyThemeStyle(themeDefinition.getNormal());

            Character fillCharacter = unusedSpaceCharacter;
            if(fillCharacter == null) {
                fillCharacter = themeDefinition.getCharacter("FILL", ' ');
            }
            graphics.fill(fillCharacter);

            int lineNo = viewTopLine;
            int offset = viewTopLineOffset;
            List<String> lines = TerminalTextUtils.getWordWrappedText(textAreaSize.getColumns(), component.getLine(lineNo));
            for (int row = 0; row < textAreaSize.getRows(); row++) {
                if (offset >= lines.size()) {
                    offset = 0;
                    lineNo++;
                    if (lineNo >= component.lines.size()) {
                        break;
                    }
                    lines = TerminalTextUtils.getWordWrappedText(textAreaSize.getColumns(), component.getLine(lineNo));
                }

                String line = lines.get(offset);
                if(component.getMask() != null) {
                    StringBuilder builder = new StringBuilder();
                    for(int i = 0; i < line.length(); i++) {
                        builder.append(component.getMask());
                    }
                    line = builder.toString();
                }
                graphics.putString(0, row, line);
                offset++;
            }
        }
    }
}
