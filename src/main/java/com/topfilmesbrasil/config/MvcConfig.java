// Este arquivo tem a função especifica de configurar como o Spring MVC lida com recursos estáticos.
// Principalmente aqueles que não estão dentro das pastas padrão do projeto.

package com.topfilmesbrasil.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration // Anotação que indica que esta classe é uma configuração do Spring. O Spring irá processar esta classe durante a inicialização para aplicar as configurações personalizadas que ela tem.
public class MvcConfig implements WebMvcConfigurer { // Implementando essa interface, a classe ganha a habilidade de "conversar" com a configuração padrão do Spring MVC e personalizá-la. Ela oferece vários métodos que posso sobrescrever para ajustar o comportamento do framework

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) { // O méto.do é usado para registrar "manipuladores de recursos" (Resource Handlers), que são responsáveis por servir arquivos estáticos como CSS, JavaScript e, nesse caso, imagens
        String uploadDir = Paths.get("uploads").toAbsolutePath().toString(); // Esta linha usa a classe Paths do Java para criar um objeto que representa o caminho para uma pasta chamada uploads.
        // toAbsolutePath(): Este méto.do converte o caminho relativo (uploads) em um caminho absoluto no sistema de arquivos do servidor
        // .toString(): Converte o objeto de caminho absoluto em uma String de texto. A variável uploadDir agora tem o caminho completo no disco para a pasta de uploads.
        registry.addResourceHandler("/uploads/**")  // Aqui definimos o padrão de URL. To dizendo ao Spring: "Sempre que uma requisição HTTP chegar para uma URL que comece com /uploads/...".
                // O ** significa que qualquer coisa após /uploads/ será tratada por este manipulador de recursos. Por exemplo, /uploads/imagem.jpg ou /uploads/folder/arquivo.png.
                .addResourceLocations("file:/" + uploadDir + "/"); // file:/ Aqui definimos o local físico correspondente ao padrão de URL. Você está dizendo: "...vá buscar o arquivo correspondente neste local do disco".
        // + uploadDir + "/": Concatena o prefixo com o caminho absoluto da pasta uploads que foi definido anteriormente.
    }
}