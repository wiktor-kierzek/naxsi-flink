package com.example.mappers;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Created by wiktor.kierzek@curency-one.com on 12.09.2017.
 */
public class ExtractNaxsiMessageTest {

    @Test
    public void testEXLog_1() throws Exception {
        String rabbitMQMessage = "{\"@timestamp\":\"2017-09-12T16:40:11.957Z\",\"offset\":53495626,\"@version\":\"1\",\"input_type\":\"log\",\"beat\":{\"hostname\":\"naxsi\",\"name\":\"naxsi\",\"version\":\"5.6.0\"},\"host\":\"naxsi\",\"source\":\"/var/log/nginx/error.log\",\"message\":\"2017/09/12 16:40:09 [error] 2693#2693: *40539 NAXSI_EXLOG: ip=85.232.253.3&server=naxsi.kierzek.pl&uri=/&id=1000&zone=ARGS&var_name=fwf&content=-9977'))%20OR%204740=CONVERT(INT,(SELECT%20CHAR(113)%2BCHAR(118)%2BCHAR(112)%2BCHAR(106)%2BCHAR(113)%2B(SELECT%20(CASE%20WHEN%20(4740=4740)%20THEN%20CHAR(49)%20ELSE%20CHAR(48)%20END))%2BCHAR(113)%2BCHAR(107)%2BCHAR(98)%2BCHAR(98)%2BCHAR(113)))%20AND%20(('aYiI'%20LIKE%20'aYiI, client: 85.232.253.3, server: naxsi.kierzek.pl, request: \\\"GET /?fwf=-9977%27%29%29%20OR%204740%3DCONVERT%28INT%2C%28SELECT%20CHAR%28113%29%2BCHAR%28118%29%2BCHAR%28112%29%2BCHAR%28106%29%2BCHAR%28113%29%2B%28SELECT%20%28CASE%20WHEN%20%284740%3D4740%29%20THEN%20CHAR%2849%29%20ELSE%20CHAR%2848%29%20END%29%29%2BCHAR%28113%29%2BCHAR%28107%29%2BCHAR%2898%29%2BCHAR%2898%29%2BCHAR%28113%29%29%29%20AND%20%28%28%27aYiI%27%20LIKE%20%27aYiI&njf=2323 HTTP/1.1\\\", host: \\\"naxsi.kierzek.pl\\\"\",\"type\":\"log\",\"tags\":[\"beats_input_codec_plain_applied\"]}";
        ExtractNaxsiMessage.NaxsiTuple tuple = new ExtractNaxsiMessage().map(rabbitMQMessage);

        assertEquals("exlog", tuple.getLog());
    }

    @Test
    public void testFMT_1() throws Exception {
        String rabbitMQMessage = "{\"@timestamp\":\"2017-09-12T16:40:11.957Z\",\"offset\":53501034,\"@version\":\"1\",\"beat\":{\"hostname\":\"naxsi\",\"name\":\"naxsi\",\"version\":\"5.6.0\"},\"input_type\":\"log\",\"host\":\"naxsi\",\"source\":\"/var/log/nginx/error.log\",\"message\":\"2017/09/12 16:40:09 [error] 2693#2693: *40539 NAXSI_FMT: ip=85.232.253.3&server=naxsi.kierzek.pl&uri=/&learning=1&vers=0.55.3&total_processed=20269&total_blocked=17499&block=1&cscore0=$SQL&score0=184&cscore1=$XSS&score1=336&zone0=ARGS&id0=1000&var_name0=fwf&zone1=ARGS&id1=1009&var_name1=fwf&zone2=ARGS&id2=1010&var_name2=fwf&zone3=ARGS&id3=1011&var_name3=fwf&zone4=ARGS&id4=1013&var_name4=fwf&zone5=ARGS&id5=1015&var_name5=fwf, client: 85.232.253.3, server: naxsi.kierzek.pl, request: \\\"GET /?fwf=-9977%27%29%29%20OR%204740%3DCONVERT%28INT%2C%28SELECT%20CHAR%28113%29%2BCHAR%28118%29%2BCHAR%28112%29%2BCHAR%28106%29%2BCHAR%28113%29%2B%28SELECT%20%28CASE%20WHEN%20%284740%3D4740%29%20THEN%20CHAR%2849%29%20ELSE%20CHAR%2848%29%20END%29%29%2BCHAR%28113%29%2BCHAR%28107%29%2BCHAR%2898%29%2BCHAR%2898%29%2BCHAR%28113%29%29%29%20AND%20%28%28%27aYiI%27%20LIKE%20%27aYiI&njf=2323 HTTP/1.1\\\", host: \\\"naxsi.kierzek.pl\\\"\",\"type\":\"log\",\"tags\":[\"beats_input_codec_plain_applied\"]}";
        ExtractNaxsiMessage.NaxsiTuple tuple = new ExtractNaxsiMessage().map(rabbitMQMessage);

        assertEquals(tuple.getLog(), "fmt");
        assertEquals(tuple.getMessage(), "2017/09/12 16:40:09 [error] 2693#2693: *40539 NAXSI_FMT: ip=85.232.253.3&server=naxsi.kierzek.pl&uri=/&learning=1&vers=0.55.3&total_processed=20269&total_blocked=17499&block=1&cscore0=$SQL&score0=184&cscore1=$XSS&score1=336&zone0=ARGS&id0=1000&var_name0=fwf&zone1=ARGS&id1=1009&var_name1=fwf&zone2=ARGS&id2=1010&var_name2=fwf&zone3=ARGS&id3=1011&var_name3=fwf&zone4=ARGS&id4=1013&var_name4=fwf&zone5=ARGS&id5=1015&var_name5=fwf, client: 85.232.253.3, server: naxsi.kierzek.pl, request: \\\"GET /?fwf=-9977%27%29%29%20OR%204740%3DCONVERT%28INT%2C%28SELECT%20CHAR%28113%29%2BCHAR%28118%29%2BCHAR%28112%29%2BCHAR%28106%29%2BCHAR%28113%29%2B%28SELECT%20%28CASE%20WHEN%20%284740%3D4740%29%20THEN%20CHAR%2849%29%20ELSE%20CHAR%2848%29%20END%29%29%2BCHAR%28113%29%2BCHAR%28107%29%2BCHAR%2898%29%2BCHAR%2898%29%2BCHAR%28113%29%29%29%20AND%20%28%28%27aYiI%27%20LIKE%20%27aYiI&njf=2323 HTTP/1.1\\\", host: \\\"naxsi.kierzek.pl\\\"");
    }

}
