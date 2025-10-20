package com.topfilmesbrasil.service.impl;

import com.topfilmesbrasil.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.url-base}")
    private String urlBase;

    @Override
    public void enviarEmailVerificacao(String emailDestinatario, String nomeUsuario, String tokenVerificacao) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(emailDestinatario);
            helper.setSubject("🎬 Verificação de Email - Top Filmes Brasil");
            
            String linkVerificacao = urlBase + "/api/verify-email?token=" + tokenVerificacao;
            
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verificação de Email</title>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f8f9fa; }
                        .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; }
                        .content { padding: 30px; }
                        .welcome { font-size: 18px; color: #333; margin-bottom: 20px; }
                        .message { color: #666; line-height: 1.6; margin-bottom: 25px; }
                        .button-container { text-align: center; margin: 30px 0; }
                        .button { display: inline-block; background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; }
                        .button:hover { transform: translateY(-2px); box-shadow: 0 4px 8px rgba(220, 53, 69, 0.3); }
                        .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 15px; margin: 20px 0; color: #856404; }
                        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }
                        .emoji { font-size: 24px; margin-right: 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1><span class="emoji">🎬</span>Top Filmes Brasil</h1>
                        </div>
                        <div class="content">
                            <div class="welcome">
                                <span class="emoji">👋</span>Olá, <strong>%s</strong>!
                            </div>
                            <div class="message">
                                <span class="emoji">🎉</span>Bem-vindo ao <strong>Top Filmes Brasil</strong>!<br><br>
                                Estamos muito felizes em tê-lo conosco! Para começar a explorar nossa plataforma e descobrir os melhores filmes e séries, você precisa verificar seu email.
                            </div>
                            <div class="button-container">
                                <a href="%s" class="button">
                                    <span class="emoji">✅</span>Verificar Email
                                </a>
                            </div>
                            <div class="warning">
                                <span class="emoji">⏰</span><strong>Importante:</strong> Este link é válido por <strong>24 horas</strong>. Após esse período, será necessário solicitar um novo link de verificação.
                            </div>
                            <div class="message">
                                <span class="emoji">🔒</span>Se você não criou uma conta em nosso site, pode ignorar este email com segurança.
                            </div>
                        </div>
                        <div class="footer">
                            <span class="emoji">❤️</span>Atenciosamente,<br>
                            <strong>Equipe Top Filmes Brasil</strong>
                        </div>
                    </div>
                </body>
                </html>
                """, nomeUsuario, linkVerificacao);
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email de verificação: " + e.getMessage());
        }
    }

    @Override
    public void enviarEmailRedefinicaoSenha(String emailDestinatario, String nomeUsuario, String tokenRedefinicao) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(emailDestinatario);
            helper.setSubject("🔐 Redefinição de Senha - Top Filmes Brasil");
            
            String linkRedefinicao = urlBase + "/reset-password?token=" + tokenRedefinicao;
            
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Redefinição de Senha</title>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f8f9fa; }
                        .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; }
                        .content { padding: 30px; }
                        .welcome { font-size: 18px; color: #333; margin-bottom: 20px; }
                        .message { color: #666; line-height: 1.6; margin-bottom: 25px; }
                        .button-container { text-align: center; margin: 30px 0; }
                        .button { display: inline-block; background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; }
                        .button:hover { transform: translateY(-2px); box-shadow: 0 4px 8px rgba(220, 53, 69, 0.3); }
                        .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 15px; margin: 20px 0; color: #856404; }
                        .security { background-color: #d1ecf1; border: 1px solid #bee5eb; border-radius: 8px; padding: 15px; margin: 20px 0; color: #0c5460; }
                        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }
                        .emoji { font-size: 24px; margin-right: 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1><span class="emoji">🔐</span>Redefinição de Senha</h1>
                        </div>
                        <div class="content">
                            <div class="welcome">
                                <span class="emoji">👋</span>Olá, <strong>%s</strong>!
                            </div>
                            <div class="message">
                                <span class="emoji">🔑</span>Recebemos uma solicitação para redefinir a senha da sua conta no <strong>Top Filmes Brasil</strong>.<br><br>
                                Se foi você quem solicitou, clique no botão abaixo para criar uma nova senha segura.
                            </div>
                            <div class="button-container">
                                <a href="%s" class="button">
                                    <span class="emoji">🔄</span>Redefinir Senha
                                </a>
                            </div>
                            <div class="warning">
                                <span class="emoji">⏰</span><strong>Importante:</strong> Este link é válido por <strong>1 hora</strong>. Após esse período, será necessário solicitar um novo link.
                            </div>
                            <div class="security">
                                <span class="emoji">🛡️</span><strong>Segurança:</strong> Se você não solicitou a redefinição de senha, pode ignorar este email com segurança. Sua senha atual permanecerá inalterada.
                            </div>
                            <div class="message">
                                <span class="emoji">💡</span><strong>Dica:</strong> Use uma senha forte com pelo menos 8 caracteres, incluindo letras maiúsculas, minúsculas, números e símbolos.
                            </div>
                        </div>
                        <div class="footer">
                            <span class="emoji">❤️</span>Atenciosamente,<br>
                            <strong>Equipe Top Filmes Brasil</strong>
                        </div>
                    </div>
                </body>
                </html>
                """, nomeUsuario, linkRedefinicao);
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email de redefinição de senha: " + e.getMessage());
        }
    }
}

