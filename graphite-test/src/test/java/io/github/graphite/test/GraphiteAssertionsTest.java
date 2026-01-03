package io.github.graphite.test;

import io.github.graphite.GraphiteError;
import io.github.graphite.GraphiteResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.graphite.test.GraphiteAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GraphiteAssertionsTest {

    @Nested
    class ResponseAssertions {

        @Test
        void hasNoErrorsPassesForEmptyErrors() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThat(response).hasNoErrors();
        }

        @Test
        void hasNoErrorsFailsForErrors() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(error), Map.of());
            assertThrows(AssertionError.class, () -> assertThat(response).hasNoErrors());
        }

        @Test
        void hasErrorsPassesForErrors() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(error), Map.of());
            assertThat(response).hasErrors();
        }

        @Test
        void hasErrorsFailsForNoErrors() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThrows(AssertionError.class, () -> assertThat(response).hasErrors());
        }

        @Test
        void hasErrorCountPassesForCorrectCount() {
            GraphiteError e1 = new GraphiteError("Error 1", null, null, null);
            GraphiteError e2 = new GraphiteError("Error 2", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(e1, e2), Map.of());
            assertThat(response).hasErrorCount(2);
        }

        @Test
        void hasErrorCountFailsForWrongCount() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(error), Map.of());
            assertThrows(AssertionError.class, () -> assertThat(response).hasErrorCount(2));
        }

        @Test
        void hasDataPassesForNonNullData() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThat(response).hasData();
        }

        @Test
        void hasDataFailsForNullData() {
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(), Map.of());
            assertThrows(AssertionError.class, () -> assertThat(response).hasData());
        }

        @Test
        void hasNoDataPassesForNullData() {
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(), Map.of());
            assertThat(response).hasNoData();
        }

        @Test
        void hasNoDataFailsForNonNullData() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThrows(AssertionError.class, () -> assertThat(response).hasNoData());
        }

        @Test
        void hasDataEqualToPassesForEqualData() {
            GraphiteResponse<String> response = new GraphiteResponse<>("hello", List.of(), Map.of());
            assertThat(response).hasDataEqualTo("hello");
        }

        @Test
        void hasDataEqualToFailsForDifferentData() {
            GraphiteResponse<String> response = new GraphiteResponse<>("hello", List.of(), Map.of());
            assertThrows(AssertionError.class, () -> assertThat(response).hasDataEqualTo("world"));
        }

        @Test
        void satisfiesDataPassesForMatchingData() {
            GraphiteResponse<String> response = new GraphiteResponse<>("hello", List.of(), Map.of());
            assertThat(response).satisfiesData(data -> assertEquals("hello", data));
        }

        @Test
        void satisfiesDataFailsForNonMatchingData() {
            GraphiteResponse<String> response = new GraphiteResponse<>("hello", List.of(), Map.of());
            assertThrows(AssertionError.class, () ->
                    assertThat(response).satisfiesData(data -> assertEquals("world", data)));
        }

        @Test
        void errorsReturnsListAssert() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(error), Map.of());
            assertThat(response).errors().hasSize(1);
        }

        @Test
        void hasErrorWithMessagePassesForMatchingMessage() {
            GraphiteError error = new GraphiteError("Not found", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            assertThat(response).hasErrorWithMessage("Not found");
        }

        @Test
        void hasErrorWithMessageFailsForNoMatch() {
            GraphiteError error = new GraphiteError("Not found", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            assertThrows(AssertionError.class, () ->
                    assertThat(response).hasErrorWithMessage("Unauthorized"));
        }

        @Test
        void hasErrorMessageContainingPassesForMatchingText() {
            GraphiteError error = new GraphiteError("User not found", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            assertThat(response).hasErrorMessageContaining("not found");
        }

        @Test
        void hasErrorMessageContainingFailsForNoMatch() {
            GraphiteError error = new GraphiteError("User not found", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            assertThrows(AssertionError.class, () ->
                    assertThat(response).hasErrorMessageContaining("unauthorized"));
        }

        @Test
        void hasErrorWithCodePassesForMatchingCode() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "NOT_FOUND"));
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            assertThat(response).hasErrorWithCode("NOT_FOUND");
        }

        @Test
        void hasErrorWithCodeFailsForNoMatch() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "NOT_FOUND"));
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            assertThrows(AssertionError.class, () ->
                    assertThat(response).hasErrorWithCode("UNAUTHORIZED"));
        }

        @Test
        void hasErrorWithCodeFailsForNullExtensions() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            assertThrows(AssertionError.class, () ->
                    assertThat(response).hasErrorWithCode("NOT_FOUND"));
        }

        @Test
        void hasExtensionsPassesForNonEmptyExtensions() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(),
                    Map.of("traceId", "abc123"));
            assertThat(response).hasExtensions();
        }

        @Test
        void hasExtensionsFailsForEmptyExtensions() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThrows(AssertionError.class, () -> assertThat(response).hasExtensions());
        }

        @Test
        void hasExtensionKeyPassesForExistingKey() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(),
                    Map.of("traceId", "abc123"));
            assertThat(response).hasExtension("traceId");
        }

        @Test
        void hasExtensionKeyFailsForMissingKey() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(),
                    Map.of("traceId", "abc123"));
            assertThrows(AssertionError.class, () -> assertThat(response).hasExtension("duration"));
        }

        @Test
        void hasExtensionKeyValuePassesForMatchingValue() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(),
                    Map.of("duration", 150));
            assertThat(response).hasExtension("duration", 150);
        }

        @Test
        void hasExtensionKeyValueFailsForDifferentValue() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(),
                    Map.of("duration", 150));
            assertThrows(AssertionError.class, () ->
                    assertThat(response).hasExtension("duration", 200));
        }

        @Test
        void isSuccessfulPassesForDataAndNoErrors() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThat(response).isSuccessful();
        }

        @Test
        void isSuccessfulFailsForErrors() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(error), Map.of());
            assertThrows(AssertionError.class, () -> assertThat(response).isSuccessful());
        }

        @Test
        void isSuccessfulFailsForNullData() {
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(), Map.of());
            assertThrows(AssertionError.class, () -> assertThat(response).isSuccessful());
        }

        @Test
        void firstErrorSatisfiesPassesForMatchingError() {
            GraphiteError error = new GraphiteError("First error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            assertThat(response).firstErrorSatisfies(e -> assertEquals("First error", e.message()));
        }

        @Test
        void assertThatResponseRejectsNull() {
            assertThrows(AssertionError.class, () ->
                    assertThat((GraphiteResponse<String>) null).hasNoErrors());
        }

        @Test
        void hasErrorWithMessageRejectsNull() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThrows(NullPointerException.class, () ->
                    assertThat(response).hasErrorWithMessage(null));
        }

        @Test
        void hasErrorMessageContainingRejectsNull() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThrows(NullPointerException.class, () ->
                    assertThat(response).hasErrorMessageContaining(null));
        }

        @Test
        void hasErrorWithCodeRejectsNull() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThrows(NullPointerException.class, () ->
                    assertThat(response).hasErrorWithCode(null));
        }

        @Test
        void hasExtensionRejectsNullKey() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            assertThrows(NullPointerException.class, () ->
                    assertThat(response).hasExtension(null));
        }
    }

    @Nested
    class ErrorAssertions {

        @Test
        void hasMessagePassesForMatchingMessage() {
            GraphiteError error = new GraphiteError("Not found", null, null, null);
            assertThat(error).hasMessage("Not found");
        }

        @Test
        void hasMessageFailsForDifferentMessage() {
            GraphiteError error = new GraphiteError("Not found", null, null, null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasMessage("Unauthorized"));
        }

        @Test
        void hasMessageContainingPassesForMatchingText() {
            GraphiteError error = new GraphiteError("User not found", null, null, null);
            assertThat(error).hasMessageContaining("not found");
        }

        @Test
        void hasMessageContainingFailsForNonMatchingText() {
            GraphiteError error = new GraphiteError("User not found", null, null, null);
            assertThrows(AssertionError.class, () ->
                    assertThat(error).hasMessageContaining("unauthorized"));
        }

        @Test
        void hasLocationsPassesForNonEmptyLocations() {
            GraphiteError.Location loc = new GraphiteError.Location(1, 5);
            GraphiteError error = new GraphiteError("Error", List.of(loc), null, null);
            assertThat(error).hasLocations();
        }

        @Test
        void hasLocationsFailsForEmptyLocations() {
            GraphiteError error = new GraphiteError("Error", List.of(), null, null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasLocations());
        }

        @Test
        void hasLocationsFailsForNullLocations() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasLocations());
        }

        @Test
        void hasNoLocationsPassesForNullLocations() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            assertThat(error).hasNoLocations();
        }

        @Test
        void hasNoLocationsPassesForEmptyLocations() {
            GraphiteError error = new GraphiteError("Error", List.of(), null, null);
            assertThat(error).hasNoLocations();
        }

        @Test
        void hasNoLocationsFailsForNonEmptyLocations() {
            GraphiteError.Location loc = new GraphiteError.Location(1, 5);
            GraphiteError error = new GraphiteError("Error", List.of(loc), null, null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasNoLocations());
        }

        @Test
        void hasLocationAtPassesForMatchingLocation() {
            GraphiteError.Location loc = new GraphiteError.Location(10, 5);
            GraphiteError error = new GraphiteError("Error", List.of(loc), null, null);
            assertThat(error).hasLocationAt(10, 5);
        }

        @Test
        void hasLocationAtFailsForNoMatch() {
            GraphiteError.Location loc = new GraphiteError.Location(10, 5);
            GraphiteError error = new GraphiteError("Error", List.of(loc), null, null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasLocationAt(1, 1));
        }

        @Test
        void hasPathPassesForNonEmptyPath() {
            GraphiteError error = new GraphiteError("Error", null, List.of("user", "name"), null);
            assertThat(error).hasPath();
        }

        @Test
        void hasPathFailsForEmptyPath() {
            GraphiteError error = new GraphiteError("Error", null, List.of(), null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasPath());
        }

        @Test
        void hasPathFailsForNullPath() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasPath());
        }

        @Test
        void hasNoPathPassesForNullPath() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            assertThat(error).hasNoPath();
        }

        @Test
        void hasNoPathPassesForEmptyPath() {
            GraphiteError error = new GraphiteError("Error", null, List.of(), null);
            assertThat(error).hasNoPath();
        }

        @Test
        void hasNoPathFailsForNonEmptyPath() {
            GraphiteError error = new GraphiteError("Error", null, List.of("user"), null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasNoPath());
        }

        @Test
        void hasPathWithElementsPassesForMatchingPath() {
            GraphiteError error = new GraphiteError("Error", null, List.of("user", "name"), null);
            assertThat(error).hasPath("user", "name");
        }

        @Test
        void hasPathWithElementsFailsForDifferentPath() {
            GraphiteError error = new GraphiteError("Error", null, List.of("user", "name"), null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasPath("user", "email"));
        }

        @Test
        void hasPathContainingPassesForMatchingElement() {
            GraphiteError error = new GraphiteError("Error", null, List.of("user", 0, "name"), null);
            assertThat(error).hasPathContaining("user");
        }

        @Test
        void hasPathContainingPassesForIntegerElement() {
            GraphiteError error = new GraphiteError("Error", null, List.of("users", 0, "name"), null);
            assertThat(error).hasPathContaining(0);
        }

        @Test
        void hasPathContainingFailsForNoMatch() {
            GraphiteError error = new GraphiteError("Error", null, List.of("user", "name"), null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasPathContaining("email"));
        }

        @Test
        void hasExtensionsPassesForNonEmptyExtensions() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "NOT_FOUND"));
            assertThat(error).hasExtensions();
        }

        @Test
        void hasExtensionsFailsForEmptyExtensions() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of());
            assertThrows(AssertionError.class, () -> assertThat(error).hasExtensions());
        }

        @Test
        void hasExtensionsFailsForNullExtensions() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            assertThrows(AssertionError.class, () -> assertThat(error).hasExtensions());
        }

        @Test
        void hasNoExtensionsPassesForNullExtensions() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            assertThat(error).hasNoExtensions();
        }

        @Test
        void hasNoExtensionsPassesForEmptyExtensions() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of());
            assertThat(error).hasNoExtensions();
        }

        @Test
        void hasNoExtensionsFailsForNonEmptyExtensions() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "ERROR"));
            assertThrows(AssertionError.class, () -> assertThat(error).hasNoExtensions());
        }

        @Test
        void hasExtensionKeyPassesForExistingKey() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "NOT_FOUND"));
            assertThat(error).hasExtension("code");
        }

        @Test
        void hasExtensionKeyFailsForMissingKey() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "NOT_FOUND"));
            assertThrows(AssertionError.class, () -> assertThat(error).hasExtension("reason"));
        }

        @Test
        void hasExtensionKeyValuePassesForMatchingValue() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "NOT_FOUND"));
            assertThat(error).hasExtension("code", "NOT_FOUND");
        }

        @Test
        void hasExtensionKeyValueFailsForDifferentValue() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "NOT_FOUND"));
            assertThrows(AssertionError.class, () ->
                    assertThat(error).hasExtension("code", "UNAUTHORIZED"));
        }

        @Test
        void hasCodePassesForMatchingCode() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "NOT_FOUND"));
            assertThat(error).hasCode("NOT_FOUND");
        }

        @Test
        void hasCodeFailsForDifferentCode() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "NOT_FOUND"));
            assertThrows(AssertionError.class, () -> assertThat(error).hasCode("UNAUTHORIZED"));
        }

        @Test
        void assertThatErrorRejectsNull() {
            assertThrows(AssertionError.class, () ->
                    assertThat((GraphiteError) null).hasMessage("test"));
        }

        @Test
        void hasMessageRejectsNull() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            assertThrows(NullPointerException.class, () -> assertThat(error).hasMessage(null));
        }

        @Test
        void hasMessageContainingRejectsNull() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            assertThrows(NullPointerException.class, () ->
                    assertThat(error).hasMessageContaining(null));
        }

        @Test
        void hasExtensionRejectsNullKey() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of());
            assertThrows(NullPointerException.class, () ->
                    assertThat(error).hasExtension(null));
        }
    }

    @Nested
    class FailureMessages {

        @Test
        void hasNoErrorsIncludesErrorsInMessage() {
            GraphiteError error = new GraphiteError("Test error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(error), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(response).hasNoErrors());
            assertTrue(e.getMessage().contains("Test error"));
        }

        @Test
        void hasErrorsIncludesNoErrorsMessage() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(response).hasErrors());
            assertTrue(e.getMessage().contains("no errors") || e.getMessage().contains("none"));
        }

        @Test
        void hasErrorCountIncludesActualCount() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(error), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(response).hasErrorCount(5));
            assertTrue(e.getMessage().contains("1") || e.getMessage().contains("5"));
        }

        @Test
        void hasDataIncludesNullMessage() {
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(response).hasData());
            assertTrue(e.getMessage().contains("null") || e.getMessage().contains("data"));
        }

        @Test
        void hasNoDataIncludesActualData() {
            GraphiteResponse<String> response = new GraphiteResponse<>("mydata", List.of(), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(response).hasNoData());
            assertTrue(e.getMessage().contains("mydata"));
        }

        @Test
        void hasDataEqualToIncludesBothValues() {
            GraphiteResponse<String> response = new GraphiteResponse<>("actual", List.of(), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(response).hasDataEqualTo("expected"));
            assertTrue(e.getMessage().contains("actual") && e.getMessage().contains("expected"));
        }

        @Test
        void hasErrorWithMessageIncludesSearchedMessage() {
            GraphiteError error = new GraphiteError("Different error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () ->
                    assertThat(response).hasErrorWithMessage("Not found"));
            assertTrue(e.getMessage().contains("Not found"));
        }

        @Test
        void hasErrorMessageContainingIncludesSearchedText() {
            GraphiteError error = new GraphiteError("Different error", null, null, null);
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () ->
                    assertThat(response).hasErrorMessageContaining("not found"));
            assertTrue(e.getMessage().contains("not found"));
        }

        @Test
        void hasErrorWithCodeIncludesSearchedCode() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "OTHER"));
            GraphiteResponse<String> response = new GraphiteResponse<>(null, List.of(error), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () ->
                    assertThat(response).hasErrorWithCode("NOT_FOUND"));
            assertTrue(e.getMessage().contains("NOT_FOUND"));
        }

        @Test
        void hasExtensionsIncludesNoExtensionsMessage() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(), Map.of());
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(response).hasExtensions());
            assertTrue(e.getMessage().contains("extension") || e.getMessage().contains("none"));
        }

        @Test
        void hasExtensionKeyIncludesSearchedKey() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(),
                    Map.of("other", "value"));
            AssertionError e = assertThrows(AssertionError.class, () ->
                    assertThat(response).hasExtension("missing"));
            assertTrue(e.getMessage().contains("missing"));
        }

        @Test
        void hasExtensionValueIncludesExpectedAndActual() {
            GraphiteResponse<String> response = new GraphiteResponse<>("data", List.of(),
                    Map.of("key", "actual"));
            AssertionError e = assertThrows(AssertionError.class, () ->
                    assertThat(response).hasExtension("key", "expected"));
            assertTrue(e.getMessage().contains("expected") && e.getMessage().contains("actual"));
        }

        @Test
        void errorHasMessageIncludesActualMessage() {
            GraphiteError error = new GraphiteError("Actual message", null, null, null);
            AssertionError e = assertThrows(AssertionError.class, () ->
                    assertThat(error).hasMessage("Expected message"));
            assertTrue(e.getMessage().contains("Actual message") && e.getMessage().contains("Expected message"));
        }

        @Test
        void errorHasMessageContainingIncludesSearchedText() {
            GraphiteError error = new GraphiteError("Some error", null, null, null);
            AssertionError e = assertThrows(AssertionError.class, () ->
                    assertThat(error).hasMessageContaining("not found"));
            assertTrue(e.getMessage().contains("not found"));
        }

        @Test
        void errorHasLocationsIncludesNoLocationsMessage() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasLocations());
            assertTrue(e.getMessage().contains("location") || e.getMessage().contains("none"));
        }

        @Test
        void errorHasNoLocationsIncludesActualLocations() {
            GraphiteError.Location loc = new GraphiteError.Location(1, 5);
            GraphiteError error = new GraphiteError("Error", List.of(loc), null, null);
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasNoLocations());
            assertTrue(e.getMessage().contains("1") || e.getMessage().contains("location"));
        }

        @Test
        void errorHasLocationAtIncludesSearchedLocation() {
            GraphiteError.Location loc = new GraphiteError.Location(10, 5);
            GraphiteError error = new GraphiteError("Error", List.of(loc), null, null);
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasLocationAt(1, 1));
            assertTrue(e.getMessage().contains("1"));
        }

        @Test
        void errorHasPathIncludesNoPathMessage() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasPath());
            assertTrue(e.getMessage().contains("path") || e.getMessage().contains("none"));
        }

        @Test
        void errorHasNoPathIncludesActualPath() {
            GraphiteError error = new GraphiteError("Error", null, List.of("user"), null);
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasNoPath());
            assertTrue(e.getMessage().contains("user") || e.getMessage().contains("path"));
        }

        @Test
        void errorHasPathWithElementsIncludesBothPaths() {
            GraphiteError error = new GraphiteError("Error", null, List.of("user", "name"), null);
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasPath("user", "email"));
            assertTrue(e.getMessage().contains("name") || e.getMessage().contains("email"));
        }

        @Test
        void errorHasPathContainingIncludesSearchedElement() {
            GraphiteError error = new GraphiteError("Error", null, List.of("user"), null);
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasPathContaining("email"));
            assertTrue(e.getMessage().contains("email"));
        }

        @Test
        void errorHasExtensionsIncludesNoExtensionsMessage() {
            GraphiteError error = new GraphiteError("Error", null, null, null);
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasExtensions());
            assertTrue(e.getMessage().contains("extension") || e.getMessage().contains("none"));
        }

        @Test
        void errorHasNoExtensionsIncludesActualExtensions() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("code", "ERROR"));
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasNoExtensions());
            assertTrue(e.getMessage().contains("code") || e.getMessage().contains("ERROR"));
        }

        @Test
        void errorHasExtensionKeyIncludesSearchedKey() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("other", "value"));
            AssertionError e = assertThrows(AssertionError.class, () -> assertThat(error).hasExtension("missing"));
            assertTrue(e.getMessage().contains("missing"));
        }

        @Test
        void errorHasExtensionValueIncludesExpectedAndActual() {
            GraphiteError error = new GraphiteError("Error", null, null, Map.of("key", "actual"));
            AssertionError e = assertThrows(AssertionError.class, () ->
                    assertThat(error).hasExtension("key", "expected"));
            assertTrue(e.getMessage().contains("expected") && e.getMessage().contains("actual"));
        }
    }

    @Test
    void chainingWorks() {
        GraphiteError error = new GraphiteError("Not found",
                List.of(new GraphiteError.Location(1, 5)),
                List.of("user"),
                Map.of("code", "NOT_FOUND"));
        GraphiteResponse<String> response = new GraphiteResponse<>("partial", List.of(error),
                Map.of("traceId", "abc123"));

        assertThat(response)
                .hasData()
                .hasErrors()
                .hasErrorCount(1)
                .hasErrorWithMessage("Not found")
                .hasErrorWithCode("NOT_FOUND")
                .hasExtension("traceId", "abc123");

        assertThat(error)
                .hasMessage("Not found")
                .hasCode("NOT_FOUND")
                .hasLocations()
                .hasLocationAt(1, 5)
                .hasPath("user");
    }
}
