/*
 * Copyright 2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tutorial;

import java.util.Date;

/**
 * Alternative RFC 1123 data formatter.  SimpleDataFormat class has thread-safe
 * issues as documented by Sun.  This formatter is based on code provided by
 * Tim Kientzle on the Tomcat list-serv and made available for free public use.
 * @see http://marc.theaimsgroup.com/?l=tomcat-dev&m=97146648030873&w=2
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class RfcDateFormat {

    /**
     * Enumeration of the days of the week.
     */
    private static final String[] WEEKDAYS =
        {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    /**
     * Enumeration of the months of the year.
     */
    private static final String[] MONTHS =
        {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /**
     * Formats a date object according to RFC 1123.
     * <p>
     * An example formatted date would appear as "Sun, 06 Nov 1994 08:49:37 GMT"
     *
     * @param date the date to format
     * @return the given date formatting according to RFC 1123
     */
    public static String format(Date date) {

        // Compute day number, seconds since beginning of day
        long s = date.getTime();;
        if (s >= 0) {
            // seconds since 1 Jan 1970
            s /= 1000;
        } else {
            // floor(sec/1000)
            s = (s - 999) / 1000;
        }

        int dn = (int) (s / 86400);
        s %= 86400;  // positive seconds since beginning of day
        if (s < 0) {
            s += 86400;
            dn--;
        }
        dn += 1969 * 365 + 492 - 19 + 4; // days since "1 Jan, year 1"

        // Convert days since 1 Jan, year 1 to year/yearday
        int y = 400 * (dn / 146097) + 1;
        int d = dn % 146097;
        if (d == 146096) {
            // Last year of 400 is long
            y += 399;
            d = 365;
        } else {
            y += 100 * (d / 36524);
            d %= 36524;
            y += 4 * (d / 1461);
            d %= 1461;
            if (d == 1460) {
                // Last year out of 4 is long
                y += 3;
                d = 365;
            } else {
                y += d / 365;
                d %= 365;
            }
        }

        boolean isleap = ((y % 4 == 0) && !(y % 100 == 0)) || (y % 400 == 0);

        // Compute month/monthday from year/yearday
        if (!isleap && (d >= 59)) {
            // Skip non-existent Feb 29
            d++;
        }
        if (d >= 60) {
            // Skip non-existent Feb 30
            d++;
        }
        int mon = ((d % 214) / 61) * 2 + ((d % 214) % 61) / 31;
        if (d > 213) {
            mon += 7;
        }
        d = ((d % 214) % 61) % 31 + 1;

    // Convert second to hour/min/sec
	int m = (int) (s / 60);
    int h = m / 60;
    m %= 60;
    s %= 60;

    // Day of week, 0==Sun
	int w = (dn + 1) % 7;

	/* RFC 1123 date string: "Sun, 06 Nov 1994 08:49:37 GMT" */
	StringBuffer buff = new StringBuffer(32);
	buff.append(WEEKDAYS[w]);
	buff.append(", ");
	buff.append((char) (d / 10 + '0'));
	buff.append((char) (d % 10 + '0'));
	buff.append(' ');
	buff.append(MONTHS[mon]);
	buff.append(' ');
	buff.append(y);
	buff.append(' ');
	buff.append((char) (h / 10 + '0'));
	buff.append((char) (h % 10 + '0'));
	buff.append(':');
	buff.append((char) (m / 10 + '0'));
	buff.append((char) (m % 10 + '0'));
	buff.append(':');
	buff.append((char) (s / 10 + '0'));
	buff.append((char) (s % 10 + '0'));
	buff.append(" GMT");
	return buff.toString();
    }
}
