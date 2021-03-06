package io.remedymatch.artikel.domain;


import io.remedymatch.TestApplication;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class)
@DirtiesContext
@ActiveProfiles("test")
@Tag("InMemory")
@Tag("SpringBoot")
public class ArtikelJpaRepositoryInMemoryTestShould {
    @Autowired
    private ArtikelJpaRepository jpaRepository;

    @Test
    @Transactional
    @Rollback(true)
    public void artikel_zurueckliefern_wenn_vorhanden() {

        var article = artikel("Mein Artikel");
        var id = jpaRepository.save(article).getId();

        var fetched = jpaRepository.findById(id).orElseThrow();
        assertEquals(fetched.getName(), article.getName());
    }

    private ArtikelEntity artikel(String name) {
        return ArtikelEntity.builder()
                .name(name)
                .ean("SAMPLE_EAN")
                .beschreibung("Sample Beschreibung")
                .hersteller("Egal")
                .build();
    }
}
