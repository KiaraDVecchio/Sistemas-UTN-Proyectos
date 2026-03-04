package ar.utn.ba.ddsi.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JwtUtil {
    @Getter
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15 min
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 días

     /*
    public static String generarAccessToken(String username, Long userId, Collection<String> roles, Collection<String> permisos) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("servicioUsuario")
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .claim("uid", userId)
                .claim("roles", roles != null ? roles : List.of())
                .claim("permisos", permisos != null ? permisos : List.of())
                .signWith(key)
                .compact();
    } */

    public static String generarAccessToken(String username, Long userId) {
        return Jwts.builder()
            .setSubject(username) // el mail
            .setIssuer("servicioUsuario")
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
            .claim("uid", userId)  // aca guardamos el id de Usuario
            .signWith(key)
            .compact();
    }


    public static String generarRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("servicioUsuario")
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .claim("type", "refresh") // diferenciamos refresh del access
                .signWith(key)
                .compact();
    }

    public static String validarToken(String token) { //trata de fijarse si puede obtener el nombre de usuario (porq tiene seteado eso el profesor)
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
