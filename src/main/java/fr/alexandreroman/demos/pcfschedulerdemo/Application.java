/*
 * Copyright (c) 2019 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.demos.pcfschedulerdemo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.*;
import java.time.OffsetDateTime;

import static java.lang.Thread.sleep;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@Configuration
@Slf4j
@RequiredArgsConstructor
class BatchConfig {
    private final JobExecutionRepository repo;

    @Bean
    // Disable batch execution when running unit tests.
    @Profile("!test")
    CommandLineRunner batchRunner() {
        return args -> {
            log.info("Starting batch job");
            try {
                doRun();
            } finally {
                log.info("Batch job stopped");
            }
        };
    }

    private void doRun() throws Exception {
        // This method actually runs a batch job.
        final var maxSteps = 10;
        for (int step = 1; step <= maxSteps; ++step) {
            log.info("Running batch job: step {}/{}", step, maxSteps);

            // Simulate lengthy task.
            final JobExecution job = new JobExecution();
            job.setMessage(String.format("step %d", step));
            repo.save(job);
            sleep(5000);
        }
    }
}

// Fake database operations.

@Data
@Entity
class JobExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false)
    private OffsetDateTime timestamp = OffsetDateTime.now();
    @Column(nullable = false, length = 32)
    private String message;
}

interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {
}
