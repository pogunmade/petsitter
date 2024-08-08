package com.example.petsitter.config;

import com.example.petsitter.model.Dog;
import com.example.petsitter.model.Job;
import com.example.petsitter.model.User;
import com.example.petsitter.model.UserRole;
import com.example.petsitter.repository.JobRepository;
import com.example.petsitter.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Set;

@Configuration
class LoadDatabaseH2 {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabaseH2.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, JobRepository jobRepository) {

        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFullName("Tomas Johnson");
        adminUser.setRoles(Set.of(UserRole.ADMIN));

        User user1 = new User();
        user1.setEmail("joe@example.com");
        user1.setPassword("password");
        user1.setFullName("Joe Bloggs");
        user1.setRoles(Set.of(UserRole.PET_OWNER));

        Dog dog1 = new Dog();
        dog1.setName("Booboo");
        dog1.setAge(3);
        dog1.setBreed("Colly");
        dog1.setSize("Medium");

        Job job1 = new Job();
        job1.setStartTime(LocalDateTime.now());
        job1.setEndTime(LocalDateTime.now().plusHours(3));
        job1.setActivity("Walk and sit");
        job1.setDog(dog1);
        job1.setJobOwner(user1);

        User user2 = new User();
        user2.setEmail("jurgen@example.com");
        user2.setPassword("password");
        user2.setFullName("Jurgen Sholtz");
        user2.setRoles(Set.of(UserRole.PET_OWNER));

        Dog dog2 = new Dog();
        dog2.setName("Rascal");
        dog2.setAge(5);
        dog2.setBreed("German Shepard");
        dog2.setSize("Large");

        Job job2 = new Job();
        job2.setStartTime(LocalDateTime.now().plusWeeks(1));
        job2.setEndTime(LocalDateTime.now().plusWeeks(1).plusHours(5));
        job2.setActivity("Sit");
        job2.setDog(dog2);
        job2.setJobOwner(user2);

        User user3 = new User();
        user3.setEmail("lily@example.com");
        user3.setPassword("password");
        user3.setFullName("Lilly Jones");
        user3.setRoles(Set.of(UserRole.PET_SITTER));

        User user4 = new User();
        user4.setEmail("babatunde@example.com");
        user4.setPassword("password");
        user4.setFullName("Babatunde Kanu");
        user4.setRoles(Set.of(UserRole.PET_SITTER));

        User user5 = new User();
        user5.setEmail("rajul@example.com");
        user5.setPassword("password");
        user5.setFullName("Rajul Asraf");
        user5.setRoles(Set.of(UserRole.PET_SITTER));

        return args -> {
            log.info("Preloading " + userRepository.save(adminUser));
            log.info("Preloading " + jobRepository.save(job1));
            log.info("Preloading " + jobRepository.save(job2));
            log.info("Preloading " + userRepository.save(user3));
            log.info("Preloading " + userRepository.save(user4));
            log.info("Preloading " + userRepository.save(user5));
        };
    }
}