package cl.bennu.bice.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Response {

    private UUID uuid;
    private String scriptZip;

}