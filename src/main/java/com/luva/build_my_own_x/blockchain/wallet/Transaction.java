package com.luva.build_my_own_x.blockchain.wallet;

import com.luva.build_my_own_x.blockchain.LuvaChain;
import com.luva.build_my_own_x.blockchain.utils.StringUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {
    public String transactionId;
    public PublicKey sender;
    public PublicKey recipient;
    public float value;
    public byte[] signature;

    public ArrayList<TransactionInput> inputs;
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0;

    public Transaction(PublicKey sender, PublicKey recipient, float value, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.value = value;
        this.inputs = inputs;
    }

    private String calculateHash() {
        sequence++;
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        value + sequence
        );
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    public boolean processTransaction() {
        if (!verifySignature()) {
            System.out.println("@Transaction Signature invalid");
            return false;
        }

        for (TransactionInput transaction : inputs) {
            transaction.UTXO = LuvaChain.UTXOs.get(transaction.transactionOutputId);
        }

        float inputsValue = getInputsValue();
        if (inputsValue < LuvaChain.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + inputsValue);
            return false;
        }

        float leftOver = inputsValue - value;
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

        for (TransactionOutput output : outputs) {
            LuvaChain.UTXOs.put(output.id, output);
        }

        for (TransactionInput input : inputs) {
            if (input.UTXO != null) {
                LuvaChain.UTXOs.remove(input.UTXO.id);
            }
        }
        return true;
    }

    public float getInputsValue() {
        return inputs.stream().filter(i -> i.UTXO != null).map(transactionInput -> transactionInput.getUTXO().getValue()).reduce(0F, Float::sum);
    }

    public float getOutputsValue() {
        return outputs.stream().map(TransactionOutput::getValue).reduce(0F, Float::sum);
    }
}
