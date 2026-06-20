import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Główna klasa aplikacji – prosty edytor figur geometrycznych.
 *
 * Użytkownik może rysować okręgi, prostokąty i wielokąty, a następnie je
 * przesuwać, skalować scrollem, obracać (Shift + scroll) i zmieniać kolor
 * prawym klikiem myszy. Stan figur można zapisać do pliku binarnego
 * i wczytać przy kolejnym uruchomieniu.
 *
 * Klasa rozszerza {@link Application} zgodnie z wymogiem JavaFX.
 * Punkt wejścia to {@link #start(Stage)}, a nie main() – ta tylko wywołuje
 * launch().
 *
 * @author Serhii Onopriienko
 * @version 1.0
 */
public class GraphicEditor extends Application {

    /**
     * Tryby pracy edytora.
     *
     * Aktywny tryb decyduje o tym, co się dzieje po kliknięciu na panelu.
     * Po narysowaniu figury aplikacja automatycznie wraca do SELECT.
     */
    public enum Tool {
        /** Rysowanie okręgu przez przeciągnięcie myszy. */
        CIRCLE,
        /** Rysowanie prostokąta przez przeciągnięcie myszy. */
        RECTANGLE,
        /**
         * Rysowanie wielokąta kliknięciami; zamknięcie przez klik blisko pierwszego
         * wierzchołka.
         */
        POLYGON,
        /** Zaznaczanie i edycja istniejących figur. */
        SELECT
    }

    /** Aktualnie aktywny tryb edytora. */
    private Tool currentTool = Tool.SELECT;

    /** Panel rysowania – wszystkie figury są jego bezpośrednimi dziećmi. */
    private Pane drawPane;

    /**
     * Aktualnie zaznaczona figura, null jeśli nic nie jest zaznaczone.
     * Zaznaczona figura ma czerwony obrys 3px (patrz: {@link #makeActive}).
     */
    private Shape activeShape = null;

    /** Punkt startowy przeciągania przy rysowaniu okręgu lub prostokąta. */
    private double startX, startY;

    /**
     * Figura w trakcie rysowania, dodana do panelu zanim użytkownik puści przycisk
     * myszy.
     */
    private Shape previewShape;

    /** Menu kontekstowe z ColorPickerem, wywoływane prawym klikiem na figurze. */
    private ContextMenu contextMenu;

    /** ColorPicker osadzony w {@link #contextMenu}. */
    private ColorPicker colorPicker;

    /**
     * Tymczasowe znaczniki wierzchołków podczas rysowania wielokąta.
     * Są usuwane w całości po zamknięciu figury lub przy rozpoczęciu nowego
     * wielokąta.
     */
    private List<Shape> tempMarkers = new ArrayList<>();

    /**
     * Buduje główne okno: BorderPane z paskiem menu na górze i panelem rysowania w
     * centrum.
     * Pane jest domyślnie przezroczysty, więc białe tło ustawiamy ręcznie.
     *
     * @param primaryStage główne okno tworzone przez framework JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        drawPane = new Pane();
        drawPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        setupRightClickMenu();
        setupMouseInteractions();

        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);
        root.setCenter(drawPane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Edytor Graficzny");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Buduje i zwraca pasek menu z pozycjami: Plik, Rysowanie, Pomoc.
     *
     * <pre>
     * Plik:      Zapisz figury | Wczytaj figury
     * Rysowanie: SELECT | Okrąg | Prostokąt | Wielokąt
     * Pomoc:     Instrukcja użytkownika | [przycisk Info]
     * </pre>
     *
     * Przycisk Info jest osadzony jako {@link CustomMenuItem} z
     * setHideOnClick(false),
     * żeby menu nie zamykało się po kliknięciu w przycisk.
     *
     * @param stage przekazywane dalej do FileChooser jako właściciel okna
     *              dialogowego
     * @return gotowy, skonfigurowany pasek menu
     */
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("Plik");
        MenuItem saveItem = new MenuItem("Zapisz figury");
        saveItem.setOnAction(e -> saveShapesToFile(stage));
        MenuItem loadItem = new MenuItem("Wczytaj figury");
        loadItem.setOnAction(e -> loadShapesFromFile(stage));
        fileMenu.getItems().addAll(saveItem, loadItem);

        Menu drawMenu = new Menu("Rysowanie");
        MenuItem selectItem = new MenuItem("Wybierz/Modyfikuj (SELECT)");
        selectItem.setOnAction(e -> currentTool = Tool.SELECT);
        MenuItem circleItem = new MenuItem("Okrąg");
        circleItem.setOnAction(e -> currentTool = Tool.CIRCLE);
        MenuItem rectItem = new MenuItem("Prostokąt");
        rectItem.setOnAction(e -> currentTool = Tool.RECTANGLE);
        MenuItem polyItem = new MenuItem("Wielokąt");
        polyItem.setOnAction(e -> currentTool = Tool.POLYGON);
        drawMenu.getItems().addAll(selectItem, new SeparatorMenuItem(), circleItem, rectItem, polyItem);

        Menu helpMenu = new Menu("Pomoc");
        MenuItem manualItem = new MenuItem("Instrukcja użytkownika");
        manualItem.setOnAction(e -> showManualDialog());

        Button infoButton = new Button("Info");
        infoButton.setOnAction(e -> showInfoDialog());
        CustomMenuItem infoButtonItem = new CustomMenuItem(infoButton);
        infoButtonItem.setHideOnClick(false);

        helpMenu.getItems().addAll(manualItem, new SeparatorMenuItem(), infoButtonItem);

        menuBar.getMenus().addAll(fileMenu, drawMenu, helpMenu);
        return menuBar;
    }

    /**
     * Tworzy menu kontekstowe z {@link ColorPicker}em wywoływane prawym klikiem na
     * figurze.
     *
     * false w konstruktorze CustomMenuItem – kliknięcie wewnątrz ColorPickera
     * nie zamknie menu, żeby użytkownik mógł spokojnie wybrać kolor.
     */
    private void setupRightClickMenu() {
        contextMenu = new ContextMenu();
        colorPicker = new ColorPicker();

        colorPicker.setOnAction(e -> {
            if (activeShape != null) {
                activeShape.setFill(colorPicker.getValue());
            }
        });

        CustomMenuItem colorItem = new CustomMenuItem(colorPicker, false);
        contextMenu.getItems().add(colorItem);
    }

    /**
     * Rejestruje handlery myszy na panelu rysowania.
     *
     * <ul>
     * <li>onMousePressed – odznaczanie figury (SELECT), dodawanie wierzchołka
     * wielokąta lub zapamiętanie punktu startowego dla okręgu/prostokąta.</li>
     * <li>onMouseDragged – aktualizacja rozmiaru okręgu lub prostokąta na
     * bieżąco.</li>
     * <li>onMouseReleased – finalizacja figury i powrót do SELECT.</li>
     * </ul>
     *
     * Wielokąt zamyka się przez kliknięcie w odległości &lt;15 px od pierwszego
     * wierzchołka (wymagane co najmniej 3 wierzchołki).
     * Promień okręgu to połowa większego z wymiarów prostokąta opisanego przez
     * przeciągnięcie.
     */
    private void setupMouseInteractions() {

        drawPane.setOnMousePressed(e -> {
            if (currentTool == Tool.SELECT) {
                // Klik na pusty obszar panelu (nie na figurę) odznacza aktywną figurę
                if (e.getTarget() == drawPane) {
                    clearActiveShape();
                }
                return;
            }

            if (e.getButton() != MouseButton.PRIMARY)
                return;

            if (currentTool == Tool.POLYGON) {

                if (previewShape == null || !(previewShape instanceof Polygon)) {
                    // Pierwsze kliknięcie – tworzymy nowy wielokąt
                    previewShape = new Polygon();
                    previewShape.setFill(Color.LIGHTBLUE);
                    previewShape.setStroke(Color.BLACK);
                    drawPane.getChildren().add(previewShape);

                    // Okrągły znacznik na pierwszym wierzchołku – odróżnia go od pozostałych,
                    // bo to właśnie w niego trzeba trafić żeby zamknąć figurę
                    Circle firstMarker = new Circle(e.getX(), e.getY(), 5);
                    firstMarker.setFill(Color.BLACK);

                    tempMarkers.clear();
                    tempMarkers.add(firstMarker);
                    drawPane.getChildren().add(firstMarker);

                    ((Polygon) previewShape).getPoints().addAll(e.getX(), e.getY());

                } else {
                    Polygon p = (Polygon) previewShape;
                    double firstX = p.getPoints().get(0);
                    double firstY = p.getPoints().get(1);

                    if (p.getPoints().size() >= 6 && Math.hypot(e.getX() - firstX, e.getY() - firstY) < 15) {
                        // Klik blisko pierwszego wierzchołka – zamykamy figurę
                        drawPane.getChildren().removeAll(tempMarkers);
                        tempMarkers.clear();

                        setupShapeBehaviors(p);
                        makeActive(p);

                        currentTool = Tool.SELECT;
                        previewShape = null;

                    } else {
                        // Kolejny wierzchołek – mały kwadratowy znacznik
                        Rectangle normalMarker = new Rectangle(e.getX() - 3, e.getY() - 3, 6, 6);
                        normalMarker.setFill(Color.BLACK);

                        tempMarkers.add(normalMarker);
                        drawPane.getChildren().add(normalMarker);
                        p.getPoints().addAll(e.getX(), e.getY());
                    }
                }

                return;
            }

            startX = e.getX();
            startY = e.getY();

            if (currentTool == Tool.CIRCLE) {
                previewShape = new Circle(startX, startY, 0);
            } else if (currentTool == Tool.RECTANGLE) {
                previewShape = new Rectangle(startX, startY, 0, 0);
            }

            if (previewShape != null) {
                previewShape.setFill(Color.LIGHTBLUE);
                previewShape.setStroke(Color.BLACK);
                drawPane.getChildren().add(previewShape);
            }
        });

        drawPane.setOnMouseDragged(e -> {
            if (currentTool == Tool.SELECT || currentTool == Tool.POLYGON || previewShape == null)
                return;

            double currentX = Math.max(0, e.getX());
            double currentY = Math.max(0, e.getY());
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);

            if (previewShape instanceof Circle) {
                double radius = Math.max(width, height) / 2;
                ((Circle) previewShape).setRadius(radius);

            } else if (previewShape instanceof Rectangle) {
                Rectangle r = (Rectangle) previewShape;
                // Math.min żeby poprawnie obsługiwać przeciąganie w lewo lub górę
                r.setX(Math.min(startX, currentX));
                r.setY(Math.min(startY, currentY));
                r.setWidth(width);
                r.setHeight(height);
            }
        });

        drawPane.setOnMouseReleased(e -> {
            if (currentTool == Tool.SELECT || currentTool == Tool.POLYGON || previewShape == null)
                return;

            setupShapeBehaviors(previewShape);
            makeActive(previewShape);
            currentTool = Tool.SELECT;
            previewShape = null;
        });
    }

    /**
     * Podpina handlery edycji do figury: zaznaczanie, przeciąganie, menu koloru i
     * scroll.
     *
     * Offset kursora jest zapisywany przez setUserData() jako double[2], żeby
     * figura
     * nie "skakała" do kursora przy pierwszym ruchu podczas przeciągania.
     *
     * Scroll pionowy: skalowanie o ±10% na krok (min. 10% oryginalnego rozmiaru).
     * Shift+Scroll lub deltaX != 0 (trackpad): obrót o ±10° na krok.
     *
     * @param shape figura, do której podpinamy handlery
     */
    private void setupShapeBehaviors(Shape shape) {

        shape.setOnMousePressed(e -> {
            if (currentTool != Tool.SELECT)
                return;

            makeActive(shape);

            shape.setUserData(new double[] {
                    e.getSceneX() - shape.getTranslateX(),
                    e.getSceneY() - shape.getTranslateY()
            });

            // consume() żeby zdarzenie nie trafiło też do drawPane i nie odznaczył figury
            e.consume();
        });

        shape.setOnMouseDragged(e -> {
            if (currentTool != Tool.SELECT || shape != activeShape)
                return;

            double[] offset = (double[]) shape.getUserData();
            shape.setTranslateX(e.getSceneX() - offset[0]);
            shape.setTranslateY(e.getSceneY() - offset[1]);
            e.consume();
        });

        shape.setOnContextMenuRequested(e -> {
            if (currentTool != Tool.SELECT)
                return;

            makeActive(shape);
            colorPicker.setValue((Color) shape.getFill());
            contextMenu.show(shape, e.getScreenX(), e.getScreenY());
            e.consume();
        });

        shape.setOnScroll(e -> {
            if (currentTool != Tool.SELECT || shape != activeShape)
                return;

            double deltaY = e.getDeltaY();
            double deltaX = e.getDeltaX();

            if (deltaY == 0 && deltaX == 0)
                return;

            if (e.isShiftDown() || deltaX != 0) {
                // Na niektórych systemach Shift+Scroll generuje deltaX zamiast deltaY
                double delta = (deltaX != 0) ? deltaX : deltaY;
                double angleDelta = delta > 0 ? 10 : -10;
                shape.setRotate(shape.getRotate() + angleDelta);

            } else {
                double scaleFactor = deltaY > 0 ? 1.1 : 0.9;

                if (shape.getScaleX() * scaleFactor > 0.1) {
                    shape.setScaleX(shape.getScaleX() * scaleFactor);
                    shape.setScaleY(shape.getScaleY() * scaleFactor);
                }
            }

            e.consume();
        });
    }

    /**
     * Zaznacza figurę jako aktywną (czerwony obrys 3px).
     * Poprzednio zaznaczona figura wraca do czarnego obrysu 1px.
     *
     * @param shape figura do zaznaczenia
     */
    private void makeActive(Shape shape) {
        clearActiveShape();
        activeShape = shape;
        activeShape.setStrokeWidth(3);
        activeShape.setStroke(Color.RED);
    }

    /**
     * Odznacza aktywną figurę (przywraca czarny obrys 1px) i zamyka menu
     * kontekstowe.
     * Jeśli żadna figura nie jest aktywna, nie robi nic.
     */
    private void clearActiveShape() {
        if (activeShape != null) {
            activeShape.setStrokeWidth(1);
            activeShape.setStroke(Color.BLACK);
            activeShape = null;
        }
        contextMenu.hide();
    }

    /**
     * Zapisuje wszystkie figury z panelu do wybranego pliku (serializacja binarna).
     *
     * Shape z JavaFX nie jest serializowalny – każda figura jest najpierw
     * konwertowana do {@link ShapeData}. Anulowanie dialogu nie powoduje żadnej
     * akcji.
     *
     * @param stage właściciel okna FileChooser (wymagany przez API)
     */
    private void saveShapesToFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz figury");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            List<ShapeData> dataList = new ArrayList<>();

            for (var node : drawPane.getChildren()) {
                if (node instanceof Shape) {
                    dataList.add(new ShapeData((Shape) node));
                }
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(dataList);
                showAlert("Sukces", "Zapisano pomyślnie.");
            } catch (IOException ex) {
                showAlert("Błąd zapisu", ex.getMessage());
            }
        }
    }

    /**
     * Wczytuje figury z pliku binarnego, zastępując całą zawartość panelu.
     *
     * Wczytane {@link ShapeData} są zamieniane z powrotem na figury JavaFX,
     * a następnie rejestrowane są dla nich handlery przez
     * {@link #setupShapeBehaviors}.
     * Łapie {@code Exception} (nie tylko IOException), bo readObject() może rzucić
     * też ClassNotFoundException gdy plik pochodzi z innej wersji programu.
     *
     * @param stage właściciel okna FileChooser (wymagany przez API)
     */
    private void loadShapesFromFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj figury");
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

                @SuppressWarnings("unchecked")
                List<ShapeData> dataList = (List<ShapeData>) ois.readObject();

                drawPane.getChildren().clear();
                clearActiveShape();

                for (ShapeData data : dataList) {
                    Shape shape = data.createShape();
                    setupShapeBehaviors(shape);
                    drawPane.getChildren().add(shape);
                }

                showAlert("Sukces", "Wczytano pomyślnie. Możesz kontynuować edycję.");

            } catch (Exception ex) {
                showAlert("Błąd odczytu", ex.getMessage());
            }
        }
    }

    /**
     * Wyświetla okno dialogowe z informacjami o programie.
     *
     * setMinHeight/Width(USE_PREF_SIZE) jest konieczne, bo bez tego JavaFX
     * może przyciąć długi tekst w oknie alertu.
     */
    private void showInfoDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText("O programie");
        alert.setContentText("Nazwa: Edytor Graficzny\n" +
                "Przeznaczenie: Rysowanie i edycja figur geometrycznych\n" +
                "Autor: Serhii Onopriienko | 293539");

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    /**
     * Wyświetla instrukcję obsługi programu w oknie dialogowym.
     *
     * setMinHeight/Width(USE_PREF_SIZE) jest konieczne, bo bez tego JavaFX
     * może przyciąć długi tekst w oknie alertu.
     */
    private void showManualDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instrukcja użytkownika");
        alert.setHeaderText("Jak używać programu");
        alert.setContentText(
                "1. RYSOWANIE: Wybierz figurę z menu 'Rysowanie'. Kliknij i przeciągnij myszą po białym polu.\n" +
                        "2. AKTYWACJA: Wybierz narzędzie 'SELECT' i kliknij na figurę, by ją uaktywnić (czerwona ramka).\n"
                        +
                        "3. PRZESUWANIE: Przeciągnij aktywną figurę myszą.\n" +
                        "4. ZMIANA ROZMIARU: Użyj kółka myszy (scroll) na aktywnej figurze.\n" +
                        "5. OBRACANIE: Przytrzymaj klawisz SHIFT i użyj kółka myszy na aktywnej figurze.\n" +
                        "6. ZMIANA KOLORU: Kliknij prawym przyciskiem myszy na aktywną figurę.\n" +
                        "7. ZAPIS/ODCZYT: Użyj menu 'Plik'.");

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    /**
     * Wyświetla prosty alert informacyjny – pomocnik do komunikatów o sukcesie i
     * błędach.
     *
     * @param title   tytuł okna dialogowego
     * @param content treść komunikatu
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Punkt wejścia aplikacji – wywołuje launch(), które inicjalizuje JavaFX i
     * uruchamia start().
     */
    public static void main(String[] args) {
        launch(args);
    }
}

