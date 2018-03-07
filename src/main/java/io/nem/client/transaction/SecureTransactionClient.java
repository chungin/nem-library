package io.nem.client.transaction;

import io.nem.client.common.*;
import io.nem.client.common.multisig.Modification;
import io.nem.client.common.multisig.RelativeChange;
import io.nem.client.node.NodeClient;
import io.nem.client.transaction.encode.DefaultSigner;
import io.nem.client.transaction.encode.HexConverter;
import io.nem.client.transaction.encode.Signer;
import io.nem.client.transaction.encode.TransactionEncoder;
import io.nem.client.transaction.fee.FeeCalculator;
import io.nem.client.transaction.request.RequestAnnounce;
import io.nem.client.transaction.response.NemAnnounceResult;
import io.nem.client.transaction.version.Network;
import io.nem.client.transaction.version.VersionProvider;

import java.util.List;

import static io.nem.client.transaction.TransactionType.*;
import static java.math.BigInteger.TEN;
import static java.util.stream.Collectors.toList;

public class SecureTransactionClient implements TransactionClient {

    private final Network network;
    private final FeignTransactionClient feignTransactionClient;
    private final TransactionEncoder transactionEncoder;
    private final HexConverter hexConverter;
    private final VersionProvider versionProvider;
    private final FeeCalculator feeCalculator;
    private final NodeClient nodeClient;

    public SecureTransactionClient(Network network,
                                   FeignTransactionClient feignTransactionClient,
                                   TransactionEncoder transactionEncoder,
                                   HexConverter hexConverter,
                                   VersionProvider versionProvider,
                                   FeeCalculator feeCalculator, NodeClient nodeClient) {
        this.network = network;
        this.feignTransactionClient = feignTransactionClient;
        this.transactionEncoder = transactionEncoder;
        this.hexConverter = hexConverter;
        this.versionProvider = versionProvider;
        this.feeCalculator = feeCalculator;
        this.nodeClient = nodeClient;
    }

    @Override
    public NemAnnounceResult transferNem(String privateKey, String toAddress, long microXemAmount, String message, int timeToLiveInSeconds) {

        Signer signer = new DefaultSigner(privateKey);
        String publicKey = signer.publicKey();

        int currentTime = nodeClient.extendedInfo().nisInfo.currentTime;

        Transaction transaction = transferNemTransaction(toAddress, microXemAmount, message, timeToLiveInSeconds, publicKey, currentTime);

        byte[] data = transactionEncoder.data(transaction);
        return feignTransactionClient.prepare(new RequestAnnounce(hexConverter.getString(data), signer.sign(data)));
    }

    @Override
    public NemAnnounceResult transferMosaics(String privateKey, String toAddress, String message, int timeToLiveInSeconds, List<MosaicTransfer> mosaics, int times) {

        Signer signer = new DefaultSigner(privateKey);
        String publicKey = signer.publicKey();

        int currentTime = nodeClient.extendedInfo().nisInfo.currentTime;

        Transaction transaction = mosaicsTransferTransaction(toAddress, message, timeToLiveInSeconds, mosaics, times, publicKey, currentTime);

        byte[] data = transactionEncoder.data(transaction);

        return feignTransactionClient.prepare(new RequestAnnounce(hexConverter.getString(data), signer.sign(data)));
    }

    @Override
    public NemAnnounceResult createMultisigAccount(String privateKey, int timeToLiveInSeconds, List<String> cosignatories, int minCosignatories) {

        Signer signer = new DefaultSigner(privateKey);

        int currentTime = nodeClient.extendedInfo().nisInfo.currentTime;

        Transaction transaction = Transaction.builder()
                .type(MULTISIG_AGGREGATE_MODIFICATION.type)
                .version(versionProvider.version(network, MULTISIG_AGGREGATE_MODIFICATION))
                .timeStamp(currentTime)
                .signer(signer.publicKey())
                .fee(feeCalculator.multisigAccountCreationFee())
                .deadline(currentTime + timeToLiveInSeconds)
                .modifications(cosignatories.stream().map(publicKey -> new Modification(1, publicKey)).collect(toList()))
                .minCosignatories(new RelativeChange(minCosignatories))
                .build();

        byte[] data = transactionEncoder.data(transaction);

        return feignTransactionClient.prepare(new RequestAnnounce(hexConverter.getString(data), signer.sign(data)));
    }

    @Override
    public NemAnnounceResult multisigTransferNem(String privateKey, String multisigPublicKey, String toAddress, long microXemAmount, String message, int timeToLiveInSeconds) {
        Signer signer = new DefaultSigner(privateKey);
        int currentTime = nodeClient.extendedInfo().nisInfo.currentTime;

        Transaction transferTransaction = transferNemTransaction(toAddress, microXemAmount, message, timeToLiveInSeconds, multisigPublicKey, currentTime);
        Transaction transaction = Transaction.builder()
                .type(MULTISIG_TRANSACTION.type)
                .version(versionProvider.version(network, MULTISIG_TRANSACTION))
                .timeStamp(currentTime)
                .signer(signer.publicKey())
                .fee(feeCalculator.multisigTransactionFee())
                .deadline(currentTime + timeToLiveInSeconds)
                .otherTrans(transferTransaction)
                .build();

        byte[] data = transactionEncoder.data(transaction);

        return feignTransactionClient.prepare(new RequestAnnounce(hexConverter.getString(data), signer.sign(data)));
    }

