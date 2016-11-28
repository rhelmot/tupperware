import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

public class ChatLineTextBox extends TextBox {
    public boolean handleLine(String data) {
        return false;
    }

    public boolean handleUp(KeyStroke stroke) { return false; }
    public boolean handleDown(KeyStroke stroke) { return false; }
    public boolean handlePageUp(KeyStroke stroke) { return false; }
    public boolean handlePageDown(KeyStroke stroke) { return false; }

    @Override
    public Interactable.Result handleKeyStroke(KeyStroke stroke) {
        switch (stroke.getKeyType()) {
            case Enter:
                if (handleLine(getText())) {
                    setText("");
                }
                return Interactable.Result.HANDLED;

            case ArrowLeft:
            case ArrowRight:
                if (stroke.isCtrlDown() || stroke.isAltDown()) {
                    return Interactable.Result.UNHANDLED;
                } else {
                    return super.handleKeyStroke(stroke);
                }

            case ArrowUp:
            case ArrowDown:
                if (stroke.isCtrlDown() || stroke.isAltDown()) {
                    return Interactable.Result.UNHANDLED;
                } else {
                    if (stroke.getKeyType() == KeyType.ArrowUp ? handleUp(stroke) : handleDown(stroke)) {
                        return Interactable.Result.HANDLED;
                    } else {
                        return Interactable.Result.UNHANDLED;
                    }
                }

            case PageUp:
            case PageDown:
                if (stroke.isCtrlDown() || stroke.isAltDown()) {
                    return Interactable.Result.UNHANDLED;
                } else {
                    if (stroke.getKeyType() == KeyType.PageUp ? handlePageUp(stroke) : handlePageDown(stroke)) {
                        return Interactable.Result.HANDLED;
                    } else {
                        return Interactable.Result.UNHANDLED;
                    }
                }

            default:
                return super.handleKeyStroke(stroke);
        }
    }
}
