/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.utils;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static software.amazon.awssdk.utils.DateUtils.ALTERNATE_ISO_8601_DATE_FORMAT;
import static software.amazon.awssdk.utils.DateUtils.RFC_822_DATE_TIME;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class DateUtilsTest {
    private static final boolean DEBUG = false;
    private static final int MAX_MILLIS_YEAR = 292278994;
    private static final SimpleDateFormat COMMON_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final SimpleDateFormat LONG_DATE_FORMAT =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    static {
        COMMON_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(UTC));
        LONG_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(UTC));
    }

    private static final Instant INSTANT = Instant.ofEpochMilli(1400284606000L);

    @Test
    public void tt0031561767() {
        String input = "Fri, 16 May 2014 23:56:46 GMT";
        Instant instant = DateUtils.parseRfc1123Date(input);
        assertEquals(input, DateUtils.formatRfc1123Date(instant));
    }

    @Test
    public void formatIso8601Date() throws ParseException {
        Date date = Date.from(INSTANT);
        String expected = COMMON_DATE_FORMAT.format(date);
        String actual = DateUtils.formatIso8601Date(date.toInstant());
        assertEquals(expected, actual);

        Instant expectedDate = COMMON_DATE_FORMAT.parse(expected).toInstant();
        Instant actualDate = DateUtils.parseIso8601Date(actual);
        assertEquals(expectedDate, actualDate);
    }

    @Test
    public void formatRfc822Date_DateWithTwoDigitDayOfMonth_ReturnsFormattedString() throws ParseException {
        String string = DateUtils.formatRfc822Date(INSTANT);
        Instant parsedDateAsInstant = LONG_DATE_FORMAT.parse(string).toInstant();
        assertThat(parsedDateAsInstant).isEqualTo(INSTANT);
    }

    @Test
    public void formatRfc822Date_DateWithSingleDigitDayOfMonth_ReturnsFormattedString() throws ParseException {
        Instant INSTANT_SINGLE_DIGIT_DAY_OF_MONTH = Instant.ofEpochMilli(1399484606000L);;
        String string = DateUtils.formatRfc822Date(INSTANT_SINGLE_DIGIT_DAY_OF_MONTH);
        Instant parsedDateAsInstant = LONG_DATE_FORMAT.parse(string).toInstant();
        assertThat(parsedDateAsInstant).isEqualTo(INSTANT_SINGLE_DIGIT_DAY_OF_MONTH);
    }

    @Test
    public void formatRfc822Date_DateWithSingleDigitDayOfMonth_ReturnsStringWithZeroLeadingDayOfMonth() throws ParseException {
        final Instant INSTANT_SINGLE_DIGIT_DAY_OF_MONTH = Instant.ofEpochMilli(1399484606000L);;
        String string = DateUtils.formatRfc822Date(INSTANT_SINGLE_DIGIT_DAY_OF_MONTH);
        String expectedString = "Wed, 07 May 2014 17:43:26 GMT";
        assertThat(string).isEqualTo(expectedString);
    }

    @Test
    public void parseRfc822Date_DateWithTwoDigitDayOfMonth_ReturnsInstantObject() throws ParseException {
        String formattedDate = LONG_DATE_FORMAT.format(Date.from(INSTANT));
        Instant parsedInstant = DateUtils.parseRfc822Date(formattedDate);
        assertThat(parsedInstant).isEqualTo(INSTANT);
    }

    @Test
    public void parseRfc822Date_DateWithSingleDigitDayOfMonth_ReturnsInstantObject() throws ParseException {
        final Instant INSTANT_SINGLE_DIGIT_DAY_OF_MONTH = Instant.ofEpochMilli(1399484606000L);;
        String formattedDate = LONG_DATE_FORMAT.format(Date.from(INSTANT_SINGLE_DIGIT_DAY_OF_MONTH));
        Instant parsedInstant = DateUtils.parseRfc822Date(formattedDate);
        assertThat(parsedInstant).isEqualTo(INSTANT_SINGLE_DIGIT_DAY_OF_MONTH);
    }

    @Test
    public void parseRfc822Date_DateWithInvalidDayOfMonth_IsParsedWithSmartResolverStyle() {
        String badDateString = "Wed, 31 Apr 2014 17:43:26 GMT";
        String validDateString = "Wed, 30 Apr 2014 17:43:26 GMT";
        Instant badDateParsedInstant = DateUtils.parseRfc822Date(badDateString);
        Instant validDateParsedInstant = DateUtils.parseRfc1123Date(validDateString);
        assertThat(badDateParsedInstant).isEqualTo(validDateParsedInstant);
    }

    @Test
    public void parseRfc822Date_DateWithInvalidDayOfMonth_MatchesRfc1123Behavior() {
        String dateString = "Wed, 31 Apr 2014 17:43:26 GMT";
        Instant parsedInstantFromRfc822Parser = DateUtils.parseRfc822Date(dateString);
        Instant parsedInstantFromRfc1123arser = DateUtils.parseRfc1123Date(dateString);
        assertThat(parsedInstantFromRfc822Parser).isEqualTo(parsedInstantFromRfc1123arser);
    }
    
    @Test
    public void parseRfc822Date_DateWithDayOfMonthLessThan10th_MatchesRfc1123Behavior() {
        String rfc822DateString = "Wed, 02 Apr 2014 17:43:26 GMT";
        String rfc1123DateString = "Wed, 2 Apr 2014 17:43:26 GMT";
        Instant parsedInstantFromRfc822Parser = DateUtils.parseRfc822Date(rfc822DateString);
        Instant parsedInstantFromRfc1123arser = DateUtils.parseRfc1123Date(rfc1123DateString);
        assertThat(parsedInstantFromRfc822Parser).isEqualTo(parsedInstantFromRfc1123arser);
    }

    @Test
    public void resolverStyleOfRfc822FormatterMatchesRfc1123Formatter() {
        assertThat(RFC_822_DATE_TIME.getResolverStyle()).isSameAs(RFC_1123_DATE_TIME.getResolverStyle());
    }

    @Test
    public void chronologyOfRfc822FormatterMatchesRfc1123Formatter() {
        assertThat(RFC_822_DATE_TIME.getChronology()).isSameAs(RFC_1123_DATE_TIME.getChronology());
    }

    @Test
    public void formatRfc1123Date() throws ParseException {
        String string = DateUtils.formatRfc1123Date(INSTANT);
        Instant parsedDateAsInstant = LONG_DATE_FORMAT.parse(string).toInstant();
        assertEquals(INSTANT, parsedDateAsInstant);

        String formattedDate = LONG_DATE_FORMAT.format(Date.from(INSTANT));
        Instant parsedInstant = DateUtils.parseRfc1123Date(formattedDate);
        assertEquals(INSTANT, parsedInstant);
    }

    @Test
    public void parseRfc1123Date() throws ParseException {
        String formatted = LONG_DATE_FORMAT.format(Date.from(INSTANT));
        Instant expected = LONG_DATE_FORMAT.parse(formatted).toInstant();
        Instant actual = DateUtils.parseRfc1123Date(formatted);
        assertEquals(expected, actual);
    }

    @Test
    public void parseIso8601Date() throws ParseException {
        checkParsing(DateTimeFormatter.ISO_INSTANT, COMMON_DATE_FORMAT);
    }

    @Test
    public void parseIso8601Date_withUtcOffset() {
        String formatted = "2021-05-10T17:12:13-07:00";
        Instant expected = ISO_OFFSET_DATE_TIME.parse(formatted, Instant::from);
        Instant actual = DateUtils.parseIso8601Date(formatted);
        assertEquals(expected, actual);

        String actualString = OffsetDateTime.ofInstant(actual, ZoneId.of("-7")).toString();
        assertEquals(formatted, actualString);
    }

    @Test
    public void parseIso8601Date_usingAlternativeFormat() throws ParseException {
        checkParsing(ALTERNATE_ISO_8601_DATE_FORMAT, COMMON_DATE_FORMAT);
    }

    private void checkParsing(DateTimeFormatter dateTimeFormatter, SimpleDateFormat dateFormat) throws ParseException {
        String formatted = dateFormat.format(Date.from(INSTANT));
        String alternative = dateTimeFormatter.format(INSTANT);
        assertEquals(formatted, alternative);
        Instant expected = dateFormat.parse(formatted).toInstant();
        Instant actualDate = DateUtils.parseIso8601Date(formatted);
        assertEquals(expected, actualDate);
    }

    @Test
    public void alternateIso8601DateFormat() throws ParseException {
        String expected = COMMON_DATE_FORMAT.format(Date.from(INSTANT));
        String actual = ALTERNATE_ISO_8601_DATE_FORMAT.format(INSTANT);
        assertEquals(expected, actual);

        Date expectedDate = COMMON_DATE_FORMAT.parse(expected);
        ZonedDateTime actualDateTime = ZonedDateTime.parse(actual, ALTERNATE_ISO_8601_DATE_FORMAT);
        assertEquals(expectedDate, Date.from(actualDateTime.toInstant()));
    }

    @Test(expected = ParseException.class)
    public void legacyHandlingOfInvalidDate() throws ParseException {
        final String input = "2014-03-06T14:28:58.000Z.000Z";
        COMMON_DATE_FORMAT.parse(input);
    }

    @Test(expected = DateTimeParseException.class)
    public void invalidDate() {
        final String input = "2014-03-06T14:28:58.000Z.000Z";
        DateUtils.parseIso8601Date(input);
    }

    @Test
    public void testIssue233() throws ParseException {
        // https://github.com/aws/aws-sdk-java/issues/233
        final String edgeCase = String.valueOf(MAX_MILLIS_YEAR) + "-08-17T07:12:00Z";
        Instant expected = COMMON_DATE_FORMAT.parse(edgeCase).toInstant();
        if (DEBUG) {
            System.out.println("date: " + expected);
        }
        String formatted = DateUtils.formatIso8601Date(expected);
        if (DEBUG) {
            System.out.println("formatted: " + formatted);
        }
        // we have '+' sign as prefix for years. See java.time.format.SignStyle.EXCEEDS_PAD
        assertEquals(edgeCase, formatted.substring(1));

        Instant parsed = DateUtils.parseIso8601Date(formatted);
        if (DEBUG) {
            System.out.println("parsed: " + parsed);
        }
        assertEquals(expected, parsed);
        String reformatted = ISO_INSTANT.format(parsed);
        // we have '+' sign as prefix for years. See java.time.format.SignStyle.EXCEEDS_PAD
        assertEquals(edgeCase, reformatted.substring(1));
    }

    @Test
    public void testIssue233JavaTimeLimit() {
        // https://github.com/aws/aws-sdk-java/issues/233
        String s = ALTERNATE_ISO_8601_DATE_FORMAT.format(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.MAX_VALUE), UTC));
        System.out.println("s: " + s);

        Instant parsed = DateUtils.parseIso8601Date(s);
        assertEquals(ZonedDateTime.ofInstant(parsed, UTC).getYear(), MAX_MILLIS_YEAR);
    }

    @Test
    public void testIssueDaysDiff() throws ParseException {
        // https://github.com/aws/aws-sdk-java/issues/233
        COMMON_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(UTC));
        String edgeCase = String.valueOf(MAX_MILLIS_YEAR) + "-08-17T07:12:55Z";
        String testCase = String.valueOf(MAX_MILLIS_YEAR - 1) + "-08-17T07:12:55Z";
        Date edgeDate = COMMON_DATE_FORMAT.parse(edgeCase);
        Date testDate = COMMON_DATE_FORMAT.parse(testCase);
        long diff = edgeDate.getTime() - testDate.getTime();
        assertTrue(diff == TimeUnit.DAYS.toMillis(365));
    }

    @Test
    public void numberOfDaysSinceEpoch() {
        final long now = System.currentTimeMillis();
        final long days = DateUtils.numberOfDaysSinceEpoch(now);
        final long oneDayMilli = Duration.ofDays(1).toMillis();
        // Could be equal at 00:00:00.
        assertTrue(now >=  Duration.ofDays(days).toMillis());
        assertTrue((now -  Duration.ofDays(days).toMillis()) <= oneDayMilli);
    }

    /**
     * Tests the Date marshalling and unmarshalling. Asserts that the value is
     * same before and after marshalling/unmarshalling
     */
    @Test
    public void testUnixTimestampRoundtrip() throws Exception {
        long[] testValues = new long[] {System.currentTimeMillis(), 1L, 0L};
        Arrays.stream(testValues)
              .mapToObj(Instant::ofEpochMilli)
              .forEach(instant -> {
                  String serverSpecificDateFormat = DateUtils.formatUnixTimestampInstant(instant);
                  Instant parsed = DateUtils.parseUnixTimestampInstant(String.valueOf(serverSpecificDateFormat));
                  assertEquals(instant, parsed);
              });
    }

    @Test
    public void parseUnixTimestampInstant_longerThan20Char_throws() {
        String largeNum = Stream.generate(() -> "9").limit(21).collect(Collectors.joining());
        assertThatThrownBy(() -> DateUtils.parseUnixTimestampInstant(largeNum))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("20");
    }

}
