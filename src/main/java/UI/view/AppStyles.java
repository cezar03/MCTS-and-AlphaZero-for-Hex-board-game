package UI.view;

import java.util.Objects;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

/**
 * Centralized utility class for applying application-wide JavaFX styles.
 *
 * <p>This class is responsible for loading and applying the main CSS stylesheet
 * ({@code app.css}) to different JavaFX UI elements such as {@link Scene}s
 * and dialog components.</p>
 *
 * <p>Using this utility ensures that:</p>
 * <ul>
 *   <li>All scenes share a consistent visual theme</li>
 *   <li>Alerts and dialogs visually match the rest of the application</li>
 *   <li>CSS loading logic is defined in a single, reusable location</li>
 * </ul>
 *
 * <p>The stylesheet is loaded from the classpath root using
 * {@code /app.css}. If the resource is missing, a {@link NullPointerException}
 * will be thrown via {@link Objects#requireNonNull(Object)}.</p>
 *
 * <p>This class is non-instantiable and intended to be used statically.</p>
 */
public final class AppStyles {

    /**
     * Private constructor to prevent instantiation.
     */
    private AppStyles() {}

    /**
     * Applies the application stylesheet to the given JavaFX scene.
     *
     * <p>This should typically be called once per scene, after the scene
     * has been created and before it is shown.</p>
     *
     * @param scene the JavaFX scene to style
     * @throws NullPointerException if {@code app.css} cannot be found on the classpath
     */
    public static void apply(Scene scene) {
        scene.getStylesheets().add(
                Objects.requireNonNull(AppStyles.class.getResource("/app.css")).toExternalForm()
        );
    }

    /**
     * Applies application styling to a JavaFX {@link Alert}.
     *
     * <p>This is a convenience overload that styles the underlying
     * {@link DialogPane} of the alert.</p>
     *
     * @param alert the alert to style
     */
    public static void styleDialog(Alert alert) {
        styleDialog(alert.getDialogPane());
    }

    /**
     * Applies application styling to a JavaFX {@link DialogPane}.
     *
     * <p>The method attaches the main stylesheet and adds the
     * {@code themed-dialog} style class, allowing dialogs to be
     * customized via CSS.</p>
     *
     * @param pane the dialog pane to style
     * @throws NullPointerException if {@code app.css} cannot be found on the classpath
     */
    public static void styleDialog(DialogPane pane) {
        pane.getStylesheets().add(
                Objects.requireNonNull(AppStyles.class.getResource("/app.css")).toExternalForm()
        );
        pane.getStyleClass().add("themed-dialog");
    }
}
