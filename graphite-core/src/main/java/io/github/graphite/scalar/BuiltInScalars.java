package io.github.graphite.scalar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Built-in scalar codecs for common GraphQL scalar types.
 *
 * <p>Provides codecs for:</p>
 * <ul>
 *   <li>{@code DateTime} - {@link OffsetDateTime}</li>
 *   <li>{@code Date} - {@link LocalDate}</li>
 *   <li>{@code Time} - {@link LocalTime}</li>
 *   <li>{@code UUID} - {@link UUID}</li>
 *   <li>{@code BigDecimal} - {@link BigDecimal}</li>
 *   <li>{@code Long} - {@link Long}</li>
 *   <li>{@code URL} - {@link URI}</li>
 * </ul>
 */
public final class BuiltInScalars {

    private BuiltInScalars() {
        // Utility class
    }

    /**
     * Codec for DateTime scalar using ISO-8601 format.
     */
    public static final ScalarCodec<OffsetDateTime> DATE_TIME = new ScalarCodec<>() {
        @Override
        @NotNull
        public String getGraphQLTypeName() {
            return "DateTime";
        }

        @Override
        @NotNull
        public Class<OffsetDateTime> getJavaType() {
            return OffsetDateTime.class;
        }

        @Override
        @Nullable
        public Object serialize(@Nullable OffsetDateTime value) {
            return value == null ? null : value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        @Override
        @Nullable
        public OffsetDateTime deserialize(@Nullable Object value) {
            if (value == null) {
                return null;
            }
            return OffsetDateTime.parse(value.toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    };

    /**
     * Codec for Date scalar using ISO-8601 format (yyyy-MM-dd).
     */
    public static final ScalarCodec<LocalDate> DATE = new ScalarCodec<>() {
        @Override
        @NotNull
        public String getGraphQLTypeName() {
            return "Date";
        }

        @Override
        @NotNull
        public Class<LocalDate> getJavaType() {
            return LocalDate.class;
        }

        @Override
        @Nullable
        public Object serialize(@Nullable LocalDate value) {
            return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        @Override
        @Nullable
        public LocalDate deserialize(@Nullable Object value) {
            if (value == null) {
                return null;
            }
            return LocalDate.parse(value.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
        }
    };

    /**
     * Codec for Time scalar using ISO-8601 format (HH:mm:ss).
     */
    public static final ScalarCodec<LocalTime> TIME = new ScalarCodec<>() {
        @Override
        @NotNull
        public String getGraphQLTypeName() {
            return "Time";
        }

        @Override
        @NotNull
        public Class<LocalTime> getJavaType() {
            return LocalTime.class;
        }

        @Override
        @Nullable
        public Object serialize(@Nullable LocalTime value) {
            return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_TIME);
        }

        @Override
        @Nullable
        public LocalTime deserialize(@Nullable Object value) {
            if (value == null) {
                return null;
            }
            return LocalTime.parse(value.toString(), DateTimeFormatter.ISO_LOCAL_TIME);
        }
    };

    /**
     * Codec for UUID scalar.
     */
    public static final ScalarCodec<UUID> UUID_CODEC = new ScalarCodec<>() {
        @Override
        @NotNull
        public String getGraphQLTypeName() {
            return "UUID";
        }

        @Override
        @NotNull
        public Class<UUID> getJavaType() {
            return UUID.class;
        }

        @Override
        @Nullable
        public Object serialize(@Nullable UUID value) {
            return value == null ? null : value.toString();
        }

        @Override
        @Nullable
        public UUID deserialize(@Nullable Object value) {
            if (value == null) {
                return null;
            }
            return UUID.fromString(value.toString());
        }
    };

    /**
     * Codec for BigDecimal scalar.
     */
    public static final ScalarCodec<BigDecimal> BIG_DECIMAL = new ScalarCodec<>() {
        @Override
        @NotNull
        public String getGraphQLTypeName() {
            return "BigDecimal";
        }

        @Override
        @NotNull
        public Class<BigDecimal> getJavaType() {
            return BigDecimal.class;
        }

        @Override
        @Nullable
        public Object serialize(@Nullable BigDecimal value) {
            return value == null ? null : value.toString();
        }

        @Override
        @Nullable
        public BigDecimal deserialize(@Nullable Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof BigDecimal bd) {
                return bd;
            }
            if (value instanceof Number n) {
                return BigDecimal.valueOf(n.doubleValue());
            }
            return new BigDecimal(value.toString());
        }
    };

    /**
     * Codec for Long scalar.
     */
    public static final ScalarCodec<Long> LONG = new ScalarCodec<>() {
        @Override
        @NotNull
        public String getGraphQLTypeName() {
            return "Long";
        }

        @Override
        @NotNull
        public Class<Long> getJavaType() {
            return Long.class;
        }

        @Override
        @Nullable
        public Object serialize(@Nullable Long value) {
            return value;
        }

        @Override
        @Nullable
        public Long deserialize(@Nullable Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Long l) {
                return l;
            }
            if (value instanceof Number n) {
                return n.longValue();
            }
            return Long.parseLong(value.toString());
        }
    };

    /**
     * Codec for URL scalar using Java's URI class.
     */
    public static final ScalarCodec<URI> URL = new ScalarCodec<>() {
        @Override
        @NotNull
        public String getGraphQLTypeName() {
            return "URL";
        }

        @Override
        @NotNull
        public Class<URI> getJavaType() {
            return URI.class;
        }

        @Override
        @Nullable
        public Object serialize(@Nullable URI value) {
            return value == null ? null : value.toString();
        }

        @Override
        @Nullable
        public URI deserialize(@Nullable Object value) {
            if (value == null) {
                return null;
            }
            return URI.create(value.toString());
        }
    };
}
