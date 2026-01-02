package io.github.graphite.scalar;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BuiltInScalarsTest {

    @Nested
    class DateTimeCodecTest {
        @Test
        void hasCorrectTypeName() {
            assertEquals("DateTime", BuiltInScalars.DATE_TIME.getGraphQLTypeName());
        }

        @Test
        void hasCorrectJavaType() {
            assertEquals(OffsetDateTime.class, BuiltInScalars.DATE_TIME.getJavaType());
        }

        @Test
        void serializesDateTime() {
            OffsetDateTime dt = OffsetDateTime.of(2024, 6, 15, 10, 30, 0, 0, ZoneOffset.UTC);
            assertEquals("2024-06-15T10:30:00Z", BuiltInScalars.DATE_TIME.serialize(dt));
        }

        @Test
        void deserializesDateTime() {
            OffsetDateTime result = BuiltInScalars.DATE_TIME.deserialize("2024-06-15T10:30:00Z");
            assertEquals(2024, result.getYear());
            assertEquals(6, result.getMonthValue());
            assertEquals(15, result.getDayOfMonth());
            assertEquals(10, result.getHour());
            assertEquals(30, result.getMinute());
        }

        @Test
        void handlesNull() {
            assertNull(BuiltInScalars.DATE_TIME.serialize(null));
            assertNull(BuiltInScalars.DATE_TIME.deserialize(null));
        }
    }

    @Nested
    class DateCodecTest {
        @Test
        void hasCorrectTypeName() {
            assertEquals("Date", BuiltInScalars.DATE.getGraphQLTypeName());
        }

        @Test
        void hasCorrectJavaType() {
            assertEquals(LocalDate.class, BuiltInScalars.DATE.getJavaType());
        }

        @Test
        void serializesDate() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            assertEquals("2024-06-15", BuiltInScalars.DATE.serialize(date));
        }

        @Test
        void deserializesDate() {
            LocalDate result = BuiltInScalars.DATE.deserialize("2024-06-15");
            assertEquals(2024, result.getYear());
            assertEquals(6, result.getMonthValue());
            assertEquals(15, result.getDayOfMonth());
        }

        @Test
        void handlesNull() {
            assertNull(BuiltInScalars.DATE.serialize(null));
            assertNull(BuiltInScalars.DATE.deserialize(null));
        }
    }

    @Nested
    class TimeCodecTest {
        @Test
        void hasCorrectTypeName() {
            assertEquals("Time", BuiltInScalars.TIME.getGraphQLTypeName());
        }

        @Test
        void hasCorrectJavaType() {
            assertEquals(LocalTime.class, BuiltInScalars.TIME.getJavaType());
        }

        @Test
        void serializesTime() {
            LocalTime time = LocalTime.of(10, 30, 45);
            assertEquals("10:30:45", BuiltInScalars.TIME.serialize(time));
        }

        @Test
        void deserializesTime() {
            LocalTime result = BuiltInScalars.TIME.deserialize("10:30:45");
            assertEquals(10, result.getHour());
            assertEquals(30, result.getMinute());
            assertEquals(45, result.getSecond());
        }

        @Test
        void handlesNull() {
            assertNull(BuiltInScalars.TIME.serialize(null));
            assertNull(BuiltInScalars.TIME.deserialize(null));
        }
    }

    @Nested
    class UuidCodecTest {
        @Test
        void hasCorrectTypeName() {
            assertEquals("UUID", BuiltInScalars.UUID_CODEC.getGraphQLTypeName());
        }

        @Test
        void hasCorrectJavaType() {
            assertEquals(UUID.class, BuiltInScalars.UUID_CODEC.getJavaType());
        }

        @Test
        void serializesUuid() {
            UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            assertEquals("550e8400-e29b-41d4-a716-446655440000", BuiltInScalars.UUID_CODEC.serialize(uuid));
        }

        @Test
        void deserializesUuid() {
            UUID result = BuiltInScalars.UUID_CODEC.deserialize("550e8400-e29b-41d4-a716-446655440000");
            assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), result);
        }

        @Test
        void handlesNull() {
            assertNull(BuiltInScalars.UUID_CODEC.serialize(null));
            assertNull(BuiltInScalars.UUID_CODEC.deserialize(null));
        }
    }

    @Nested
    class BigDecimalCodecTest {
        @Test
        void hasCorrectTypeName() {
            assertEquals("BigDecimal", BuiltInScalars.BIG_DECIMAL.getGraphQLTypeName());
        }

        @Test
        void hasCorrectJavaType() {
            assertEquals(BigDecimal.class, BuiltInScalars.BIG_DECIMAL.getJavaType());
        }

        @Test
        void serializesBigDecimal() {
            BigDecimal value = new BigDecimal("123.456");
            assertEquals("123.456", BuiltInScalars.BIG_DECIMAL.serialize(value));
        }

        @Test
        void deserializesBigDecimalFromString() {
            BigDecimal result = BuiltInScalars.BIG_DECIMAL.deserialize("123.456");
            assertEquals(new BigDecimal("123.456"), result);
        }

        @Test
        void deserializesBigDecimalFromNumber() {
            BigDecimal result = BuiltInScalars.BIG_DECIMAL.deserialize(123.456);
            assertEquals(123.456, result.doubleValue(), 0.001);
        }

        @Test
        void deserializesBigDecimalFromBigDecimal() {
            BigDecimal input = new BigDecimal("123.456");
            BigDecimal result = BuiltInScalars.BIG_DECIMAL.deserialize(input);
            assertSame(input, result);
        }

        @Test
        void handlesNull() {
            assertNull(BuiltInScalars.BIG_DECIMAL.serialize(null));
            assertNull(BuiltInScalars.BIG_DECIMAL.deserialize(null));
        }
    }

    @Nested
    class LongCodecTest {
        @Test
        void hasCorrectTypeName() {
            assertEquals("Long", BuiltInScalars.LONG.getGraphQLTypeName());
        }

        @Test
        void hasCorrectJavaType() {
            assertEquals(Long.class, BuiltInScalars.LONG.getJavaType());
        }

        @Test
        void serializesLong() {
            assertEquals(123L, BuiltInScalars.LONG.serialize(123L));
        }

        @Test
        void deserializesLongFromString() {
            assertEquals(123L, BuiltInScalars.LONG.deserialize("123"));
        }

        @Test
        void deserializesLongFromNumber() {
            assertEquals(123L, BuiltInScalars.LONG.deserialize(123));
        }

        @Test
        void deserializesLongFromLong() {
            assertEquals(123L, BuiltInScalars.LONG.deserialize(123L));
        }

        @Test
        void handlesNull() {
            assertNull(BuiltInScalars.LONG.serialize(null));
            assertNull(BuiltInScalars.LONG.deserialize(null));
        }
    }

    @Nested
    class UrlCodecTest {
        @Test
        void hasCorrectTypeName() {
            assertEquals("URL", BuiltInScalars.URL.getGraphQLTypeName());
        }

        @Test
        void hasCorrectJavaType() {
            assertEquals(URI.class, BuiltInScalars.URL.getJavaType());
        }

        @Test
        void serializesUrl() {
            URI uri = URI.create("https://example.com/path");
            assertEquals("https://example.com/path", BuiltInScalars.URL.serialize(uri));
        }

        @Test
        void deserializesUrl() {
            URI result = BuiltInScalars.URL.deserialize("https://example.com/path");
            assertEquals(URI.create("https://example.com/path"), result);
        }

        @Test
        void handlesNull() {
            assertNull(BuiltInScalars.URL.serialize(null));
            assertNull(BuiltInScalars.URL.deserialize(null));
        }
    }
}
