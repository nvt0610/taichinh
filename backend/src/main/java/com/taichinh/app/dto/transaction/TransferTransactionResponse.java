package com.taichinh.app.dto.transaction;

public record TransferTransactionResponse(
        TransactionResponse sourceTransaction,
        TransactionResponse destinationTransaction) {
}