/**
 * Serializowalny kontener danych figury JavaFX.
 *
 * Shape z javafx.scene.shape nie implementuje Serializable, więc każda figura
 * jest "fotografowana" do tej klasy przed zapisem i odtwarzana przy odczycie.
 * Przechowuje: typ, geometrię, translację, skalę, obrót i kolor (jako hex
 * string).
 *
 * serialVersionUID jest ustawiony ręcznie dla kompatybilności między wersjami
 * programu.
 */
class ShapeData implements Serializable {

    /** Stała wersji serializacji – zmień jeśli zmienisz strukturę tej klasy. */
    private static final long serialVersionUID = 1L;

    /** Typ figury: "CIRCLE", "RECTANGLE" lub "POLYGON". */
    private String type;

    /**
     * Współrzędna X pozycji bazowej (środek okręgu lub lewy górny róg prostokąta).
     */
    private double x;

    /** Współrzędna Y pozycji bazowej. */
    private double y;

    /** Szerokość prostokąta. Dla pozostałych typów nieużywane. */
    private double width;

    /** Wysokość prostokąta. Dla pozostałych typów nieużywane. */
    private double height;

    /** Promień okręgu. Dla pozostałych typów nieużywane. */
    private double radius;

    /**
     * Lista wierzchołków wielokąta w formacie [x0, y0, x1, y1, ...].
     * null dla okręgu i prostokąta.
     */
    private List<Double> points;