    @Override
    public NemAnnounceResult multisigTransferMosaics(String privateKey, String multisigPublicKey, String toAddress, String message, int timeToLiveInSeconds, List<MosaicTransfer> mosaics, int times) {

        Signer signer = new DefaultSigner(privateKey);
        int currentTime = nodeClient.extendedInfo().nisInfo.currentTime;

        Transaction transferTransaction = mosaicsTransferTransaction(toAddress, message, timeToLiveInSeconds, mosaics, times, multisigPublicKey, currentTime);
        Transaction transaction = Transaction.builder()
                .type(MULTISIG_TRANSACTION.type)
                .version(versionProvider.version(network, MULTISIG_TRANSACTION))
                .timeStamp(currentTime)
                .signer(signer.publicKey())
                .fee(feeCalculator.multisigTransactionFee())
                .deadline(currentTime + timeToLiveInSeconds)
                .otherTrans(transferTransaction)
                .build();

        byte[] data = transactionEncoder.data(transaction);

        return feignTransactionClient.prepare(new RequestAnnounce(hexConverter.getString(data), signer.sign(data)));
    }

    @Override
    public NemAnnounceResult cosignTransaction(String privateKey, String transactionHash, String multisigAddress, int timeToLiveInSeconds) {
        Signer signer = new DefaultSigner(privateKey);
        int currentTime = nodeClient.extendedInfo().nisInfo.currentTime;

        Transaction transaction = Transaction.builder()
                .type(MULTISIG_SIGNATURE.type)
                .version(versionProvider.version(network, MULTISIG_SIGNATURE))
                .timeStamp(currentTime)
                .signer(signer.publicKey())
                .fee(feeCalculator.cosigningFee())
                .deadline(currentTime + timeToLiveInSeconds)
                .otherAccount(multisigAddress)
                .otherHash(new Hash(transactionHash))
                .build();

        byte[] data = transactionEncoder.data(transaction);

        return feignTransactionClient.prepare(new RequestAnnounce(hexConverter.getString(data), signer.sign(data)));
    }

    @Override
    public NemAnnounceResult createNamespace(String privateKey, String parentNamespace, String namespace, int timeToLiveInSeconds) {
        Signer signer = new DefaultSigner(privateKey);
        int currentTime = nodeClient.extendedInfo().nisInfo.currentTime;

        ProvisionNamespaceTransaction transaction = ProvisionNamespaceTransaction.builder()
                .type(PROVISION_NAMESPACE.type)
                .version(versionProvider.version(network, PROVISION_NAMESPACE))
                .timeStamp(currentTime)
                .signer(signer.publicKey())
                .fee(feeCalculator.namespaceProvisionFee())
                .deadline(currentTime + timeToLiveInSeconds)
                .rentalFeeSink(network.rentalFeeSink)
                .rentalFee(feeCalculator.rentalFee(parentNamespace, namespace))
                .parent(parentNamespace)
                .newPart(namespace)
                .build();

        byte[] data = transactionEncoder.data(transaction);

        return feignTransactionClient.prepare(new RequestAnnounce(hexConverter.getString(data), signer.sign(data)));
    }

    private Transaction transferNemTransaction(String toAddress, long microXemAmount, String message, int timeToLiveInSeconds, String publicKey, int currentTime) {
        return Transaction.builder()
                .type(TRANSFER_NEM.type)
                .version(versionProvider.version(network, TRANSFER_NEM))
                .timeStamp(currentTime)
                .signer(publicKey)
                .fee(feeCalculator.fee(microXemAmount, message))
                .deadline(currentTime + timeToLiveInSeconds)
                .recipient(toAddress)
                .amount(microXemAmount)
                .message(new Message(message, 1))
                .build();
    }

    private Transaction mosaicsTransferTransaction(String toAddress, String message, int timeToLiveInSeconds, List<MosaicTransfer> mosaics, int times, String publicKey, int currentTime) {
        return Transaction.builder()
                .type(TRANSFER_MOSAICS.type)
                .version(versionProvider.version(network, TRANSFER_MOSAICS))
                .timeStamp(currentTime)
                .signer(publicKey)
                .fee(feeCalculator.fee(mosaics, times, message))
                .deadline(currentTime + timeToLiveInSeconds)
                .recipient(toAddress)
                .amount(times * TEN.pow(6).longValue())
                .message(new Message(message, 1))
                .mosaics(mosaics)
                .build();
    }
}
