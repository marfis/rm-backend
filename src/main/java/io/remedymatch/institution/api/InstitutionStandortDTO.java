package io.remedymatch.institution.api;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
public class InstitutionStandortDTO {


    private UUID id;

    @NotNull
    private String name;

    @NotNull
    private String plz;

    @NotNull
    private String ort;

    @NotNull
    private String strasse;

    @NotNull
    private String land;

    private double longitude;

    private double latitude;
}
