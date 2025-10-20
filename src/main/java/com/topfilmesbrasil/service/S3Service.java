package com.topfilmesbrasil.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    
    /**
     * Faz upload de um arquivo para o S3 e retorna a URL pública
     * @param file Arquivo a ser enviado
     * @return URL pública do arquivo no S3
     */
    String uploadFile(MultipartFile file);
    
    /**
     * Remove um arquivo do S3
     * @param fileUrl URL do arquivo a ser removido
     */
    void deleteFile(String fileUrl);
    
    /**
     * Verifica se o serviço S3 está habilitado
     * @return true se estiver em ambiente de produção com S3 configurado
     */
    boolean isS3Enabled();
}

