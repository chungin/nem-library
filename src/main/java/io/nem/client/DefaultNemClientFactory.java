package io.nem.client;

import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.ribbon.RibbonClient;
import io.nem.client.account.AccountClient;
import io.nem.client.account.FeignAccountClient;
import io.nem.client.blockchain.BlockchainClient;
import io.nem.client.blockchain.FeignBlockchainClient;
import io.nem.client.mosaic.FeignMosaicClient;
import io.nem.client.mosaic.MosaicClient;
import io.nem.client.node.FeignNodeClient;
import io.nem.client.node.NodeClient;
import io.nem.client.status.FeignStatusClient;
import io.nem.client.status.StatusClient;
import io.nem.client.transaction.FeignTransactionClient;
import io.nem.client.transaction.SecureTransactionClient;
import io.nem.client.transaction.TransactionClient;
import io.nem.client.transaction.encode.*;
import io.nem.client.transaction.fee.DefaultFeeCalculator;
import io.nem.client.transaction.fee.FeeCalculator;
import io.nem.client.transaction.version.DefaultVersionProvider;
import io.nem.client.transaction.version.Network;
import io.nem.client.transaction.version.VersionProvider;

import static feign.hystrix.HystrixFeign.builder;
import static java.lang.String.format;

public class DefaultNemClientFactory implements NemClientFactory {

    public final static Network MAIN = new Network(0x68, "NAMESPACEWH4MKFMBCVFERDPOOP4FK7MTBXDPZZA", "NBMOSAICOD4F54EE5CDMR23CCBGOAM2XSIUX6TRS");
    public final static Network TEST = new Network(0x98, "TAMESPACEWH4MKFMBCVFERDPOOP4FK7MTDJEYP35", "TBMOSAICOD4F54EE5CDMR23CCBGOAM2XSJBR5OLC");

    @Override
    public StatusClient createStatusClient(String configurationPrefix) {
        return builder()
                .client(RibbonClient.create())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(FeignStatusClient.class, format("http://%s", configurationPrefix));
    }

    @Override
    public AccountClient createAccountClient(String configurationPrefix) {
        return builder()
                .client(RibbonClient.create())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(FeignAccountClient.class, format("http://%s", configurationPrefix));
    }

    @Override
    public BlockchainClient createBlockchainClient(String configurationPrefix) {
        return builder()
                .client(RibbonClient.create())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(FeignBlockchainClient.class, format("http://%s", configurationPrefix));
    }

    @Override
    public NodeClient createNodeClient(String configurationPrefix) {
        return builder()
                .client(RibbonClient.create())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(FeignNodeClient.class, format("http://%s", configurationPrefix));
    }

    @Override
    public MosaicClient createMosaicClient(String configurationPrefix) {
        return builder()
                .client(RibbonClient.create())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(FeignMosaicClient.class, format("http://%s", configurationPrefix));
    }

    @Override
    public TransactionClient createTransactionClient(String configurationPrefix,
                                                     Network network,
                                                     MosaicClient mosaicClient,
                                                     AccountClient accountClient,
                                                     NodeClient nodeClient) {
        FeignTransactionClient feignTransactionClient = builder()
                .client(RibbonClient.create())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(FeignTransactionClient.class, format("http://%s", configurationPrefix));
        HexConverter hexConverter = new DefaultHexConverter();
        ByteSerializer byteSerializer = new DefaultByteSerializer(hexConverter);
        TransactionEncoder transactionEncoder = new ByteArrayTransactionEncoder(byteSerializer, hexConverter);
        VersionProvider versionProvider = new DefaultVersionProvider();
        FeeCalculator feeCalculator = new DefaultFeeCalculator(mosaicClient, accountClient);
        return new SecureTransactionClient(network, feignTransactionClient, transactionEncoder, hexConverter, versionProvider, feeCalculator, nodeClient);
    }
}
