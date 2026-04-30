package sopra.steria;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class UciSanityTest {

    private Uci uci;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        uci = new Uci(false);
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    /**
     * Requirement of (baseline) engine: UCI base should work, including uci
     */
    @Test
    void testUciCommand() {
        // Act
        uci.handleCommand("uci");
        String output = outContent.toString();

        assertTrue(output.contains("id name"), "Engine should identify itself");
        assertTrue(output.contains("uciok"), "Engine should respond with uciok");
    }

    /**
     * Requirement of (baseline) engine: UCI base should work, including isReady
     */
    @Test
    void testIsReadyCommand() {
        uci.handleCommand("isready");
        String output = outContent.toString();

        assertTrue(output.contains("readyok"), "Engine should respond with readyok");
    }

    /**
     * Requirement of (baseline) engine: UCI base should work, including position startpos
     */
    @Test
    void testPositionStartpos() {
        uci.handleCommand("position startpos");

        assertNotNull(uci.getBoard(), "Board should be initialized");
        assertTrue(uci.getBoard().isWhiteToMove(), "White should be to move in startpos");
    }

    /**
     * Requirement of (baseline) engine: UCI base should work, including position startpos with moves
     */
    @Test
    void testPositionWithMoves() {
        uci.handleCommand("position startpos moves e2e4 e7e5");

        assertNotNull(uci.getBoard(), "Board should be initialized");
        assertTrue(uci.getBoard().isWhiteToMove(), "White should be to move after black's move");
    }

    /**
     * Requirement of (baseline) engine: UCI base should work, including position fen
     */
    @Test
    void testPositionFen() {
        uci.handleCommand("position fen rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");

        assertNotNull(uci.getBoard(), "Board should be initialized from FEN");
        assertFalse(uci.getBoard().isWhiteToMove(), "Black should be to move");
    }

    /**
     * Requirement of (baseline) engine: UCI should work with standard procedure
     */
    @Test
    void testStartProcedures() {
        uci.handleCommand("uci");
        uci.handleCommand("isready");
        uci.handleCommand("position startpos moves e2e4 e7e5 g1f3");

        String output = outContent.toString();

        assertTrue(output.contains("id name"), "Engine should identify itself");
        assertTrue(output.contains("uciok"), "Engine should respond with uciok");
        assertTrue(output.contains("readyok"), "Engine should respond with readyok");
        assertNotNull(uci.getBoard(), "Board should be initialized");
        assertFalse(uci.getBoard().isWhiteToMove(), "Black should be to move");
    }

    /**
     * Requirement of (baseline) engine: UCI should work with standard procedure including go
     */
    @Test
    @Tag(TestTags.UNRELIABLE)
    void testStartProcedure_withGo() {
        uci.handleCommand("uci");
        uci.handleCommand("isready");
        uci.handleCommand("position startpos moves e2e4 e7e5 g1f3");

        long startTime = System.currentTimeMillis();
        uci.handleCommand("go depth 1");

        try {
            await()
                    .atMost(Duration.ofSeconds(5))
                    .pollInterval(Duration.ofMillis(50))
                    .untilAsserted(() -> {
                        String output = outContent.toString();
                        assertTrue(output.contains("info depth"),
                                "Missing 'info depth'. Current output:\n" + output);
                        assertTrue(output.contains("bestmove"),
                                "Missing 'bestmove'. Current output:\n" + output);
                    });
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            String currentOutput = outContent.toString();

            System.err.println("=== TEST FAILURE DEBUG INFO ===");
            System.err.println("Time elapsed: " + elapsed + "ms");
            System.err.println("Current output length: " + currentOutput.length());
            System.err.println("Full output:\n" + currentOutput);
            System.err.println("Board state: " + (uci.getBoard() != null ? uci.getBoard().exportFen() : "null"));
            System.err.println("=== THREAD STATE ===");
            Thread.getAllStackTraces().forEach((thread, stackTrace) -> {
                if (thread.getName().contains("Thread") || thread.getName().contains("search")) {
                    System.err.println("Thread: " + thread.getName() + " - State: " + thread.getState());
                    for (StackTraceElement element : stackTrace) {
                        System.err.println("  at " + element);
                    }
                }
            });
            System.err.println("================================");

            throw e;
        }

        String output = outContent.toString();
        assertTrue(output.contains("id name"), "Engine should identify itself");
        assertTrue(output.contains("uciok"), "Engine should respond with uciok");
        assertTrue(output.contains("readyok"), "Engine should respond with readyok");
        assertNotNull(uci.getBoard(), "Board should be initialized");
        assertFalse(uci.getBoard().isWhiteToMove(), "Black should be to move");
        assertTrue(output.contains("info depth"), "Expected \"info depth\", but got:\n" + output);
        assertTrue(output.contains("bestmove"), "Expected \"bestmove\", but got:\n" + output);
    }


    /**
     * Requirement of (baseline) engine: Should respect time control
     */
    @Test
    @Tag(TestTags.UNRELIABLE)
    void testTimeControl() {
        // Test white to move with normal time
        uci.handleCommand("position startpos");
        long startTime = System.currentTimeMillis();
        uci.handleCommand("go wtime 3000 btime 3000 winc 100 binc 100");

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(50))
                .untilAsserted(() -> {
                    String output = outContent.toString();
                    assertTrue(output.contains("bestmove"));
                });

        long elapsedTime = System.currentTimeMillis() - startTime;
        assertTrue(elapsedTime < 2000, "Engine should respect time limit, used " + elapsedTime + "ms");

        String output = outContent.toString();
        assertTrue(output.contains("bestmove"), "Engine should return a move");
        assertFalse(output.contains("bestmove 0000"), "Engine should not return null move");
    }

}