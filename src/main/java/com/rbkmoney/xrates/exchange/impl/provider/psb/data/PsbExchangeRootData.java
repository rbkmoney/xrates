package com.rbkmoney.xrates.exchange.impl.provider.psb.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.List;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PsbExchangeRootData {

    @JsonProperty("rates")
    private List<PsbExchangeRateData> rates;

    @JsonProperty("ERROR")
    private String error;

    public boolean hasError() {
        return !StringUtils.isEmpty(error);
    }

}
