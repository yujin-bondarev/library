package com.example.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.JWSAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public JwtAuthFilter() throws Exception {
        URL jwkSetURL = new URL("https://www.googleapis.com/oauth2/v3/certs");
        this.jwtProcessor = new DefaultJWTProcessor<>();
        RemoteJWKSet<SecurityContext> keySource = new RemoteJWKSet<>(jwkSetURL);
        this.jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.info("JwtAuthFilter: start filtering request {}", request.getRequestURI());
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                SignedJWT signedJWT = SignedJWT.parse(token);
                JWTClaimsSet claimsSet = jwtProcessor.process(signedJWT, null);

                String email = claimsSet.getSubject();
                logger.info("JwtAuthFilter: token subject (email) = {}", email);

                List<String> authorities = (List<String>) claimsSet.getClaim("authorities");
                List<SimpleGrantedAuthority> grantedAuthorities;
                if (authorities != null) {
                    grantedAuthorities = authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    logger.info("JwtAuthFilter: authorities = {}", authorities);
                } else {
                    grantedAuthorities = Collections.emptyList();
                    logger.info("JwtAuthFilter: no authorities found in token");
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null,
                        grantedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (ParseException | JOSEException e) {
                logger.error("JwtAuthFilter: invalid or expired JWT token", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                return;
            } catch (Exception e) {
                logger.error("JwtAuthFilter: error processing JWT token", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error processing JWT token");
                return;
            }
        } else {
            logger.info("JwtAuthFilter: no Authorization header or does not start with Bearer");
        }
        filterChain.doFilter(request, response);
    }
}
