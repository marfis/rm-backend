package io.remedymatch.bedarf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.remedymatch.TestApplication;
import io.remedymatch.WithMockJWT;
import io.remedymatch.artikel.ArtikelFixtures;
import io.remedymatch.artikel.api.ArtikelMapper;
import io.remedymatch.artikel.domain.ArtikelEntity;
import io.remedymatch.artikel.domain.ArtikelJpaRepository;
import io.remedymatch.artikel.domain.ArtikelKategorieEntity;
import io.remedymatch.artikel.domain.ArtikelKategorieRepository;
import io.remedymatch.bedarf.api.BedarfDTO;
import io.remedymatch.bedarf.domain.BedarfEntity;
import io.remedymatch.bedarf.domain.BedarfRepository;
import io.remedymatch.institution.domain.InstitutionEntity;
import io.remedymatch.institution.domain.InstitutionRepository;
import io.remedymatch.person.domain.PersonEntity;
import io.remedymatch.person.domain.PersonRepository;
import io.remedymatch.web.UserProvider;
import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsIterableWithSize;
import org.hamcrest.core.IsIterableContaining;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("InMemory")
@Tag("SpringBoot")
@Disabled // da Endphase und noch schnell einiges sich ändern kann
public class BedarfIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BedarfRepository bedarfRepository;

    @Autowired
    private ArtikelJpaRepository artikelJpaRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ArtikelKategorieRepository artikelKategorieRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    @Transactional
    public void beforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        bedarfRepository.deleteAll();
        artikelJpaRepository.deleteAll();
        artikelKategorieRepository.deleteAll();
        personRepository.deleteAll();
        institutionRepository.deleteAll();

        var institution = institutionRepository.save(InstitutionEntity.builder()
                .name("testinstitut")
                .institutionKey(UUID.randomUUID().toString())
                .build());
        personRepository.save(PersonEntity.builder()
                .username("testUser")
                .institution(institution)
                .build());
    }

    @Test
    @WithMockJWT(groupsClaim = {"testgroup"}, subClaim = "testUser")
    public void bedarfMelden() throws Exception {
        var artikelFixture = ArtikelFixtures.completeArtikelEntity();
        var savedArtikelKategorie = artikelKategorieRepository.save(artikelFixture.getArtikelKategorie());
        artikelFixture.setArtikelKategorie(savedArtikelKategorie);
        var savedArtikel = artikelJpaRepository.save(artikelFixture);

        var bedarf = BedarfDTO.builder()
                .anzahl(2000.00)
                .artikel(ArtikelMapper.getArticleDTO(savedArtikel))
                .build();

        MvcResult postBedarfResult = mockMvc.perform(post("/bedarf")
                .contentType("application/json")
                .accept("application/json")
                .content(objectMapper.writeValueAsString(bedarf)))
                .andReturn();

        assertThat(postBedarfResult.getResponse().getStatus(), CoreMatchers.is(200));
        var allBeduerfnisse = bedarfRepository.findAll();
        assertThat(allBeduerfnisse, IsIterableWithSize.iterableWithSize(1));
        BedarfEntity savedBedarf = allBeduerfnisse.iterator().next();
        assertThat(savedBedarf.getAnzahl(), CoreMatchers.is(2000.00));
        assertThat(savedBedarf.getArtikel().getId(), CoreMatchers.is(savedArtikel.getId()));
    }

    @Test
    @WithMockJWT(groupsClaim = {"ROLE_admin"}, subClaim = "testUser")
    @Disabled
    public void alleBeduerfnisseLaden() throws Exception {
        var artikelFixture = ArtikelFixtures.completeArtikelEntity();
        var savedArtikelKategorie = artikelKategorieRepository.save(artikelFixture.getArtikelKategorie());
        artikelFixture.setArtikelKategorie(savedArtikelKategorie);
        var savedArtikel = artikelJpaRepository.save(artikelFixture);

        var saved1 = bedarfRepository.save(BedarfEntity.builder()
                .anzahl(2000.00)
                .artikel(savedArtikel)
                .build());
        var saved2 = bedarfRepository.save(BedarfEntity.builder()
                .anzahl(4000.00)
                .artikel(savedArtikel)
                .build());

        var getResponse = mockMvc.perform(get("/bedarf")
                .accept("application/json"))
                .andReturn();
        assertThat(getResponse.getResponse().getStatus(),CoreMatchers.is(200));
        List<BedarfDTO> beduerfnisse = objectMapper.readValue(getResponse.getResponse().getContentAsString(), new TypeReference<List<BedarfDTO>>(){});
        assertThat(beduerfnisse,IsIterableWithSize.iterableWithSize(2));
    }
}
