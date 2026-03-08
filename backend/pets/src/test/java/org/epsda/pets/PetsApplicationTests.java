package org.epsda.pets;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PetsApplicationTests {

    @Test
    void contextLoads() {
        String SECRET_KEY = Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
        System.out.println(SECRET_KEY);
    }

}