    /** Poziome przesunięcie figury (wynik operacji drag). */
    private double translateX;

    /** Pionowe przesunięcie figury (wynik operacji drag). */
    private double translateY;

    /** Skala w osi X (1.0 = bez zmiany). */
    private double scaleX;

    /** Skala w osi Y (1.0 = bez zmiany). */
    private double scaleY;

    /** Kąt obrotu w stopniach. */
    private double rotate;

    /**
     * Kolor wypełnienia jako hex string w formacie JavaFX ("0xrrggbbaa").
     * Color nie jest serializowalny, stąd zapis jako String.
     */
    private String hexColor;

    /**
     * Zapisuje stan figury JavaFX w polach tej klasy.
     *
     * @param shape figura do zapisania – musi być Circle, Rectangle lub Polygon;
     *              inne typy są ignorowane (type pozostaje null)
     */
    public ShapeData(Shape shape) {
        this.translateX = shape.getTranslateX();
        this.translateY = shape.getTranslateY();
        this.scaleX = shape.getScaleX();
        this.scaleY = shape.getScaleY();
        this.rotate = shape.getRotate();
        this.hexColor = shape.getFill().toString();

        if (shape instanceof Circle) {
            this.type = "CIRCLE";
            Circle c = (Circle) shape;
            this.x = c.getCenterX();
            this.y = c.getCenterY();
            this.radius = c.getRadius();

        } else if (shape instanceof Rectangle) {
            this.type = "RECTANGLE";
            Rectangle r = (Rectangle) shape;
            this.x = r.getX();
            this.y = r.getY();
            this.width = r.getWidth();
            this.height = r.getHeight();

        } else if (shape instanceof Polygon) {
            this.type = "POLYGON";
            Polygon p = (Polygon) shape;
            // Kopia listy – nie chcemy trzymać referencji do wewnętrznej listy JavaFX
            this.points = new ArrayList<>(p.getPoints());
        }
    }

    /**
     * Odtwarza figurę JavaFX z zapisanych danych.
     *
     * Figura startuje zawsze z czarnym obrysem 1px – nie chcemy wczytywać figur
     * z czerwoną ramką zaznaczenia. Color.web() parsuje format "0xrrggbbaa".
     *
     * @return odtworzona figura gotowa do dodania do drawPane,
     *         lub {@code null} jeśli {@link #type} ma nieznany typ
     */
    public Shape createShape() {
        Shape shape = null;

        if ("CIRCLE".equals(type)) {
            shape = new Circle(x, y, radius);

        } else if ("RECTANGLE".equals(type)) {
            shape = new Rectangle(x, y, width, height);

        } else if ("POLYGON".equals(type)) {
            Polygon p = new Polygon();
            p.getPoints().addAll(points);
            shape = p;
        }

        if (shape != null) {
            shape.setTranslateX(translateX);
            shape.setTranslateY(translateY);
            shape.setScaleX(scaleX);
            shape.setScaleY(scaleY);
            shape.setRotate(rotate);
            shape.setFill(Color.web(hexColor));
            shape.setStroke(Color.BLACK);
            shape.setStrokeWidth(1);
        }

        return shape;
    }
}
