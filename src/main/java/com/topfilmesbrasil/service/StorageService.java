package com.topfilmesbrasil.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    
    private final Path rootLocation = Paths.get("uploads");
    private final S3Service s3Service;

    public StorageService(S3Service s3Service) {
        this.s3Service = s3Service;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de upload.", e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Falha ao salvar arquivo nulo ou vazio.");
        }

        // Se S3 estiver habilitado (produção), usar S3
        if (s3Service.isS3Enabled()) {
            logger.info("Usando S3 para armazenamento de arquivo");
            return s3Service.uploadFile(file);
        }

        // Caso contrário, usar armazenamento local (desenvolvimento)
        logger.info("Usando armazenamento local para arquivo");
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return "/" + this.rootLocation.getFileName().toString() + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo.", e);
        }
    }
    
    public void delete(String fileUrl) {
        // Se S3 estiver habilitado, deletar do S3
        if (s3Service.isS3Enabled()) {
            logger.info("Deletando arquivo do S3: {}", fileUrl);
            s3Service.deleteFile(fileUrl);
            return;
        }
        
        // Caso contrário, deletar do armazenamento local
        try {
            if (fileUrl.startsWith("/uploads/")) {
                String filename = fileUrl.substring("/uploads/".length());
                Path file = rootLocation.resolve(filename);
                Files.deleteIfExists(file);
                logger.info("Arquivo local deletado: {}", filename);
            }
        } catch (IOException e) {
            logger.error("Erro ao deletar arquivo local: {}", e.getMessage());
        }
    }
}