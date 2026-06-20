# Symulacja kolorów — Lista nr 6 (Kurs programowania 2026)

Wielowątkowa symulacja planszy *n × m* napisana w Javie (Swing). Każde pole planszy
jest osobnym wątkiem, który co losowy czas zmienia swój kolor.

## Zrealizowana funkcjonalność

### Ocena 3.0
- Plansza to prostokąt *n × m* pól.
- Parametry `n`, `m`, `k` (szybkość), `p` (prawdopodobieństwo) podawane jako argumenty
  wiersza poleceń lub przez pola tekstowe w GUI. Kolory początkowe są losowe.
- Każde pole to wątek, który co losowy czas z przedziału `[0.5k, 1.5k]` ms:
  - z prawdopodobieństwem `p` zmienia kolor na losowy,
  - z prawdopodobieństwem `1-p` zostawia kolor (zachowanie rozszerzone w 4.0).

### Ocena 4.0
- Z prawdopodobieństwem `1-p` pole przyjmuje **średnią RGB czterech sąsiadów**,
  traktując planszę jako **torus 2D** (`computeNeighborAverage`).
- Synchronizacja przez wspólny `globalLock` w `SimulationPanel` — sekcje krytyczne
  wątków są serializowane, więc nie dochodzi do odczytu koloru sąsiada w trakcie jego
  zmiany. Każdy wątek wypisuje nieprzeplatane pary `Start: X` / `End: X`.

### Ocena 5.0
- **Zawieszanie / wznawianie** wątku kliknięciem na pole (`suspendThread` /
  `resumeThread`, mechanizm `wait()` / `notifyAll()`). Zawieszone pole jest pomijane
  przy obliczaniu średniej kolorów sąsiadów.
- **Dynamiczne dodawanie wiersza i kolumny** z nowymi, od razu działającymi wątkami
  (`addRow`, `addColumn`) — operacja pod `globalLock`.

## Architektura

| Plik                   | Rola                                                                 |
|------------------------|---------------------------------------------------------------------|
| `ColorSimulation.java` | Okno aplikacji (`JFrame`), panel parametrów, punkt wejścia `main`.   |
| `SimulationPanel.java` | Panel siatki: tworzenie/uruchamianie wątków, rysowanie, obsługa myszy, jedyny generator `Random`, `globalLock`. |
| `CellThread.java`      | Wątek pojedynczego pola: pętla, losowanie koloru, średnia sąsiadów, zawieszanie. |

Jeden generator liczb pseudolosowych (`SimulationPanel.random`, klasa
`java.util.Random`) używany przez całą aplikację — metody `nextDouble()` i
`nextInt(int)`.

## Kompilacja

```bash
javac -d . CellThread.java ColorSimulation.java SimulationPanel.java
```

Kod kompiluje się **bez ostrzeżeń** (`javac -Xlint:all`).

## Uruchomienie

Z parametrami (`n m k p`), symulacja startuje automatycznie:

```bash
java ColorSimulation 10 10 500 0.3
```

Bez parametrów (wartości wpisuje się w GUI i klika **Start**):

```bash
java ColorSimulation
```

**Obsługa:** klik na pole = zawieś/wznów jego wątek; przyciski **Dodaj wiersz** /
**Dodaj kolumnę** rozszerzają planszę w trakcie działania; **Stop** zatrzymuje wątki.

## Generowanie dokumentacji

javadoc (do katalogu `docs/`, przechodzi `-Xdoclint:all` bez błędów):

```bash
javadoc -encoding UTF-8 -charset UTF-8 -private -author -version -d docs \
        CellThread.java ColorSimulation.java SimulationPanel.java
```

doxygen (do katalogu `doxygen-docs/html/`, konfiguracja w `Doxyfile`):

```bash
doxygen Doxyfile
```

> Uwaga: doxygen zgłasza kilka ostrzeżeń „unable to resolve link" dla klas
> standardowej biblioteki JDK (`SwingUtilities`, `JScrollPane`, `Thread#join`) —
> wynika to z braku źródeł JDK i nie wpływa na treść dokumentacji.
