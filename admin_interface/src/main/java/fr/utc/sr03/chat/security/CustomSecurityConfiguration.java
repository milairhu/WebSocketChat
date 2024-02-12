package fr.utc.sr03.chat.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
public class CustomSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final JwtTokenFilter jwtTokenFilter;

    public CustomSecurityConfiguration(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Obligatoire pour pouvoir executer des requetes depuis un navigateur avec CORS
        // => Force Spring a ajouter les headers CORS (Access-Control-Allow...) dans les reponses
        var CorsConfig = new CorsConfiguration().applyPermitDefaultValues();
        CorsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        CorsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        CorsConfig.setAllowCredentials(true);
        http.cors().configurationSource(request -> CorsConfig);

        // Configuration de la securite
        http.authorizeRequests()
                // Autorisation des ressources "statiques"
                .antMatchers("/css/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/assets/**").permitAll()
                // Autorisation des Controllers "Web" : la securite est geree manuellement dans les controllers
                .antMatchers("/","/login/**", "/admin/**","/user/**", "/chat/**").permitAll()
                // Autorisation du endpoint REST "login" ... faut bien pouvoir se logger
                .antMatchers("/users/login", "/users/login/forgotten").permitAll()
                // Autorisation des endpoints Websocket : la securite est geree manuellement dans le serveur websocket
                .antMatchers("/WsChat/**").permitAll()
                // Toutes les autres requetes necessitent une authentification
                .anyRequest().authenticated()
                // Desactivation Spring CSRF protection pour autoriser les requetes POST
                .and().csrf().disable();

        // Application du filtre JWT
        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}