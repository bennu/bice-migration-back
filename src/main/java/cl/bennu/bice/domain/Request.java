package cl.bennu.bice.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Request {

    private Integer fundCounter;
    private Integer operationsMax;
    private Integer operationDetailsMax;
    private Integer percentageUsdCurrency;
    private Integer percentageOrigin;

}
