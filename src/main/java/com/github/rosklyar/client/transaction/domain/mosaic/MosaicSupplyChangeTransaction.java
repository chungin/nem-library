package com.github.rosklyar.client.transaction.domain.mosaic;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public class MosaicSupplyChangeTransaction {
    public final int timeStamp;
    public final long fee;
    public final int type;
    public final int deadline;
    public final int version;
    public final String signer;
    public final SupplyType supplyType;
    public final long delta;
    public final MosaicId mosaicId;
}
