package io.remedymatch.bedarf.domain;

import io.remedymatch.anfrage.domain.AnfrageEntity;
import io.remedymatch.anfrage.domain.AnfrageRepository;
import io.remedymatch.engine.EngineClient;
import io.remedymatch.institution.domain.InstitutionEntity;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static io.remedymatch.engine.AnfrageProzessConstants.PROZESS_KEY;

@AllArgsConstructor
@Service
public class BedarfService {

    private final BedarfRepository bedarfRepository;
    private final EngineClient engineClient;
    private final AnfrageRepository anfrageRepository;

    @Transactional
    public void bedarfMelden(BedarfEntity bedarf, InstitutionEntity institutionEntity) {
        //TODO Prozess starten?
        bedarf.setInstitution(institutionEntity);
        bedarfRepository.save(bedarf);
    }

    @Transactional
    public Optional<BedarfEntity> bedarfLaden(String bedarfId) {
        return bedarfRepository.findById(UUID.fromString(bedarfId));
    }

    @Transactional
    public Iterable<BedarfEntity> alleBedarfeLaden() {
        return bedarfRepository.findAll();
    }

    @Transactional
    public void bedarfLoeschen(UUID id) {
        val bedarf = bedarfRepository.findById(id);
        bedarfRepository.delete(bedarf.get());
    }

    @Transactional
    public void bedarfUpdaten(BedarfEntity bedarf) {
        val oldBedarf = bedarfRepository.findById(bedarf.getId()).get();
        oldBedarf.setAnzahl(bedarf.getAnzahl());
        oldBedarf.setArtikel(bedarf.getArtikel());
        bedarfRepository.save(oldBedarf);
    }


    @Transactional
    public void starteAnfrage(UUID bedarfId, InstitutionEntity anfrager, String kommentar) {

        val bedarf = bedarfRepository.findById(bedarfId);

        if (bedarf.isEmpty()) {
            throw new IllegalArgumentException("Bedarf ist nicht vorhanden");
        }

        val anfrage = AnfrageEntity.builder()
                .anfrager(anfrager)
                .kommentar(kommentar)
                .bedarf(bedarf.get())
                .build();

        anfrageRepository.save(anfrage);
        val prozessInstanzId = engineClient.prozessStarten(PROZESS_KEY, "BEDARF", anfrage.getId().toString(), bedarf.get().getInstitution().getId().toString());
        anfrage.setProzessInstanzId(prozessInstanzId);
        anfrageRepository.save(anfrage);
    }

    @Transactional
    public void anfrageBeantworten(String taskId, boolean entscheidung) {
        engineClient.taskAbschliessen(taskId, entscheidung);
    }

}
