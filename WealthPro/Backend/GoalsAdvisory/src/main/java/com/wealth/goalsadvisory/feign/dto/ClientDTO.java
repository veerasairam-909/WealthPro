package com.wealth.goalsadvisory.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClientDTO {
    private Long clientId;
    private String name;
    private String email;
    private String status;
    private String segment;
}
