package com.rbkmoney.xrates.exchange.impl.provider.psb.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PsbExchangeRateData {

    @JsonProperty("CURR")
    private String currencyCode;

    @JsonProperty("IPS")
    private String ips;

    @JsonProperty("BUY")
    private BigDecimal value;


}
