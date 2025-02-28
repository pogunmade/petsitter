package com.example.petsitter;

import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static com.example.petsitter.users.User.UserRole.*;

@SpringBootApplication
public class PetSitterApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetSitterApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner initDemoData(EntityManager entityManager) {

        return new CommandLineRunner() {

            @Override
            @Transactional
            public void run(String... args) {

                var aliceTheAdminUuid = UUID.fromString("cc085f34-a338-44bb-aeb0-557ee724d7cd");
                var owenThePetOwnerUuid = UUID.fromString("a96056bc-c7bc-4d96-a5f5-ed6510293731");
                var sallyThePetSitterUuid = UUID.fromString("9fb1ceaa-277e-43c3-9cff-af24ef0a8e99");
                var jobUuid = UUID.fromString("fdafe54b-4614-4660-b29d-f3ef5a28a409");

                entityManager.createNativeQuery("""
                        INSERT INTO users (id, email, password, full_name, version)
                        	 VALUES (?,?,?,?,?)""")
                    .setParameter(1, aliceTheAdminUuid)
                    .setParameter(2, "admin@example.com")
                    .setParameter(3, "{bcrypt}$2a$10$16L4qAUqBZKqfVmmkbTtFecqp5nRnw80DYB1vTgoQB8gVu7XUrkEe")
                    .setParameter(4, "Alice The Admin")
                    .setParameter(5, 0)
                    .executeUpdate();

                entityManager.createNativeQuery("""
                        INSERT INTO user_roles (user_id, roles)
                             VALUES (?,?)""")
                    .setParameter(1, aliceTheAdminUuid)
                    .setParameter(2, ADMIN.name())
                    .executeUpdate();

                entityManager.createNativeQuery("""
                        INSERT INTO users (id, email, password, full_name, version)
                          	 VALUES (?,?,?,?,?)""")
                    .setParameter(1, owenThePetOwnerUuid)
                    .setParameter(2, "pet-owner@example.com")
                    .setParameter(3, "{bcrypt}$2a$10$NmzKr5PKbBwc6aNwrlq5IOjfoGwvubZ57B9HL2hjDekBLYcTwW0ey")
                    .setParameter(4, "Owen The Pet Owner")
                    .setParameter(5, 0)
                    .executeUpdate();

                entityManager.createNativeQuery("""
                        INSERT INTO user_roles (user_id, roles)
                             VALUES (?,?)""")
                    .setParameter(1, owenThePetOwnerUuid)
                    .setParameter(2, PET_OWNER.name())
                    .executeUpdate();

                entityManager.createNativeQuery("""
                        INSERT INTO users (id, email, password, full_name, version)
                        	 VALUES (?,?,?,?,?)""")
                    .setParameter(1, sallyThePetSitterUuid)
                    .setParameter(2, "pet-sitter@example.com")
                    .setParameter(3, "{bcrypt}$2a$10$dKanbl3YZUHJOCYvZGTwA.A6VkXszaddPn8ExqEvrWbL/Y8ik6Df.")
                    .setParameter(4, "Sally The Pet Sitter")
                    .setParameter(5, 0)
                    .executeUpdate();

                entityManager.createNativeQuery("""
                        INSERT INTO user_roles (user_id, roles)
                             VALUES (?,?)""")
                    .setParameter(1, sallyThePetSitterUuid)
                    .setParameter(2, PET_SITTER.name())
                    .executeUpdate();

                var startTime = LocalDate.now().plusWeeks(1).atTime(LocalTime.NOON);

                entityManager.createNativeQuery("""
                        INSERT INTO jobs (id, start_time, end_time, activity, name, age, breed, size, job_owner_id, version)
                        	 VALUES (?,?,?,?,?,?,?,?,?,?)""")
                    .setParameter(1, jobUuid)
                    .setParameter(2, startTime)
                    .setParameter(3, startTime.plusHours(3))
                    .setParameter(4, "Walk, House sit")
                    .setParameter(5, "Molly")
                    .setParameter(6, 8)
                    .setParameter(7, "Irish Setter")
                    .setParameter(8, "26kg")
                    .setParameter(9, owenThePetOwnerUuid)
                    .setParameter(10, 0)
                    .executeUpdate();

                entityManager.createNativeQuery("""
                        INSERT INTO job_applications (id, application_job_id, application_owner_id, application_status,
                                                      version)
                             VALUES (?,?,?,?,?)""")
                    .setParameter(1, UUID.fromString("eee6b21a-bd39-4ab8-b369-a30140eb3ee1"))
                    .setParameter(2, jobUuid)
                    .setParameter(3, sallyThePetSitterUuid)
                    .setParameter(4, "PENDING")
                    .setParameter(5, 0)
                    .executeUpdate();
            }
        };
    }
}
