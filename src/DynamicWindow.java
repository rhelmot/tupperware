import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.*;

public class DynamicWindow extends BasicWindow {
    private boolean isManaged;
    private boolean isRoot;

    public DynamicWindow(String title) {
        this(title, true, null);
    }

    public DynamicWindow(String title, TerminalSize startSize) {
        this(title, false, startSize);
    }

    public DynamicWindow(String title, boolean isManaged, TerminalSize startSize) {
        this(title, isManaged, startSize, false);
    }

    public DynamicWindow(String title, boolean isManaged, TerminalSize startSize, boolean isRoot) {
        super(title);

        if (isManaged) {
            setHints(Collections.<Hint> emptyList());
        } else {
            setHints(Collections.singletonList(Hint.FIXED_SIZE));
            setSize(startSize);
        }

        this.isManaged = isManaged;
        this.isRoot = isRoot;
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handled = false;
        //System.err.println(key);
        switch(key.getKeyType()) {
            case ArrowDown:
                if (!isManaged && key.isAltDown() && key.isCtrlDown()) {
                    setSize(getSize().withRelativeRows(1));
                    handled = true;
                } else if (!key.isAltDown() && key.isCtrlDown()) {
                    setPosition(getPosition().withRelativeRow(1));
                    handled = true;
                }
                break;
            case ArrowLeft:
                if (!isManaged && key.isAltDown() && key.isCtrlDown() && getSize().getColumns() > 1) {
                    setSize(getSize().withRelativeColumns(-1));
                    handled = true;
                } else if (!key.isAltDown() && key.isCtrlDown()) {
                    setPosition(getPosition().withRelativeColumn(-1));
                    handled = true;
                }
                break;
            case ArrowRight:
                if (!isManaged && key.isAltDown() && key.isCtrlDown()) {
                    setSize(getSize().withRelativeColumns(1));
                    handled = true;
                } else if (!key.isAltDown() && key.isCtrlDown()) {
                    setPosition(getPosition().withRelativeColumn(1));
                    handled = true;
                }
                break;
            case ArrowUp:
                if (!isManaged && key.isAltDown() && key.isCtrlDown() && getSize().getRows() > 1) {
                    setSize(getSize().withRelativeRows(-1));
                    handled = true;
                } else if (!key.isAltDown() && key.isCtrlDown()) {
                    setPosition(getPosition().withRelativeRow(-1));
                    handled = true;
                }
                break;
            case Escape:
                if (!isRoot) {
                    close();
                    handled = true;
                }
                break;
            case F5:
                if (this instanceof ParametrizedWindow) {
                    ((ParametrizedWindow)this).construct();
                    handled = true;
                }
                break;
            case Character:
                if ((key.getCharacter() == ' ' || key.getCharacter() == 'n') && key.isCtrlDown() && getTextGUI().getWindows().size() > 1) {
                    Collection<Window> windows = getTextGUI().getWindows();
                    ArrayList<Window> windowNames = new ArrayList<Window>(windows);

                    Window picked = Tupperware.staticGenericPicker(getTextGUI(), windowNames, "Pick a window", "how the fuck did you just do that");
                    if (picked != null) {
                        getTextGUI().setActiveWindow(picked);
                    }
                    handled = true;
                }
                break;
            default:
        }
        if(!handled) {
            handled = super.handleInput(key);
        }
        return handled;
    }

    public String toString() {
        if (isRoot) {
            return "Tupperware Menu";
        } else {
            return getTitle();
        }
    }
}
