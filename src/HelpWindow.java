import com.googlecode.lanterna.gui2.*;

class HelpWindow extends ParametrizedWindow {
    private Panel panel;
    public void construct() {
        panel = new Panel(new GridLayout(2));
        addText("Move Window", "Ctrl + <arrow keys>");
        addText("Resize Window", "Ctrl + Alt + <arrow keys>");
        addText("Refresh Window", "F5");
        addText("Switch Windows", "Ctrl + Space or Ctrl + n");
        addText("Close Window", "Escape");

        setComponent(panel);
    }

    private void addText(String a, String b) {
        panel.addComponent(new Label(a), GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.BEGINNING))
            .addComponent(new Label(b), GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));
    }

    public HelpWindow(Tupperware root) {
        super(root, "Help");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof HelpWindow;
    }

    @Override
    public int hashCode() {
        return 7202;
    }
}
