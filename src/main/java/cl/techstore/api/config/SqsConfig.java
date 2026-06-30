package cl.techstore.api.config;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SqsConfig {

    @Bean
    public SqsClient sqsClient() {
        // DefaultCredentialsProvider es la clave para el 100% en la nube nativa.
        // En tu máquina local buscará las variables de entorno de AWS,
        // y cuando corra en Amazon ECS Fargate, leerá de forma automática 
        // e interna los tokens temporales del 'LabRole'.
        return SqsClient.builder()
                .region(Region.US_EAST_1) // Región obligatoria de tu AWS Academy Learner Lab
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}