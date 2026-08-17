package br.com.agenteingles;

import br.com.agenteingles.agente.PropriedadesDoAgente;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PropriedadesDoAgente.class)
public class AgenteInglesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenteInglesApplication.class, args);
    }
}
