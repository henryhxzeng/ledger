package com.ledger.domain;

import java.util.ArrayList;
import java.util.List;

import com.ledger.common.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    private String accountId;
    private String entityId;
    private String accountName;
    private Status status;
    private List<Wallet> wallets = new ArrayList<>();
}
