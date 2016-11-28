import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.input.KeyStroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ScrollingPanel extends AbstractComponent<ScrollingPanel> implements Container {
    private final List<Component> components;
    private TerminalSize cachedPreferredSize;
    private final ScrollBar verticalScrollBar;

    private int viewTopComponent;
    private int viewTopComponentOffset;

    public ScrollingPanel() {
        cachedPreferredSize = null;
        components = new ArrayList<Component>();
        viewTopComponent = 0;
        viewTopComponentOffset = 0;
        verticalScrollBar = new ScrollBar(Direction.VERTICAL);
    }

    public ScrollingPanel addComponent(Component component) {
        if(component == null) {
            throw new IllegalArgumentException("Cannot add null component");
        }
        synchronized(components) {
            if(components.contains(component)) {
                return this;
            }
            if(component.getParent() != null) {
                component.getParent().removeComponent(component);
            }
            components.add(component);
        }
        component.onAdded(this);
        invalidate();
        return this;
    }

    @Override
    public boolean containsComponent(Component component) {
        return component != null && component.hasParent(this);
    }

    @Override
    public boolean removeComponent(Component component) {
        if(component == null) {
            throw new IllegalArgumentException("Cannot remove null component");
        }
        synchronized(components) {
            int index = components.indexOf(component);
            if(index == -1) {
                return false;
            }
            if(getBasePane() != null && getBasePane().getFocusedInteractable() == component) {
                getBasePane().setFocusedInteractable(null);
            }
            components.remove(index);
        }
        component.onRemoved(this);
        invalidate();
        return true;
    }

    public ScrollingPanel removeAllComponents() {
        synchronized(components) {
            for(Component component : new ArrayList<Component>(components)) {
                removeComponent(component);
            }
        }
        return this;
    }

    @Override
    public int getChildCount() {
        synchronized(components) {
            return components.size();
        }
    }

    @Override
    public Collection<Component> getChildren() {
        synchronized(components) {
            return new ArrayList<Component>(components);
        }
    }

    @Override
    protected ComponentRenderer<ScrollingPanel> createDefaultRenderer() {
        return new ComponentRenderer<ScrollingPanel>() {

            @Override
            public TerminalSize getPreferredSize(ScrollingPanel component) {
                int maxWidth = 0;
                int height = 0;
                synchronized(components) {
                    for(Component subcomponent: component.components) {
                        TerminalSize preferredSize = subcomponent.getPreferredSize();
                        if(maxWidth < preferredSize.getColumns()) {
                            maxWidth = preferredSize.getColumns();
                        }
                        height += preferredSize.getRows();
                    }
                }

                component.cachedPreferredSize = new TerminalSize(maxWidth, Math.max(0, height));
                return cachedPreferredSize;
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, ScrollingPanel component) {
                if(isInvalid()) {
                    layout(graphics.getSize());
                }

                // Reset the area
                graphics.applyThemeStyle(getThemeDefinition().getNormal());
                graphics.fill(' ');

                synchronized(components) {
                    Component child;
                    for(int i = viewTopComponent, drawnTo = -viewTopComponentOffset; drawnTo < getSize().getRows() && i < components.size(); i++, drawnTo += child.getSize().getRows()) {
                        child = components.get(i);
                        TextGUIGraphics componentGraphics = graphics.newTextGraphics(new TerminalPosition(0, drawnTo), child.getSize());
                        child.draw(componentGraphics);
                    }
                }

                boolean drawVerticalScrollBar = !(isAtTop() && isAtBottom());

                //Draw scrollbars, if any
                if(drawVerticalScrollBar) {
                    verticalScrollBar.onAdded(component.getParent());
                    verticalScrollBar.setViewSize(graphics.getSize().getRows());
                    verticalScrollBar.setScrollMaximum(component.components.size());
                    verticalScrollBar.setScrollPosition(component.viewTopComponent);
                    verticalScrollBar.draw(graphics.newTextGraphics(
                            new TerminalPosition(graphics.getSize().getColumns() - 1, 0),
                            new TerminalSize(1, graphics.getSize().getRows())));
                }
            }
        };
    }

    @Override
    public TerminalSize calculatePreferredSize() {
        if(cachedPreferredSize != null && !isInvalid()) {
            return cachedPreferredSize;
        }
        return super.calculatePreferredSize();
    }

    @Override
    public boolean isInvalid() {
        synchronized(components) {
            for (int i = viewTopComponent; i < components.size() && isInView(i, false); i++) {
                if(components.get(i).isInvalid()) {
                    return true;
                }
            }
        }
        return super.isInvalid();
    }    

    // Algorithm: first check that the given component is not already in view
    // if it is not, it is either off the top or off the bottom.
    // we can tell if it's off the top because its index is less than or equal to the current
    // top component. otherwise, it's off the bottom. If it is off the top, move the view to
    // the top of this component. If it is off the bottom, perform the "moveToBottom"
    // algorithm, basically
    private void scrollIntoView(Component thisOne) {
        int index = components.indexOf(thisOne);
        if (index != -1 && !isInView(index, true)) {
            BasicWindow owner = (BasicWindow)getBasePane();
            if (index <= viewTopComponent) {
                viewTopComponent = index;
                viewTopComponentOffset = 0;
            } else {
                int curHeight = heightOfComponent(index);
                while (curHeight < getSize().getRows() && index > 0) {
                    index--;
                    curHeight += heightOfComponent(index);
                }

                if (curHeight > getSize().getRows()) {
                    viewTopComponent = index;
                    viewTopComponentOffset = curHeight - getSize().getRows();
                } else {
                    // I'm about 80% sure this case should be impossible
                    viewTopComponent = index;
                    viewTopComponentOffset = 0;
                }
            }
        }
    }

    private int heightOfComponent(int index) {
        return components.get(index).getSize().getRows();
    }

    // `entirely` controls the behavior of this method - when set, only return true if the entire
    // component is visible
    //
    // Algorithm: at each step, track the current component and the offset of its top relative
    // to the top of the view. If at any step (before the current top has surpassed the boundaries
    // of the view) we find that the current component is the target component, it at least clips
    // the view. If we require that the component be entirely in view, we can ensure that by
    // checking that neither the top nor the bottom of the current component reach outside the
    // view.
    private boolean isInView(int index, boolean entirely) {
        if (index < viewTopComponent) {
            return false;
        }

        int curComponent = viewTopComponent;
        int curRow = -viewTopComponentOffset;
        while (curComponent < components.size() && curRow < getSize().getRows()) {
            if (curComponent == index) {
                if (entirely) {
                    if (curRow < 0) {
                        return false;
                    } else if (curRow + heightOfComponent(curComponent) > getSize().getRows()) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            curRow += heightOfComponent(curComponent);
            curComponent++;
        }
        return false;
    }

    public void scrollToTop() {
        viewTopComponent = 0;
        viewTopComponentOffset = 0;
    }

    public void scrollToBottom() {
        int curComponent = components.size();
        int curHeight = 0;
        while (curHeight < getSize().getRows() && curComponent > 0) {
            curComponent--;
            curHeight += heightOfComponent(curComponent);
        }

        if (curHeight > getSize().getRows()) {
            viewTopComponent = curComponent;
            viewTopComponentOffset = curHeight - getSize().getRows();
        } else {
            viewTopComponent = curComponent;
            viewTopComponentOffset = 0;
        }
    }

    public boolean isAtTop() {
        return viewTopComponent == 0 && viewTopComponentOffset == 0;
    }

    public boolean isAtBottom() {
        int remaining = getSize().getRows() + viewTopComponentOffset;
        int curComponent = viewTopComponent;
        while (remaining > 0 && curComponent < components.size()) {
            remaining -= heightOfComponent(curComponent);
            curComponent++;
        }
        return curComponent == components.size() && remaining >= 0;
    }

    @Override
    public Interactable nextFocus(Interactable fromThis) {
        boolean chooseNextAvailable = (fromThis == null);

        synchronized(components) {
            for(Component component : components) {
                if(chooseNextAvailable) {
                    if(component instanceof Interactable && ((Interactable) component).isEnabled() && ((Interactable) component).isFocusable()) {
                        scrollIntoView(component);
                        return (Interactable) component;
                    }
                    else if(component instanceof Container) {
                        Interactable firstInteractable = ((Container) (component)).nextFocus(null);
                        if(firstInteractable != null) {
                            scrollIntoView(component);
                            return firstInteractable;
                        }
                    }
                    continue;
                }

                if(component == fromThis) {
                    chooseNextAvailable = true;
                    continue;
                }

                if(component instanceof Container) {
                    Container container = (Container) component;
                    if(fromThis.isInside(container)) {
                        Interactable next = container.nextFocus(fromThis);
                        if(next == null) {
                            chooseNextAvailable = true;
                        }
                        else {
                            scrollIntoView(component);
                            return next;
                        }
                    }
                }
            }
        }
        scrollToBottom();
        return null;
    }

    @Override
    public Interactable previousFocus(Interactable fromThis) {
        boolean chooseNextAvailable = (fromThis == null);

        List<Component> revComponents = new ArrayList<Component>();
        synchronized(components) {
            revComponents.addAll(components);
        }
        Collections.reverse(revComponents);

        for (Component component : revComponents) {
            if (chooseNextAvailable) {
                if (component instanceof Interactable && ((Interactable)component).isEnabled() && ((Interactable)component).isFocusable()) {
                    scrollIntoView(component);
                    return (Interactable) component;
                }
                if (component instanceof Container) {
                    Interactable lastInteractable = ((Container)(component)).previousFocus(null);
                    if (lastInteractable != null) {
                        scrollIntoView(component);
                        return lastInteractable;
                    }
                }
                continue;
            }

            if (component == fromThis) {
                chooseNextAvailable = true;
                continue;
            }

            if (component instanceof Container) {
				Container container = (Container) component;
				if(fromThis.isInside(container)) {
					Interactable next = container.nextFocus(fromThis);
					if(next == null) {
						chooseNextAvailable = true;
					}
					else {
                        scrollIntoView(component);
						return next;
					}
				}
			}
        }
        scrollToTop();
        return null;
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        return false;
    }
    
    @Override
    public void updateLookupMap(InteractableLookupMap interactableLookupMap) {
        // decline to do any of this shit
        // force the calling of the nextComponent and previousComponent methods
        /*
        synchronized(components) {
            for(Component component: components) {
                if(component instanceof Container) {
                    ((Container)component).updateLookupMap(interactableLookupMap);
                }
                else if(component instanceof Interactable && ((Interactable)component).isEnabled() && ((Interactable)component).isFocusable()) {
                    interactableLookupMap.add((Interactable)component);
                }
            }
        }
        */
    }

    @Override
    public void invalidate() {
        super.invalidate();

        synchronized(components) {
            //Propagate
            for(Component component: components) {
                component.invalidate();
            }
        }
    }

    private void layout(TerminalSize size) {
        synchronized(components) {
            int availableHorizontalSpace = size.getColumns() - 1;
            int availableVerticalSpace = size.getRows();
            int currentVerticalPosition = 0;
            for(Component component: components) {
                Alignment alignment = Alignment.Beginning;
                //LayoutData layoutData = component.getLayoutData();
                //if (layoutData instanceof LinearLayoutData) {
                //    alignment = ((LinearLayoutData)layoutData).alignment;
                //}

                component.setSize(new TerminalSize(availableHorizontalSpace, 1));
                TerminalSize preferredSize = component.getPreferredSize();

                // this is sort of a hack... the scrolling will do some very shady things when
                // presented with elements that are larger than the available vertical space
                TerminalSize decidedSize = new TerminalSize(
                        Math.min(availableHorizontalSpace, preferredSize.getColumns()),
                        Math.min(availableVerticalSpace, preferredSize.getRows()));

                if(alignment == Alignment.Fill) {
                    decidedSize = decidedSize.withColumns(availableHorizontalSpace);
                    alignment = Alignment.Beginning;
                }

                TerminalPosition position = component.getPosition();
                position = position.withRow(currentVerticalPosition);
                switch(alignment) {
                    case End:
                        position = position.withColumn(availableHorizontalSpace - decidedSize.getColumns());
                        break;
                    case Center:
                        position = position.withColumn((availableHorizontalSpace - decidedSize.getColumns()) / 2);
                        break;
                    case Beginning:
                    default:
                        position = position.withColumn(0);
                        break;
                }
                component.setPosition(position);
                component.setSize(decidedSize);
                currentVerticalPosition += decidedSize.getRows();
            }
        }
    }

    @Override
    public TerminalPosition toBasePane(TerminalPosition position) {
        int viewTop = 0;
        for (int i = 0; i < viewTopComponent; i++) {
            viewTop += heightOfComponent(i);
        }
        viewTop += viewTopComponentOffset;
        position = position.withRelativeRow(-viewTop);

        // ISSUE: there should be some bounds checking here but if we return null from this function
        // under any circumstances, bad things happen. where should the bound checking go?
        return super.toBasePane(position);
    }

}
