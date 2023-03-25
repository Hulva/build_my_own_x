package com.luva.build_my_own_x.blockchain.wallet;

import lombok.Data;

@Data
public class TransactionInput {
    public String transactionOutputId;
    public TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
