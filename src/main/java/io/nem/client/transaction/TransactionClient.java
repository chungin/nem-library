package io.nem.client.transaction;

import io.nem.client.common.MosaicTransfer;
import io.nem.client.transaction.response.NemAnnounceResult;

import java.util.List;

public interface TransactionClient {

    NemAnnounceResult transferNem(String privateKey, String toAddress, long microXemAmount, String message, int timeToLiveInSeconds);

    NemAnnounceResult transferMosaics(String privateKey, String toAddress, String message, int timeToLiveInSeconds, List<MosaicTransfer> mosaics, int times);

    NemAnnounceResult createMultisigAccount(String privateKey, int timeToLiveInSeconds, List<String> cosignatories, int minCosignatories);

    NemAnnounceResult multisigTransferNem(String privateKey, String multisigPublicKey, String toAddress, long microXemAmount, String message, int timeToLiveInSeconds);

    NemAnnounceResult multisigTransferMosaics(String privateKey, String multisigPublicKey, String toAddress, String message, int timeToLiveInSeconds, List<MosaicTransfer> mosaics, int times);

    NemAnnounceResult cosignTransaction(String privateKey, String transactionHash, String multisigAddress, int timeToLiveInSeconds);

    NemAnnounceResult createNamespace(String privateKey, String parentNamespace, String namespace, int timeToLiveInSeconds);

}
