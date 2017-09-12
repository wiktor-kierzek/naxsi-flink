package com.example.mappers;

import org.testng.annotations.Test;

import java.text.SimpleDateFormat;

import static org.testng.Assert.*;

/**
 * Created by _sn on 11.09.2017.
 */
public class ParseLogLineTest {

    @Test
    public void testFMT_1() throws Exception {
        String log = "2017/09/09 17:14:50 [error] 16048#0: *3045261 NAXSI_FMT: ip=77.65.91.38&server=naxsi.kierzek.pl&uri=/&total_processed=5&total_blocked=4&zone0=ARGS&id0=1000&var_name0=v&zone1=ARGS&id1=1007&var_name1=v&zone2=ARGS&id2=1015&var_name2=v, client: 77.65.91.38, server: naxsi.kierzek.pl, request: \\\"GET /?v=32%20UNION%20ALL%20SELECT%20NULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL--%20XwUM HTTP/1.1\\\", host: \\\"naxsi.kierzek.pl\\\"";

        ParseLogLine.FMTLog parsed = (ParseLogLine.FMTLog) new ParseLogLine().map(new ExtractNaxsiMessage.NaxsiTuple("fmt", log));

        assertEquals(parsed.getTimestamp(), "2017/09/09 17:14:50");
        assertEquals(parsed.getIp(), "77.65.91.38");
        assertEquals(parsed.getServer(), "naxsi.kierzek.pl");
        assertEquals(parsed.getUri(), "/");
        assertEquals(parsed.getRequest(), "GET /?v=32%20UNION%20ALL%20SELECT%20NULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL--%20XwUM HTTP/1.1\\");

        assertEquals(parsed.getFindings().size(),3);

        assertEquals(parsed.getFindings().get(0).getId(), "1000");
        assertEquals(parsed.getFindings().get(0).getZone(), "ARGS");
        assertEquals(parsed.getFindings().get(0).getType(), ParseLogLine.FMTLog.Finding.TYPE.SQLi);
        assertEquals(parsed.getFindings().get(0).getVarName(), "v");
    }

    @Test
    public void testFMT_2() throws Exception {
        String log = "2017/09/12 14:38:50 [error] 2693#2693: *35 NAXSI_FMT: ip=85.232.253.3&server=naxsi.kierzek.pl&uri=/&learning=1&vers=0.55.3&total_processed=18&total_blocked=9&block=1&cscore0=$SQL&score0=268&cscore1=$XSS&score1=336&zone0=BODY&id0=1001&var_name0=password&zone1=BODY&id1=1013&var_name1=password&zone2=BODY&id2=1302&var_name2=password&zone3=BODY&id3=1303&var_name3=password, client: 85.232.253.3, server: naxsi.kierzek.pl, request: \"POST / HTTP/1.1\", host: \"naxsi.kierzek.pl\"";

        ParseLogLine.FMTLog parsed = (ParseLogLine.FMTLog) new ParseLogLine().map(new ExtractNaxsiMessage.NaxsiTuple("fmt", log));

        assertEquals(parsed.getTimestamp(),"2017/09/12 14:38:50");
        assertEquals(parsed.getIp(), "85.232.253.3");
        assertEquals(parsed.getServer(), "naxsi.kierzek.pl");
        assertEquals(parsed.getUri(), "/");
        assertEquals(parsed.getRequest(), "POST / HTTP/1.1");

        assertEquals(parsed.getFindings().size(), 4);
    }

    @Test
    public void testEXLog() throws Exception {
        String log = "2017/09/12 14:38:28 [error] 2693#2693: *11 NAXSI_EXLOG: ip=85.232.253.3&server=naxsi.kierzek.pl&uri=/&id=1001&zone=BODY&var_name=password&content='''''''''''''\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"<script>, client: 85.232.253.3, server: naxsi.kierzek.pl, request: \"POST / HTTP/1.1\", host: \"naxsi.kierzek.pl\"";

        ParseLogLine.ExtendedLog parsed = (ParseLogLine.ExtendedLog) new ParseLogLine().map(new ExtractNaxsiMessage.NaxsiTuple("exlog", log));

        assertEquals(parsed.getTimestamp(),"2017/09/12 14:38:28");
        assertEquals(parsed.getIp(), "85.232.253.3");
        assertEquals(parsed.getServer(), "naxsi.kierzek.pl");
        assertEquals(parsed.getUri(), "/");
        assertEquals(parsed.getRequest(), "POST / HTTP/1.1");

        assertEquals(parsed.getVarName(), "password");
        assertEquals(parsed.getContent(), "'''''''''''''\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"<script>");
    }

    @Test
    public void testEXLog_2() throws Exception {
        String log = "2017/09/12 16:41:53 [error] 2693#2693: *41845 NAXSI_EXLOG: ip=85.232.253.3&server=naxsi.kierzek.pl&uri=/&id=1016&zone=ARGS&var_name=fwf&content=322)%20OR%20(SELECT%20*%20FROM%20(SELECT(SLEEP(5)))pZOq)%23, client: 85.232.253.3, server: naxsi.kierzek.pl, request: \\\"GET /?fwf=322%29%20OR%20%28SELECT%20%2A%20FROM%20%28SELECT%28SLEEP%285%29%29%29pZOq%29%23&njf=2323 HTTP/1.1\\\", host: \\\"naxsi.kierzek.pl\\\"";

        ParseLogLine.ExtendedLog parsed = (ParseLogLine.ExtendedLog) new ParseLogLine().map(new ExtractNaxsiMessage.NaxsiTuple("exlog", log));

        assertEquals(parsed.getTimestamp(),"2017/09/12 16:41:53");
        assertEquals(parsed.getIp(), "85.232.253.3");
        assertEquals(parsed.getServer(), "naxsi.kierzek.pl");
        assertEquals(parsed.getUri(), "/");
    }

}
