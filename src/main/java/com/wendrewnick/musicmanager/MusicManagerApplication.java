package com.wendrewnick.musicmanager;

import com.wendrewnick.musicmanager.entity.User;
import com.wendrewnick.musicmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MusicManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicManagerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            var adminUser = userRepository.findByUsername("admin");
            if (adminUser.isEmpty()) {
                var admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .build();
                userRepository.save(admin);
                System.out.println("=================================================");
                System.out.println(" ADMIN USER CREATED: admin / admin");
                System.out.println("=================================================");
            } else {
                var admin = adminUser.get();
                admin.setPassword(passwordEncoder.encode("admin"));
                userRepository.save(admin);
                System.out.println("=================================================");
                System.out.println(" ADMIN PASSWORD RESET TO: admin");
                System.out.println("=================================================");
            }
        };
    }
}
