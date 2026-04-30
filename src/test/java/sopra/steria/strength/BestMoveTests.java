package sopra.steria.strength;

import com.fasterxml.jackson.databind.ObjectMapper;
import knight.clubbing.core.BBoard;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sopra.steria.search.Search;
import sopra.steria.search.SearchResult;
import sopra.steria.search.SearchSetting;

import java.io.InputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("strength")
class BestMoveTests {

    static Stream<BestmovePuzzle> providePuzzles() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = BestMoveTests.class
                .getResourceAsStream("/bestmove-puzzles.json");

        assertNotNull(inputStream, "Could not find bestmove-puzzles.json");

        BestmovePuzzleContainer container = mapper.readValue(inputStream, BestmovePuzzleContainer.class);
        return container.getPuzzles().stream();
    }

    @ParameterizedTest
    @MethodSource("providePuzzles")
    void testBestMoveInPuzzle(BestmovePuzzle puzzle) {
        // Arrange
        BBoard board = new BBoard(puzzle.getFen());
        Search search = new Search();

        // Act
        SearchResult result = search.bestMove(board, new SearchSetting(5, 1000));

        // Assert
        assertNotNull(result, "Search should return a result");
        assertNotNull(result.getBestMove(), "Search should return a best move");
        assertFalse(result.getBestMove().isEmpty(), "Best move should not be empty");

        String description = puzzle.getDescription() != null
                ? puzzle.getDescription()
                : "No description provided";

        assertTrue(
                puzzle.getBestMoves().contains(result.getBestMove()),
                String.format(
                        "[%s] Expected best move to be one of %s for position '%s', but got '%s'",
                        description,
                        puzzle.getBestMoves(),
                        puzzle.getFen(),
                        result.getBestMove()
                )
        );
    }
}