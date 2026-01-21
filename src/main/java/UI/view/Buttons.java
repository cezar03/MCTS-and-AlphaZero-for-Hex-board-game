package UI.view;

import javafx.scene.control.Button;

/**
 * Utility factory class for creating consistently styled JavaFX buttons.
 *
 * <p>This class centralizes the creation of commonly used button variants
 * (primary, secondary, and ghost) so that UI styling and sizing remain
 * consistent across the application.</p>
 *
 * <p>Each factory method:</p>
 * <ul>
 *   <li>Applies a predefined CSS style class</li>
 *   <li>Sets a fixed maximum width</li>
 *   <li>Sets a preferred height appropriate for the button type</li>
 * </ul>
 *
 * <p>The actual visual appearance is controlled via external CSS
 * (e.g. {@code btn-primary}, {@code btn-secondary}, {@code btn-ghost}).</p>
 *
 * <p>This class is non-instantiable and intended to be used statically.</p>
 *
 * <pre>
 * Button play = Buttons.primary("Play");
 * Button back = Buttons.secondary("Back");
 * Button help = Buttons.ghost("Help");
 * </pre>
 */
public final class Buttons {

    /**
     * Private constructor to prevent instantiation.
     */
    private Buttons() {}

    /**
     * Creates a primary action button.
     *
     * <p>Primary buttons are typically used for the main or most important
     * action on a screen.</p>
     *
     * @param text the text displayed on the button
     * @return a styled primary {@link Button}
     */
    public static Button primary(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-primary");
        b.setMaxWidth(220);
        b.setPrefHeight(48);
        return b;
    }

    /**
     * Creates a secondary action button.
     *
     * <p>Secondary buttons are typically used for less prominent actions
     * or alternatives to the primary action.</p>
     *
     * @param text the text displayed on the button
     * @return a styled secondary {@link Button}
     */
    public static Button secondary(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-secondary");
        b.setMaxWidth(220);
        b.setPrefHeight(44);
        return b;
    }

    /**
     * Creates a ghost-style button.
     *
     * <p>Ghost buttons are usually visually minimal and are often used for
     * tertiary actions, navigation links, or dismissive actions.</p>
     *
     * @param text the text displayed on the button
     * @return a styled ghost {@link Button}
     */
    public static Button ghost(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-ghost");
        b.setMaxWidth(220);
        b.setPrefHeight(40);
        return b;
    }
}












