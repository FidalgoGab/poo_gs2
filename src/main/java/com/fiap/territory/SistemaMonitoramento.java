package com.fiap.territory;

import com.fiap.territory.servico.ServicoMonitoramento;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.Scanner;

@SpringBootApplication
public class SistemaMonitoramento {

    public static void main(String[] args) {
        SpringApplication.run(SistemaMonitoramento.class, args);
    }

    // Auxilio de IA para essa implementação: o menu é chamado por aqui (SistemaMonitoramento = menu principal), mas o loop fica no MenuConsole
    // pra ficar pequeno. O perfil "test" desliga o menu pra não travar @SpringBootTest esperando teclado.
    @Bean
    @Profile("!test")
    public CommandLineRunner menuPrincipal(ServicoMonitoramento servico) {
        return args -> new MenuConsole(servico, new Scanner(System.in)).executar();
    }
}