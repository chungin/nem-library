package io.nem.client.account.response.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.nem.client.common.transaction.Transaction;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class UnconfirmedTransactionMetaDataPair {

    public final UnconfirmedTransactionMetaData meta;
    public final Transaction transaction;

    @JsonCreator
    public UnconfirmedTransactionMetaDataPair(@JsonProperty("meta") UnconfirmedTransactionMetaData meta,
                                              @JsonProperty("transaction") Transaction transaction) {
        this.meta = meta;
        this.transaction = transaction;
    }
}
