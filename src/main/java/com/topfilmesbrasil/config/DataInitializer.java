package com.topfilmesbrasil.config;

import com.topfilmesbrasil.model.Filme;
import com.topfilmesbrasil.model.Role;
import com.topfilmesbrasil.model.Serie;
import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.repository.FilmeRepository;
import com.topfilmesbrasil.repository.SerieRepository;
import com.topfilmesbrasil.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner { // Essa interface está dizendo ao Spring: "após a aplicação ser totalmente iniciada e o contexto estiver pronto, execute o méto.do run desta classe"

    @Autowired  //Esta anotação é o mecanismo de injeção de dependência do Spring. Em vez de você mesmo criar instâncias dessas classes (ex: new UsuarioRepository()), você pede ao Spring para "injetar" (fornecer) as instâncias que ele já está gerenciando.
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FilmeRepository filmeRepository;

    @Autowired
    private SerieRepository serieRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Dependência para criptografar as senhas antes de salvar no banco de dados.

    @Override
    public void run(String... args) throws Exception {  // méto.do está sobrescrevendo o méto.do run da interface CommandLineRunner
        // Insere usuários iniciais se a tabela estiver vazia
        if (usuarioRepository.count() == 0) {
            // Admin
            Usuario admin = new Usuario();
            admin.setNomeCompleto("Administrador do Sistema");
            admin.setEmail("admin@topfilmesbrasil.com");
            admin.setSenha(passwordEncoder.encode("admin")); // Onde definimos que a senha não é salva no banco como texto simples e sim em um hash criptografado. Quando o admin tentar fazer login, o Spring Security fará o hash da senha digitada e comparará os hashes, não as senhas.
            admin.setRole(Role.ROLE_ADMIN); // define a permissão desse usuário como ADMIN. Com isso ele tem acesso às páginas e funcionalidades de administração, conforme definido no SecurityConfig.java
            usuarioRepository.save(admin);

            // User
            Usuario user = new Usuario();
            user.setNomeCompleto("Usuário Comum");
            user.setEmail("user@topfilmesbrasil.com");
            user.setSenha(passwordEncoder.encode("user"));
            user.setRole(Role.ROLE_USER); // Define a permissão desse usuário como USER. Com isso ele tem acesso às páginas e funcionalidades de usuário comum, conforme definido no SecurityConfig.java
            usuarioRepository.save(user);
        }

        // Insere filmes iniciais se a tabela estiver vazia
        if (filmeRepository.count() == 0) {
            Filme matrix = new Filme();
            matrix.setTitulo("The Matrix");
            matrix.setEmDestaque(true); // Define este filme como um destaque. A lógica no HomeController usará essa informação para exibir este filme no carrossel da página inicial.
            matrix.setDescricao("Um hacker de computador aprende com rebeldes misteriosos sobre a verdadeira natureza de sua realidade e seu papel na guerra contra seus controladores.");
            matrix.setAnoLancamento(1999);
            matrix.setGenero("Ação");
            matrix.setListaGeneros(new HashSet<>(Arrays.asList("Ação", "Ficção Científica"))); // Cria um conjunto de strings para as tags de gênero.
            matrix.setUrlPoster("/images/matrix1.jpg"); // Define os caminhos para as imagens que o frontend usará para exibir o pôster e a imagem de fundo.
            matrix.setUrlBackgroundImage("/images/matrix-bg.jpg"); // Define os caminhos para as imagens que o frontend usará para exibir o pôster e a imagem de fundo.
            matrix.setUrlTrailer("m8e-FF8MsqU"); //Salva apenas o ID do vídeo do YouTube, que o JavaScript no frontend usará para montar a URL de incorporação correta.
            matrix.setDuracaoMinutos(136);
            matrix.setClassificacaoIndicativa("16");
            matrix.setDiretor("The Wachowskis");
            matrix.setElenco("Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss");
            filmeRepository.save(matrix); // Salva o filme "The Matrix" no banco de dados. O mesmo processo é repetido para os outros filmes

            Filme shawshank = new Filme();
            shawshank.setTitulo("The Shawshank Redemption");
            shawshank.setEmDestaque(true);
            shawshank.setDescricao("Dois homens encarcerados se vinculam ao longo de vários anos, encontrando consolo e eventual redenção por meio de atos de decência comum.");
            shawshank.setAnoLancamento(1994);
            shawshank.setGenero("Drama");
            shawshank.setListaGeneros(new HashSet<>(Arrays.asList("Drama")));
            shawshank.setUrlPoster("/images/shawshank-poster.jpg");
            shawshank.setUrlBackgroundImage("/images/shawshank-bg.jpg");
            shawshank.setUrlTrailer("PLl99DlL6b4");
            shawshank.setDuracaoMinutos(142);
            shawshank.setClassificacaoIndicativa("16");
            shawshank.setDiretor("Frank Darabont");
            shawshank.setElenco("Tim Robbins, Morgan Freeman");
            filmeRepository.save(shawshank);

            Filme pulpFiction = new Filme();
            pulpFiction.setTitulo("Pulp Fiction");
            pulpFiction.setEmDestaque(true);
            pulpFiction.setDescricao("As vidas de dois assassinos da máfia, um boxeador, a esposa de um gângster e um par de bandidos de lanchonete se entrelaçam em quatro contos de violência e redenção.");
            pulpFiction.setAnoLancamento(1994);
            pulpFiction.setGenero("Crime");
            pulpFiction.setListaGeneros(new HashSet<>(Arrays.asList("Crime", "Drama")));
            pulpFiction.setUrlPoster("/images/pulp-fiction-poster.jpg");
            pulpFiction.setUrlBackgroundImage("/images/pulp-fiction-bg.jpg");
            pulpFiction.setUrlTrailer("s7EdQ4FqbhY");
            pulpFiction.setDuracaoMinutos(154);
            pulpFiction.setClassificacaoIndicativa("18");
            pulpFiction.setDiretor("Quentin Tarantino");
            pulpFiction.setElenco("John Travolta, Samuel L. Jackson, Uma Thurman");
            filmeRepository.save(pulpFiction);
        }

        if (serieRepository.count() == 0) {
            Serie breakingBad = new Serie();
            breakingBad.setTitulo("Breaking Bad");
            breakingBad.setEmDestaque(true);
            breakingBad.setDescricao("Um professor de química do ensino médio diagnosticado com câncer terminal se une a um ex-aluno para produzir e vender metanfetamina de alta qualidade.");
            breakingBad.setAnoLancamento(2008);
            breakingBad.setGenero("Crime");
            breakingBad.setListaGeneros(new HashSet<>(Arrays.asList("Crime", "Drama", "Thriller")));
            breakingBad.setUrlPoster("/images/breaking-bad-poster.jpg");
            breakingBad.setUrlBackgroundImage("/images/breaking-bad-bg.jpg");
            breakingBad.setNumeroTemporadas(5);
            breakingBad.setStatus("FINALIZADA");
            serieRepository.save(breakingBad);
        }
    }
}