package ai.bitflow.helppress.publisher.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Base62Util {

    private Map<String, String> longToShort = new HashMap<String, String>();
    private Map<String, String> shortToLong = new HashMap<String, String>();

    // Encodes a URL to a shortened URL.
    public String encode(String longUrl) {
        if(longToShort.containsKey(longUrl)) {
            return longToShort.get(longUrl);
        }
        
        while(true) {
            String shortUrl = "http://tinyurl.com/" + generate20RandomStr();
            if(!shortToLong.containsKey(shortUrl)) {
                shortToLong.put(shortUrl, longUrl);
                longToShort.put(longUrl, shortUrl);
                return shortUrl;
            }            
        }
    }
    
    public static String generate20RandomStr() {
        String charArr = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String res = "";
        for(int i = 0; i < 20; i++) {
            Random rand = new Random();
            int index = rand.nextInt(62);
            res += charArr.charAt(index);
        }
        return res;
    }

    // Decodes a shortened URL to its original URL.
    public String decode(String shortUrl) {
        if(!shortToLong.containsKey(shortUrl))
            return null;
        return shortToLong.get(shortUrl);
    }
    
}
