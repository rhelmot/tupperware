import com.googlecode.lanterna.TerminalSize;

public abstract class ParametrizedWindow extends DynamicWindow {
    @Override public abstract boolean equals(Object other);
    @Override public abstract int hashCode();
    public abstract void construct();
    protected Tupperware root;

    public ParametrizedWindow(Tupperware parent, String title) {
        super(title);
        root = parent;
    } 

    public ParametrizedWindow(Tupperware parent, String title, TerminalSize startSize) {
        super(title, startSize);
        root = parent;
    }

    public ParametrizedWindow(Tupperware parent, String title, boolean isManaged, TerminalSize startSize) {
        super(title, isManaged, startSize);
        root = parent;
    }
}
