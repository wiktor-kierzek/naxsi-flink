package com.example.mappers;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by _sn on 11.09.2017.
 */
public class ParseLogLineTest {

    @Test
    public void testFMT() throws Exception {
        String log = "2017/09/09 17:14:50 [error] 16048#0: *3045261 NAXSI_FMT: ip=77.65.91.38&server=naxsi.kierzek.pl&uri=/&total_processed=5&total_blocked=4&zone0=ARGS&id0=1000&var_name0=v&zone1=ARGS&id1=1007&var_name1=v&zone2=ARGS&id2=1015&var_name2=v, client: 77.65.91.38, server: naxsi.kierzek.pl, request: \\\"GET /?v=32%20UNION%20ALL%20SELECT%20NULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL--%20XwUM HTTP/1.1\\\", host: \\\"naxsi.kierzek.pl\\\"";

        ParseLogLine.FMTLog parsed = (ParseLogLine.FMTLog) new ParseLogLine().map(new ExtractNaxsiMessage.NaxsiTuple("fmt", log));

        assertEquals(parsed.getTimestamp(),"2017/09/09 17:14:50");
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
}
