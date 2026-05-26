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
 * Główna klasa aplikacji.
 *
 * Użytkownik może rysować trzy rodzaje figur: okrąg, prostokąt i wielokąt
 * dowolny (np. trójkąt). Każdą narysowaną figurę można później zaznaczać,
 * przesuwać, skalować kółkiem myszy, obracać (Shift + scroll) oraz zmieniać
 * jej kolor przez prawy przycisk myszy. Stan wszystkich figur (pozycja, kolor,
 * skala, obrót) można zapisać do pliku binarnego i wczytać go przy kolejnym
 * uruchomieniu.
 *
 * Klasa rozszerza {@link Application}, co jest wymagane przez framework JavaFX
 * 
 * punkt wejścia do aplikacji to metoda {@link #start(Stage)}, a nie main().
 * Metoda main() wywołuje tylko {@code launch()}, które wewnętrznie tworzy
 * instancję tej klasy i wywołuje start().
 *
 * @author Serhii Onopriienko
 * @version 1.0
 */
public class GraphicEditor extends Application {

    /**
     * Wyliczenie dostępnych trybów pracy edytora.
     *
     * Tryb jest przechowywany w polu {@link #currentTool} i decyduje o tym,
     * co dzieje się po kliknięciu na obszar rysowania:
     * <ul>
     * <li>{@code CIRCLE} - rysowanie okręgu przez przeciągnięcie myszy</li>
     * <li>{@code RECTANGLE} - rysowanie prostokąta przez przeciągnięcie myszy</li>
     * <li>{@code POLYGON} - rysowanie wielokąta punkt po punkcie (kliknięcia)</li>
     * <li>{@code SELECT} - zaznaczanie i edycja istniejących figur</li>
     * </ul>
     *
     * Po narysowaniu figury aplikacja automatycznie wraca do trybu SELECT,
     * żeby użytkownik mógł od razu manipulować nowo dodaną figurą.
     */
    public enum Tool {
        CIRCLE, RECTANGLE, POLYGON, SELECT
    }

    /** Aktualnie aktywne narzędzie. Domyślnie SELECT - tryb edycji. */
    private Tool currentTool = Tool.SELECT;

    /**
     * Główny panel rysowania, na którym umieszczane są wszystkie figury.
     * Używamy {@link javafx.scene.layout.Pane}, bo każda
     * figura jest osobnym węzłem sceny - dzięki temu możemy łatwo reagować
     * na kliknięcia konkretnie w daną figurę i modyfikować jej właściwości.
     */
    private Pane drawPane;

    /**
     * Referencja do aktualnie zaznaczonej figury.
     * Null oznacza brak zaznaczenia. Zaznaczona figura jest wyróżniona
     * czerwonym obrysem o grubości 3px (patrz: {@link #makeActive}).
     */
    private Shape activeShape = null;

    /**
     * Współrzędne punktu, w którym użytkownik zaczął przeciągać mysz
     * podczas rysowania okręgu lub prostokąta. Potrzebne w onMouseDragged,
     * żeby wyliczyć aktualny rozmiar figury odnośnie do punktu startowego.
     */
    private double startX, startY;

    /**
     * Referencja do figury w trakcie rysowania (zanim użytkownik puści przycisk
     * myszy lub zamknie wielokąt). Figura jest już dodana do drawPane i
     * aktualizowana na bieżąco podczas przeciągania. Po zakończeniu rysowania
     * pole jest zerowane do null.
     */
    private Shape previewShape;

    /**
     * Menu kontekstowe wywoływane prawym klikiem na zaznaczoną figurę.
     * Zawiera tylko jeden element: {@link #colorPicker}.
     */
    private ContextMenu contextMenu;

    /**
     * Kontrolka wyboru koloru, osadzona wewnątrz {@link #contextMenu}.
     * Po zmianie koloru przez użytkownika, nowy kolor jest od razu aplikowany
     * do aktualnie zaznaczonej figury ({@link #activeShape}).
     */
    private ColorPicker colorPicker;

    /**
     * Tymczasowe znaczniki wizualne wyświetlane podczas rysowania wielokąta.
     * Każde kliknięcie dodaje jeden znacznik: okrągły (czarny krąg r=5) dla
     * pierwszego wierzchołka, kwadratowy (6x6 px) dla kolejnych. Dzięki temu
     * użytkownik widzi, gdzie już kliknął. Lista jest czyszczona w całości
     * po zamknięciu figury lub przy rozpoczęciu nowego wielokąta.
     */
    private List<Shape> tempMarkers = new ArrayList<>();

    /**
     * Punkt wejścia aplikacji JavaFX - wywoływany przez framework po launch().
     *
     * Buduje layout okna: {@link javafx.scene.layout.BorderPane} z paskiem
     * menu na górze i panelem rysowania w centrum. Białe tło panelu jest
     * ustawione ręcznie, bo domyślnie Pane jest przezroczysty.
     *
     * @param primaryStage Główne okno aplikacji, tworzone automatycznie przez
     *                     framework JavaFX. Tu ustawiamy tytuł, scenę i robimy
     *                     show().
     */
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        drawPane = new Pane();
        // Pane domyślnie jest przezroczysty - jawnie ustawiamy białe tło,
        // żeby obszar roboczy był wyraźnie widoczny
        drawPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // Konfigurujemy menu kontekstowe i obsługę myszy zanim cokolwiek
        // pojawi się na ekranie - handlery muszą być gotowe od razu
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
     * Buduje i zwraca pasek menu z trzema pozycjami: Plik, Rysowanie, Pomoc.
     *
     * Referencja do {@code stage} jest tu potrzebna tylko po to, żeby przekazać
     * ją do okien dialogowych wyboru pliku (FileChooser wymaga właściciela okna,
     * żeby zachować prawidłową modalność).
     *
     * Struktura menu:
     * 
     * <pre>
     * Plik
     *   Zapisz figury      -> saveShapesToFile()
     *   Wczytaj figury     -> loadShapesFromFile()
     *
     * Rysowanie
     *   Wybierz/Modyfikuj  -> currentTool = SELECT
     *   ─────────────────
     *   Okrąg              -> currentTool = CIRCLE
     *   Prostokąt          -> currentTool = RECTANGLE
     *   Wielokąt           -> currentTool = POLYGON
     *
     * Pomoc
     *   Instrukcja użytkownika -> showManualDialog()
     *   ─────────────────
     *   [przycisk Info]        -> showInfoDialog()
     * </pre>
     *
     * Przycisk "Info" w menu Pomoc jest celowo dodany jako {@link CustomMenuItem}
     * z setHideOnClick(false) - menu nie zamknie się po kliknięciu w przycisk,
     * co jest bardziej przewidywalnym zachowaniem dla elementu osadzonego.
     *
     * @param stage Referencja do głównego okna, przekazywana dalej do
     *              metod zapisu/odczytu pliku.
     * @return Gotowy, skonfigurowany pasek menu.
     */
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        // --- Menu "Plik" ---
        Menu fileMenu = new Menu("Plik");
        MenuItem saveItem = new MenuItem("Zapisz figury");
        saveItem.setOnAction(e -> saveShapesToFile(stage));
        MenuItem loadItem = new MenuItem("Wczytaj figury");
        loadItem.setOnAction(e -> loadShapesFromFile(stage));
        fileMenu.getItems().addAll(saveItem, loadItem);

        // --- Menu "Rysowanie" ---
        Menu drawMenu = new Menu("Rysowanie");
        MenuItem selectItem = new MenuItem("Wybierz/Modyfikuj (SELECT)");
        selectItem.setOnAction(e -> currentTool = Tool.SELECT);
        MenuItem circleItem = new MenuItem("Okrąg");
        circleItem.setOnAction(e -> currentTool = Tool.CIRCLE);
        MenuItem rectItem = new MenuItem("Prostokąt");
        rectItem.setOnAction(e -> currentTool = Tool.RECTANGLE);
        MenuItem polyItem = new MenuItem("Wielokąt (Trójkąt)");
        polyItem.setOnAction(e -> currentTool = Tool.POLYGON);
        drawMenu.getItems().addAll(selectItem, new SeparatorMenuItem(), circleItem, rectItem, polyItem);

        // --- Menu "Pomoc" ---
        Menu helpMenu = new Menu("Pomoc");
        MenuItem infoItem = new MenuItem("Info");
        infoItem.setOnAction(e -> showInfoDialog());
        MenuItem manualItem = new MenuItem("Instrukcja użytkownika");
        manualItem.setOnAction(e -> showManualDialog());

        // Przycisk osadzony jako CustomMenuItem - kliknięcie nie zamknie menu
        // (setHideOnClick(false)), bo chcemy żeby użytkownik mógł nacisnąć
        // przycisk bez efektu ubocznego w postaci zamknięcia całego menu
        Button infoButton = new Button("Info");
        infoButton.setOnAction(e -> showInfoDialog());
        CustomMenuItem infoButtonItem = new CustomMenuItem(infoButton);
        infoButtonItem.setHideOnClick(false);

        helpMenu.getItems().addAll(manualItem, new SeparatorMenuItem(), infoButtonItem);

        menuBar.getMenus().addAll(fileMenu, drawMenu, helpMenu);
        return menuBar;
    }

    /**
     * Tworzy i konfiguruje menu kontekstowe wywoływane prawym klikiem myszy.
     *
     * Menu zawiera wyłącznie {@link ColorPicker} osadzony jako
     * {@link CustomMenuItem}.
     * Parametr {@code false} w konstruktorze CustomMenuItem oznacza, że kliknięcie
     * wewnątrz ColorPickera nie zamknie menu - użytkownik może swobodnie wybrać
     * kolor bez niespodziewanego zamknięcia kontrolki.
     *
     * Kiedy użytkownik zatwierdzi wybrany kolor, handler od razu aplikuje go
     * do {@link #activeShape}. Sprawdzenie {@code activeShape != null} jest
     * konieczne, bo w teorii menu może być widoczne w chwili, gdy activeShape
     * zostało już wyczyszczone innym zdarzeniem.
     */
    private void setupRightClickMenu() {
        contextMenu = new ContextMenu();
        colorPicker = new ColorPicker();

        colorPicker.setOnAction(e -> {
            if (activeShape != null) {
                activeShape.setFill(colorPicker.getValue());
            }
        });

        // false = menu nie znika po kliknięciu wewnątrz ColorPickera
        CustomMenuItem colorItem = new CustomMenuItem(colorPicker, false);
        contextMenu.getItems().add(colorItem);
    }

    /**
     * Rejestruje wszystkie handlery zdarzeń myszy na głównym panelu rysowania.
     *
     * Metoda konfiguruje trzy zdarzenia: onMousePressed, onMouseDragged,
     * onMouseReleased. Logika rysowania wielokąta jest wyraźnie oddzielona
     * od logiki okręgu i prostokąta, bo wielokąt buduje się kliknięciami
     * (nie przeciąganiem), a jego zakończenie następuje przez zamknięcie
     * pierwszego i ostatniego wierzchołka.
     *
     * <b>onMousePressed</b> - obsługuje:
     * <ul>
     * <li>W trybie SELECT: kliknięcie na pusty obszar odznacza figurę</li>
     * <li>W trybie POLYGON: dodaje kolejny wierzchołek lub zamyka wielokąt</li>
     * <li>W trybach CIRCLE/RECTANGLE: zapamiętuje punkt startowy przeciągania</li>
     * </ul>
     *
     * <b>onMouseDragged</b> - aktualizuje rozmiar okręgu lub prostokąta na
     * bieżąco, żeby użytkownik widział podgląd podczas przeciągania.
     * Dla okręgu promień to połowa większego z wymiarów (width lub height),
     * co sprawia że okrąg zawsze jest "kwadratowy" (równe promienie).
     * Dla prostokąta recalkulujemy lewy górny narożnik przez Math.min, żeby
     * poprawnie obsługiwać przeciąganie w lewo/górę od punktu startowego.
     *
     * <b>onMouseReleased</b> - finalizuje figurę: nadaje jej zachowania edycji
     * ({@link #setupShapeBehaviors}), zaznacza ją i wraca do trybu SELECT.
     */
    private void setupMouseInteractions() {

        drawPane.setOnMousePressed(e -> {
            // W trybie SELECT kliknięcie na puste miejsce (tj. bezpośrednio na drawPane,
            // nie na jakąś figurę) odznacza aktualnie aktywną figurę
            if (currentTool == Tool.SELECT) {
                if (e.getTarget() == drawPane) {
                    clearActiveShape();
                }
                return;
            }

            // Ignorujemy kliknięcia innymi przyciskami niż lewy (np. prawy otwiera menu)
            if (e.getButton() != MouseButton.PRIMARY)
                return;

            // ---- WIELOKĄT: rysowanie punkt po punkcie ----
            if (currentTool == Tool.POLYGON) {

                if (previewShape == null || !(previewShape instanceof Polygon)) {
                    // Pierwsze kliknięcie - inicjujemy nowy wielokąt i od razu
                    // dodajemy go do drawPane (będzie aktualizowany w miejscu)
                    previewShape = new Polygon();
                    previewShape.setFill(Color.LIGHTBLUE);
                    previewShape.setStroke(Color.BLACK);
                    drawPane.getChildren().add(previewShape);

                    // Pierwszy wierzchołek oznaczamy okrągłym znacznikiem (r=5),
                    // żeby był wyraźnie odróżnialny - użytkownik musi w niego
                    // trafić, żeby zamknąć figurę (tolerancja 15px, patrz niżej)
                    Circle firstMarker = new Circle(e.getX(), e.getY(), 5);
                    firstMarker.setFill(Color.BLACK);

                    tempMarkers.clear();
                    tempMarkers.add(firstMarker);
                    drawPane.getChildren().add(firstMarker);

                    // Dodajemy współrzędne pierwszego wierzchołka do listy punktów polygonu
                    ((Polygon) previewShape).getPoints().addAll(e.getX(), e.getY());

                } else {
                    // Kolejne kliknięcia - sprawdzamy najpierw, czy użytkownik
                    // próbuje zamknąć figurę (klik blisko pierwszego wierzchołka)
                    Polygon p = (Polygon) previewShape;
                    double firstX = p.getPoints().get(0);
                    double firstY = p.getPoints().get(1);

                    // Warunek zamknięcia: co najmniej 3 wierzchołki (6 współrzędnych)
                    // i odległość od pierwszego punktu < 15px (tolerancja na "celowanie")
                    if (p.getPoints().size() >= 6 && Math.hypot(e.getX() - firstX, e.getY() - firstY) < 15) {

                        // Usuwamy tymczasowe znaczniki - figura jest już gotowa
                        drawPane.getChildren().removeAll(tempMarkers);
                        tempMarkers.clear();

                        // Rejestrujemy zachowania edycji i zaznaczamy figurę
                        setupShapeBehaviors(p);
                        makeActive(p);

                        // Wracamy do trybu SELECT i czyścimy referencję
                        currentTool = Tool.SELECT;
                        previewShape = null;

                    } else {
                        // Kliknięcie w nowe miejsce - dodajemy kolejny wierzchołek.
                        // Pośrednie wierzchołki oznaczamy małym kwadratem (6x6 px)
                        Rectangle normalMarker = new Rectangle(e.getX() - 3, e.getY() - 3, 6, 6);
                        normalMarker.setFill(Color.BLACK);

                        tempMarkers.add(normalMarker);
                        drawPane.getChildren().add(normalMarker);
                        p.getPoints().addAll(e.getX(), e.getY());
                    }
                }

                // Wielokąt nie używa logiki przeciągania - kończymy tu obsługę
                return;
            }

            // ---- OKRĄG i PROSTOKĄT: rysowanie przeciąganiem ----
            startX = e.getX();
            startY = e.getY();

            if (currentTool == Tool.CIRCLE) {
                // Tworzymy okrąg z promieniem 0 - urośnie podczas przeciągania
                previewShape = new Circle(startX, startY, 0);
            } else if (currentTool == Tool.RECTANGLE) {
                // Analogicznie - prostokąt o wymiarach 0x0
                previewShape = new Rectangle(startX, startY, 0, 0);
            }

            if (previewShape != null) {
                previewShape.setFill(Color.LIGHTBLUE);
                previewShape.setStroke(Color.BLACK);
                drawPane.getChildren().add(previewShape);
            }
        });

        drawPane.setOnMouseDragged(e -> {
            // Wielokąt jest budowany kliknięciami - podczas przeciągania nic nie robimy.
            // Null-check na previewShape chroni przed przeciągnięciem bez uprzedniego
            // naciśnięcia (co w praktyce nie powinno się zdarzyć, ale lepiej zabezpieczyć)
            if (currentTool == Tool.SELECT || currentTool == Tool.POLYGON || previewShape == null)
                return;

            // Ograniczamy współrzędne do obszaru panelu (minimum 0),
            // żeby figura nie "wychodziła" poza lewą lub górną krawędź
            double currentX = Math.max(0, e.getX());
            double currentY = Math.max(0, e.getY());
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);

            if (previewShape instanceof Circle) {
                // Dla okręgu bierzemy połowę większego wymiaru jako promień -
                // dzięki temu okrąg mieści się w prostokącie opisanym przez przeciągnięcie
                double radius = Math.max(width, height) / 2;
                ((Circle) previewShape).setRadius(radius);

            } else if (previewShape instanceof Rectangle) {
                Rectangle r = (Rectangle) previewShape;
                // Math.min zapewnia, że lewy górny narożnik jest zawsze "mniejszy"
                // niezależnie od tego, w którą stronę użytkownik przeciąga mysz
                r.setX(Math.min(startX, currentX));
                r.setY(Math.min(startY, currentY));
                r.setWidth(width);
                r.setHeight(height);
            }
        });

        drawPane.setOnMouseReleased(e -> {
            // Wielokąt kończymy kliknięciem w pierwszy wierzchołek (obsługiwane
            // w onMousePressed), więc tutaj go pomijamy. Sprawdzamy też null na
            // previewShape na wypadek, gdyby mouseReleased odpalił się bez
            // wcześniejszego mousePressed (np. okno straciło fokus podczas przeciągania)
            if (currentTool == Tool.SELECT || currentTool == Tool.POLYGON || previewShape == null)
                return;

            // Figura jest gotowa - rejestrujemy dla niej obsługę edycji
            setupShapeBehaviors(previewShape);

            // Zaznaczamy nową figurę od razu - wygodniejsze niż szukanie jej potem
            makeActive(previewShape);

            // Automatyczny powrót do SELECT po narysowaniu figury
            currentTool = Tool.SELECT;

            previewShape = null;
        });
    }

    /**
     * Rejestruje na figurze cztery handlery umożliwiające jej edycję po
     * narysowaniu:
     * zaznaczanie, przesuwanie, zmianę koloru (prawy klik) oraz skalowanie/obrót.
     *
     * Metoda jest wywoływana raz dla każdej figury - bezpośrednio po jej
     * narysowaniu
     * oraz dla każdej figury wczytanej z pliku (wtedy figury nie mają jeszcze
     * żadnych handlerów, bo zostały odtworzone z surowych danych).
     *
     * <b>Szczegóły implementacji przesuwania:</b>
     * Offset między pozycją kursora a lewym górnym narożnikiem figury jest
     * zapisywany
     * w {@code shape.setUserData()} jako tablica {@code double[2]}. Dzięki temu
     * figura "trzyma się" kursora w tym samym punkcie przez cały czas przeciągania,
     * a nie skacze do lewego górnego narożnika przy pierwszym ruchu.
     *
     * <b>Szczegóły implementacji scrollowania:</b>
     * Kółko myszy generuje zdarzenia z deltaY (ruch pionowy) lub deltaX (poziomy).
     * Na niektórych systemach Shift+Scroll zmienia deltaX zamiast deltaY - dlatego
     * warunek obrotu sprawdza zarówno {@code isShiftDown()} jak i
     * {@code deltaX != 0}.
     * Skalowanie ma zabezpieczenie przed zbyt mocnym pomniejszeniem (scaleX > 0.1).
     *
     * @param shape Figura, do której podpinamy wszystkie handlery edycji.
     */
    private void setupShapeBehaviors(Shape shape) {

        // Kliknięcie na figurę w trybie SELECT: zaznaczamy ją i zapamiętujemy
        // offset kursora relative do obecnej pozycji translacji figury
        shape.setOnMousePressed(e -> {
            if (currentTool != Tool.SELECT)
                return;

            makeActive(shape);

            // Offset = różnica między globalną pozycją kursora a aktualną translacją
            // figury.
            // Podczas przeciągania odjęcie tego offsetu od nowej pozycji kursora daje
            // nową translację, przy której figura zachowuje się naturalnie
            shape.setUserData(new double[] {
                    e.getSceneX() - shape.getTranslateX(),
                    e.getSceneY() - shape.getTranslateY()
            });

            // consume() zapobiega "przekazaniu" zdarzenia do drawPane, co mogłoby
            // nieumyślnie wywołać handler panelu (np. odznaczenie figury)
            e.consume();
        });

        // Przeciąganie przesuwa tylko aktywną figurę.
        // Dodatkowy warunek "shape != activeShape" chroni przed przesunięciem figury,
        // która właśnie traci focus (np. kiedy kliknięto na inną figurę, ale mysz
        // nadal przesuwa się po poprzedniej)
        shape.setOnMouseDragged(e -> {
            if (currentTool != Tool.SELECT || shape != activeShape)
                return;

            double[] offset = (double[]) shape.getUserData();
            shape.setTranslateX(e.getSceneX() - offset[0]);
            shape.setTranslateY(e.getSceneY() - offset[1]);
            e.consume();
        });

        // Prawy klik otwiera menu kontekstowe z wyborem koloru.
        // Przed pokazaniem menu ustawiamy w ColorPickerze aktualny kolor figury,
        // żeby użytkownik widział punkt startowy, a nie ostatnio używany kolor
        shape.setOnContextMenuRequested(e -> {
            if (currentTool != Tool.SELECT)
                return;

            makeActive(shape);
            colorPicker.setValue((Color) shape.getFill());
            contextMenu.show(shape, e.getScreenX(), e.getScreenY());
            e.consume();
        });

        // Kółko myszy: skalowanie (tylko scroll pionowy) lub obrót (Shift + scroll,
        // albo scroll poziomy - np. na trackpadzie Shift+scroll generuje deltaX)
        shape.setOnScroll(e -> {
            if (currentTool != Tool.SELECT || shape != activeShape)
                return;

            double deltaY = e.getDeltaY();
            double deltaX = e.getDeltaX();

            // Zdarzenia scroll z deltaX=0 i deltaY=0 się zdarzają (np. przy
            // inercyjnym scrollowaniu na końcu ruchu) - ignorujemy je
            if (deltaY == 0 && deltaX == 0)
                return;

            if (e.isShiftDown() || deltaX != 0) {
                // Tryb obrotu: jeśli jest ruch poziomy (deltaX) używamy go,
                // bo jest bardziej precyzyjny niż deltaY w trybie Shift+Scroll
                double delta = (deltaX != 0) ? deltaX : deltaY;
                double angleDelta = delta > 0 ? 10 : -10; // obrót o 10 stopni na krok
                shape.setRotate(shape.getRotate() + angleDelta);

            } else {
                // Tryb skalowania: 1.1 = powiększ o 10%, 0.9 = pomniejsz o 10%
                double scaleFactor = deltaY > 0 ? 1.1 : 0.9;

                // Zabezpieczenie: nie pozwalamy na skalowanie poniżej 10% oryginalnego
                // rozmiaru - przy mniejszych wartościach figura staje się praktycznie
                // niewidoczna i trudna do odzyskania
                if (shape.getScaleX() * scaleFactor > 0.1) {
                    shape.setScaleX(shape.getScaleX() * scaleFactor);
                    shape.setScaleY(shape.getScaleY() * scaleFactor);
                }
            }

            e.consume();
        });
    }

    /**
     * Zaznacza podaną figurę jako aktywną, nadając jej czerwony obryss o grubości
     * 3px.
     *
     * Przed zaznaczeniem nowej figury wywołuje {@link #clearActiveShape()}, żeby
     * poprzednia zaznaczona figura wróciła do normalnego wyglądu. Dzięki temu
     * zawsze jest zaznaczona co najwyżej jedna figura.
     *
     * @param shape Figura, którą chcemy zaznaczyć.
     */
    private void makeActive(Shape shape) {
        // Odznaczamy poprzednią figurę (jeśli jakaś była) przed zaznaczeniem nowej
        clearActiveShape();
        activeShape = shape;
        activeShape.setStrokeWidth(3);
        activeShape.setStroke(Color.RED);
    }

    /**
     * Odznacza aktualnie aktywną figurę, przywracając jej domyślny czarny obrys
     * 1px.
     *
     * Dodatkowo zamyka menu kontekstowe, jeśli jest otwarte - żeby nie "wisiało"
     * po odznaczeniu figury, której dotyczy.
     * Jeśli żadna figura nie jest aktywna, metoda nie robi nic (null-check).
     */
    private void clearActiveShape() {
        if (activeShape != null) {
            activeShape.setStrokeWidth(1);
            activeShape.setStroke(Color.BLACK);
            activeShape = null;
        }
        // Zamykamy menu kontekstowe niezależnie od tego, czy figura była aktywna
        contextMenu.hide();
    }

    /**
     * Zapisuje wszystkie figury z panelu rysowania do wybranego przez użytkownika
     * pliku.
     *
     * Zapis odbywa się przez serializację binarną listy obiektów {@link ShapeData}.
     * Klasy Shape z JavaFX nie są serializowalne, dlatego każda figura jest
     * najpierw
     * konwertowana do {@link ShapeData} - prostego, serializowalnego kontenera
     * danych.
     *
     * Iterujemy po wszystkich węzłach drawPane i zapisujemy te, które są
     * instancjami
     * Shape. Celowo pomijamy inne węzły (np. gdyby kiedyś dodano etykiety lub inne
     * elementy UI do drawPane). Dane zapisywane dla każdej figury:
     * pozycja, rozmiar, translacja (przesunięcie po narysowaniu), skala, kąt obrotu
     * oraz kolor wypełnienia w formacie hex.
     *
     * W przypadku anulowania przez użytkownika w dialogu (file == null) metoda
     * kończy się bez żadnej akcji i bez komunikatu.
     *
     * @param stage Referencja do okna aplikacji, wymagana przez FileChooser jako
     *              właściciel dialogu (zapewnia prawidłowe zachowanie modalne).
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
     * Wczytuje figury z pliku binarnego i odtwarza je na panelu rysowania.
     *
     * Przed wczytaniem czyścimy całą zawartość drawPane i odznaczamy aktywną
     * figurę - wczytanie jest operacją zastępującą, nie addytywną.
     * Każdy odczytany obiekt {@link ShapeData} jest zamieniany z powrotem na
     * figurę JavaFX przez {@link ShapeData#createShape()}, a następnie
     * podpinane są do niej handlery edycji przez {@link #setupShapeBehaviors}.
     *
     * Metoda łapie szerokie {@code Exception} (a nie tylko IOException), bo
     * {@code ObjectInputStream.readObject()} może rzucić też ClassNotFoundException
     * (np. gdy plik pochodzi z innej wersji aplikacji z inną strukturą ShapeData).
     *
     * Adnotacja {@code @SuppressWarnings("unchecked")} jest uzasadniona: wiemy,
     * że zapisywaliśmy dokładnie {@code List<ShapeData>}, więc rzutowanie jest
     * bezpieczne, ale kompilator nie ma możliwości tego zweryfikować przez type
     * erasure.
     *
     * @param stage Referencja do okna aplikacji, wymagana przez FileChooser.
     */
    private void loadShapesFromFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj figury");
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

                @SuppressWarnings("unchecked")
                List<ShapeData> dataList = (List<ShapeData>) ois.readObject();

                // Czyścimy panel i stan edytora przed załadowaniem nowych figur
                drawPane.getChildren().clear();
                clearActiveShape();

                for (ShapeData data : dataList) {
                    Shape shape = data.createShape();
                    // Figury odtworzone z pliku nie mają handlerów - musimy je
                    // zarejestrować ponownie, tak jak przy rysowaniu
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
     * Wyświetla okno dialogowe z podstawowymi informacjami o aplikacji.
     *
     * Wywołanie {@code setMinHeight/Width(USE_PREF_SIZE)} jest konieczne,
     * bo JavaFX domyślnie nie dostosowuje rozmiaru Alertu do długości tekstu -
     * bez tego treść może zostać ucięta lub schowana za przyciskiem OK.
     */
    private void showInfoDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText("O programie");
        alert.setContentText("Nazwa: Edytor Graficzny\n" +
                "Przeznaczenie: Rysowanie i edycja figur geometrycznych\n" +
                "Autor: Serhii Onopriienko | 293539");

        // Bez tych dwóch linii JavaFX może przyciąć długi tekst -
        // USE_PREF_SIZE oznacza "dopasuj rozmiar do preferowanej wielkości zawartości"
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    /**
     * Wyświetla instrukcję obsługi programu w oknie dialogowym.
     *
     * Tekst opisuje wszystkie dostępne operacje i skróty klawiszowe/myszkowe.
     * Tak jak w {@link #showInfoDialog()}, wymuszamy dopasowanie rozmiaru okna
     * do zawartości przez USE_PREF_SIZE.
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
     * Wyświetla prosty alert informacyjny z podanym tytułem i treścią.
     *
     * Używana jako helper do komunikatów o sukcesie lub błędzie przy
     * zapisie/odczycie
     * pliku. setHeaderText(null) ukrywa środkową sekcję nagłówka, żeby dialog
     * był kompaktowy - treść komunikatu jest wystarczająca.
     *
     * @param title   Tytuł okna dialogowego (pasek tytułu).
     * @param content Treść wyświetlana w ciele dialogu.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); // brak nagłówka - sama treść wystarczy
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Standardowy punkt wejścia aplikacji Java.
     * {@code launch(args)} inicjalizuje środowisko JavaFX i wywołuje
     * {@link #start(Stage)}.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

/**
 * Serializowalny kontener danych jednej figury geometrycznej.
 *
 * Klasy z pakietu {@code javafx.scene.shape} nie implementują
 * {@link Serializable},
 * więc nie możemy ich bezpośrednio zapisać przez ObjectOutputStream. Klasa
 * ShapeData
 * jest "mostem" między światem JavaFX a serializacją binarną - przechowuje
 * tylko
 * prymitywne typy danych i Stringi, które bez problemu można zapisać do pliku.
 *
 * Przechowywane informacje dla każdej figury:
 * <ul>
 * <li>{@code type} - identyfikator typu figury: "CIRCLE", "RECTANGLE",
 * "POLYGON"</li>
 * <li>{@code x, y} - pozycja bazowa (środek okręgu / lewy górny róg
 * prostokąta)</li>
 * <li>{@code width, height} - wymiary prostokąta</li>
 * <li>{@code radius} - promień okręgu</li>
 * <li>{@code points} - lista współrzędnych wierzchołków wielokąta
 * (x0,y0,x1,y1,...)</li>
 * <li>{@code translateX/Y} - przesunięcie figury po narysowaniu (drag)</li>
 * <li>{@code scaleX/Y} - skala (zmiana rozmiaru przez scroll)</li>
 * <li>{@code rotate} - kąt obrotu w stopniach</li>
 * <li>{@code hexColor} - kolor wypełnienia w postaci stringa hex (np.
 * "0x4169e1ff")</li>
 * </ul>
 *
 * {@code serialVersionUID} jest ustawiony ręcznie, żeby zapis z jednej wersji
 * programu dało się odczytać w drugiej, o ile struktura klasy się nie zmieniła.
 */
class ShapeData implements Serializable {

    /** Stała wersji serializacji - zmień ją, jeśli zmienisz strukturę tej klasy. */
    private static final long serialVersionUID = 1L;

    /** Typ figury jako String: "CIRCLE", "RECTANGLE" lub "POLYGON". */
    private String type;

    /**
     * Współrzędna X pozycji bazowej (środek okręgu lub lewy górny róg prostokąta).
     */
    private double x;

    /** Współrzędna Y pozycji bazowej. */
    private double y;

    /** Szerokość prostokąta. Dla pozostałych typów nieużywane (wartość 0). */
    private double width;

    /** Wysokość prostokąta. Dla pozostałych typów nieużywane (wartość 0). */
    private double height;

    /** Promień okręgu. Dla pozostałych typów nieużywane (wartość 0). */
    private double radius;

    /**
     * Lista współrzędnych wierzchołków wielokąta w formacie [x0, y0, x1, y1, ...].
     * Dla okręgu i prostokąta to pole jest null.
     */
    private List<Double> points;

    /** Poziome przesunięcie figury (wynik operacji drag). */
    private double translateX;

    /** Pionowe przesunięcie figury (wynik operacji drag). */
    private double translateY;

    /** Współczynnik skalowania w osi X. Domyślnie 1.0 (brak skalowania). */
    private double scaleX;

    /** Współczynnik skalowania w osi Y. Domyślnie 1.0 (brak skalowania). */
    private double scaleY;

    /** Kąt obrotu figury w stopniach. Domyślnie 0. */
    private double rotate;

    /**
     * Kolor wypełnienia figury jako String w formacie zwracanym przez JavaFX
     * (np. "0x4169e1ff"). Używamy Stringa zamiast Color, bo Color nie jest
     * serializowalny. Przy odczycie zamieniamy go z powrotem przez Color.web().
     */
    private String hexColor;

    /**
     * Konstruktor - "sfotografowuje" stan figury JavaFX i zapisuje go w polach tej
     * klasy.
     *
     * Wszystkie właściwości transformacji (translate, scale, rotate) są wspólne dla
     * każdego typu figury, więc odczytujemy je zawsze. Geometria (pozycja, rozmiar)
     * zależy od typu i jest odczytywana przez instanceof + rzutowanie.
     *
     * @param shape Figura JavaFX do zapisania. Musi być instancją Circle, Rectangle
     *              lub Polygon - inne typy są milcząco ignorowane (type pozostanie
     *              null).
     */
    public ShapeData(Shape shape) {
        // Transformacje są wspólne dla każdego typu figury
        this.translateX = shape.getTranslateX();
        this.translateY = shape.getTranslateY();
        this.scaleX = shape.getScaleX();
        this.scaleY = shape.getScaleY();
        this.rotate = shape.getRotate();

        // Color.toString() zwraca hex w formacie "0xrrggbbaa" - wystarczy do
        // odtworzenia
        this.hexColor = shape.getFill().toString();

        // Geometria zależy od konkretnego typu figury
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
            // Robimy kopię listy punktów - oryginał należy do figury JavaFX
            // i może się zmieniać, nie chcemy żeby ShapeData współdzieliło referencję
            this.points = new ArrayList<>(p.getPoints());
        }
    }

    /**
     * Odtwarza i zwraca figurę JavaFX na podstawie danych zapisanych w tej
     * instancji.
     *
     * Po stworzeniu figury (przez odpowiedni konstruktor zależny od {@link #type})
     * aplikujemy wszystkie zapisane transformacje. Obrys jest zawsze ustawiany na
     * czarny z grubością 1 - niezależnie od tego, czy figura była aktywna w chwili
     * zapisu (nie chcemy wczytywać figur z czerwonym obrysem 3px).
     *
     * {@code Color.web(hexColor)} parsuje string hex z powrotem do obiektu Color.
     * Działa poprawnie z formatem "0xrrggbbaa" generowanym przez JavaFX.
     *
     * @return Odtworzona figura JavaFX gotowa do dodania do drawPane,
     *         lub {@code null} jeśli {@link #type} ma nierozpoznaną wartość.
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
            // Przywracamy wszystkie transformacje
            shape.setTranslateX(translateX);
            shape.setTranslateY(translateY);
            shape.setScaleX(scaleX);
            shape.setScaleY(scaleY);
            shape.setRotate(rotate);

            // Kolor wypełnienia z hex stringa
            shape.setFill(Color.web(hexColor));

            // Zawsze zaczynamy od neutralnego wyglądu obrysu - grubość 1, kolor czarny
            shape.setStroke(Color.BLACK);
            shape.setStrokeWidth(1);
        }

        return shape;
    }
}