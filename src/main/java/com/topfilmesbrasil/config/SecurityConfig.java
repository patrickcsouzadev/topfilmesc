// Este arquivo utiliza o Spring Security para proteger sua aplicação
// Ele estabelece uma série de regras e filtros que cada requisição HTTP deve passar antes de chegar aos controllers.
// é a espinha dorsal da segurança da sua aplicação. Ele estabelece uma defesa em camadas, desde ignorar arquivos estáticos para performance, até definir regras de acesso extremamente específicas para cada URL e méto.do HTTP
// garantindo que visitantes, usuários e administradores tenham acesso apenas ao que lhes é permitido.

package com.topfilmesbrasil.config;

import com.topfilmesbrasil.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // Esta é a anotação que ativa o Spring Security para a aplicação web. Ela aplica a cadeia de filtros de segurança padrão do Spring pra todas as requisições.
@EnableMethodSecurity(prePostEnabled = true) // Habilita um nível mais granular de segurança, diretamente nos métodos. Com prePostEnabled = true, podemos usar anotações como @PreAuthorize nos controllers (como fiz em AdminDeleteController) para verificar permissões antes mesmo de o méto.do ser executado.
public class SecurityConfig {

    @Autowired
    private UsuarioService usuarioService; // O Spring injeta a instância no UsuarioService
    // A classe de segurança precisa dele para uma função vital: durante o login, ele vai usar o usuarioService pra buscar os detalhes do usuário no banco de dados e verificar se a senha corresponde.

    @Bean // são objetos gerenciados pelo Spring. Aqui estou definindo e configurando os "tijolos" fundamentais do sistema de segurança.  Esse bean define COMO as senhas serão criptografadas.
    public static PasswordEncoder passwordEncoder() { // Cria um méto.do que retorna um objeto do tipo PasswordEncoder.
        return new BCryptPasswordEncoder(); // está especificando que o algoritmo de criptografia a ser usado é o BCrypt.
        // É um algoritmo forte e padrão da indústria. Ele gera um "hash" da senha que é armazenado no banco.
        // O importante é que o BCrypt é um algoritmo de hash de mão única; é praticamente impossível reverter o hash para descobrir a senha original.
    }

    @Bean // Este bean serve para otimizar a performance. Ele define uma lista de URLs que o Spring Security deve ignorar completamente.
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**"); // A instrução aqui é clara: "Ignore qualquer requisição para URLs que comecem com /css/, /js/, /images/, etc.".
        // Arquivos estáticos como CSS e JavaScript não contêm dados sensíveis e não precisam passar por filtros de autenticação ou autorização.
        // Ignorá-los torna a aplicação mais rápida, pois essas requisições não consomem recursos do sistema de segurança.
    }

    @Bean // Este é o bloco de configuração mais importante, onde defini as regras de acesso para as URLs da aplicação.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desabilita a proteção contra CSRF (Cross-Site Request Forgery).
                .authorizeHttpRequests(authz -> authz //  Inicia o bloco onde as regras de autorização são definidas.

                        .requestMatchers( // Define uma lista de URLs de páginas.
                                "/",
                                "/error",
                                "/movies",
                                "/login",
                                "/signup",
                                "/filmes",
                                "/series",
                                "/releases",
                                "/top-rated",
                                "/uploads/**",
                                "/movies/filmes/**",
                                "/movies/series/**",
                                "/email-verified",
                                "/email-verification-error",
                                "/forgot-password",
                                "/reset-password"
                        ).permitAll() // significa que qualquer pessoa (visitantes, usuários, admins) pode acessar essas páginas.

                        // Páginas que requerem autenticação
                        .requestMatchers("/profile", "/watchlist").authenticated()

                        // Permite acesso PÚBLICO às APIs de autenticação, o que é necessário para que os usuários possam se registrar e fazer login.
                        .requestMatchers("/api/signup", "/api/signin", "/api/auth/status", "/api/verify-email", "/api/resend-verification", "/api/forgot-password", "/api/reset-password").permitAll()

                        // Permite acesso PÚBLICO com méto.do GET a todas as APIs de conteúdo e reviews. Isso permite que um visitante veja os detalhes de um filme no modal, por exemplo, mas ele não poderá fazer um POST para criar um review sem estar logado.
                        .requestMatchers(HttpMethod.GET, "/movies/api/**", "/reviews/api/**", "/ratings/api/**").permitAll()

                        // Esta é uma regra restritiva. .hasRole("ADMIN") significa que qualquer URL que comece com /admin/ só pode ser acessada por um usuário que tenha a permissão ROLE_ADMIN.
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // captura qualquer outra requisição que não tenha sido coberta pelas regras anteriores.
                        // .authenticated() exige que o usuário esteja logado para acessá-la.
                        // É isso que protege, por exemplo, o POST para criar um review ou o acesso à página /favoritos.
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form // Configura o processo de login via formulário.
                        .loginPage("/login") // Informa ao Spring Security que a página de login customizada está na URL /login. Se um usuário não autenticado tentar acessar uma página protegida, ele será redirecionado para cá.
                        .permitAll()
                )
                .logout(logout -> logout // Configura o processo de logout.
                        .logoutRequestMatcher(new AntPathRequestMatcher("/usuarios/logout")) // Define que a URL /usuarios/logout irá acionar o logout.
                        .logoutSuccessUrl("/?logout") // Para onde o usuário é redirecionado após o logout.
                        .invalidateHttpSession(true) // Encerra a sessão do usuário no servidor.
                        .deleteCookies("JSESSIONID") // Remove o cookie de sessão do navegador do usuário.
                        .permitAll()
                );

        return http.build();
    }

    @Bean // Este bean configura o componente que efetivamente processa a autenticação.
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(usuarioService) // diz ao AuthenticationManager: "Para encontrar os detalhes de um usuário (como seu nome e senha hash), use o meu usuarioService".
                .passwordEncoder(passwordEncoder()); // diz: "Para comparar as senhas, use o passwordEncoder que eu defini (o BCrypt)".
        return authenticationManagerBuilder.build();
    }
}