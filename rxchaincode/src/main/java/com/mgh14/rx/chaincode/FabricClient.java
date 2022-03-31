package com.mgh14.rx.chaincode;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import sun.security.ec.ECPrivateKeyImpl;

import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public class FabricClient
{
//    Map<String, String> peerLocations = new HashMap<>();
//    Map<String, String> ordererLocations = new HashMap<>();
//    Map<String, String> eventHubLocations = new HashMap<>();
    static UserContext adminContext = new UserContext();
    static {
        adminContext.setName("Admin");
        adminContext.setRoles(Collections.emptySet());
        adminContext.setAffiliation("Org1");
        adminContext.setMspId("Org1MSP");

        // getting private key:
        // - cd /etc/hyperledger/fabric/msp/keystore
        // - cat <private-key-file>
        //      - ex: cat f019e83fa2ee33791b9a06680200b08093ebf4a084ea77e8b3536089a913e7c7_sk
        String prKS = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgXa3mln4anewXtqrMhMw6mfZhslkRa/j9P790ToKjlsihRANCAARnxLhXvU4EmnIwhVl3Bh0VcByQi2um9KsJ/QdCDjRZb1dKg447voj5SZ8SSZOUglc/v8DJFFJFTfygjwi+27gz";
        byte[] prKSBytes = Base64.getDecoder().decode(prKS);
        // getting signed PEM:
        // - cd /etc/hyperledger/fabric/msp/signcerts/
        // - cat peer0.org1.example.com-cert.pem
        String signedPem = "-----BEGIN CERTIFICATE-----\n" +
                "MIICNjCCAd2gAwIBAgIRAMnf9/dmV9RvCCVw9pZQUfUwCgYIKoZIzj0EAwIwgYEx\n" +
                "CzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1TYW4g\n" +
                "RnJhbmNpc2NvMRkwFwYDVQQKExBvcmcxLmV4YW1wbGUuY29tMQwwCgYDVQQLEwND\n" +
                "T1AxHDAaBgNVBAMTE2NhLm9yZzEuZXhhbXBsZS5jb20wHhcNMTcxMTEyMTM0MTEx\n" +
                "WhcNMjcxMTEwMTM0MTExWjBpMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZv\n" +
                "cm5pYTEWMBQGA1UEBxMNU2FuIEZyYW5jaXNjbzEMMAoGA1UECxMDQ09QMR8wHQYD\n" +
                "VQQDExZwZWVyMC5vcmcxLmV4YW1wbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0D\n" +
                "AQcDQgAEZ8S4V71OBJpyMIVZdwYdFXAckItrpvSrCf0HQg40WW9XSoOOO76I+Umf\n" +
                "EkmTlIJXP7/AyRRSRU38oI8Ivtu4M6NNMEswDgYDVR0PAQH/BAQDAgeAMAwGA1Ud\n" +
                "EwEB/wQCMAAwKwYDVR0jBCQwIoAginORIhnPEFZUhXm6eWBkm7K7Zc8R4/z7LW4H\n" +
                "ossDlCswCgYIKoZIzj0EAwIDRwAwRAIgVikIUZzgfuFsGLQHWJUVJCU7pDaETkaz\n" +
                "PzFgsCiLxUACICgzJYlW7nvZxP7b6tbeu3t8mrhMXQs956mD4+BoKuNI\n" +
                "-----END CERTIFICATE-----";
        try
        {
            adminContext.setEnrollment(new X509Enrollment(new ECPrivateKeyImpl(prKSBytes), signedPem));
        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
    }

    static HFClient newClient() throws Exception {
        HFClient instance = HFClient.createNewInstance();

        instance.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        instance.setUserContext(adminContext);

        return instance;
    }

    static Channel newChannel(HFClient instance) throws InvalidArgumentException, TransactionException, ProposalException
    {
        new ChannelConfiguration();
        Channel channel = instance.newChannel("mychannel");
        EventHub eventHub = instance.newEventHub("eventhub01", "grpc://localhost:7053");
        Orderer orderer = instance.newOrderer("orderer.example.com", "grpc://localhost:7050");
        Peer peer = instance.newPeer("peer0.org1", "grpc://localhost:7051");
        channel.addPeer(peer);
        channel.addEventHub(eventHub);
        channel.addOrderer(orderer);
        channel.initialize();

        channel.joinPeer(peer);

        return channel;
    }

    static String golangChaincodeName = "mycc";
    static HFClient instance;
    static Channel channel;
    static
    {
        try
        {
            instance = newClient();
            channel = newChannel(instance);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
        // functionName: should be "invoke", "delete", or "query"
        // acceptable args: new String[] {"a"} or new String[] {"b"}

        channel.serializeChannel();
        channel.getChannelConfigurationBytes();

        Collection<ProposalResponse> t = queryByChainCode(golangChaincodeName, "query", new String[] {"a"});
        int x = 5;
        Collection<ProposalResponse> u = storeByChainCode(golangChaincodeName);
        int y = 6;
    }

    public static Collection<ProposalResponse> storeByChainCode(String chaincodeName) throws InvalidArgumentException, ProposalException
    {
        TransactionProposalRequest transactionRequest = instance.newTransactionProposalRequest();
        transactionRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        transactionRequest.setChaincodeID(ChaincodeID.newBuilder().setName(chaincodeName).build());
        transactionRequest.setFcn("invoke");
        transactionRequest.setUserContext(instance.getUserContext());
        transactionRequest.setArgs("a", "b", "25");

        Collection<ProposalResponse> response = channel.sendTransactionProposal(transactionRequest);
        return response;
    }

    public static Collection<ProposalResponse> queryByChainCode(String chaincodeName, String functionName, String[] args)
            throws InvalidArgumentException, ProposalException
    {
//        Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
//                "Querying " + functionName + " on channel " + channel.getName());
        QueryByChaincodeRequest request = instance.newQueryProposalRequest();

        request.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        request.setChaincodeID(ChaincodeID.newBuilder().setName(chaincodeName).build());
        request.setFcn("query");
        request.setUserContext(instance.getUserContext());
        if (args != null)
            request.setArgs(args);

        Collection<ProposalResponse> response = channel.queryByChaincode(request);
        return response;
    }

    public TransactionInfo queryByTransactionId(String txnId) throws ProposalException, InvalidArgumentException {
//        Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
//                "Querying by trasaction id " + txnId + " on channel " + channel.getName());
        Collection<Peer> peers = channel.getPeers();
        for (Peer peer : peers) {
            TransactionInfo info = channel.queryTransactionByID(peer, txnId);
            return info;
        }
        return null;
    }
}
