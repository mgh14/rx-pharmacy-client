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
    static UserContext adminContext = new UserContext();
    static {
        adminContext.setName("Admin");
        adminContext.setRoles(Collections.emptySet());
        adminContext.setAffiliation("Org1");
        adminContext.setMspId("Org1MSP");

        // geting private key:
        // - cd /etc/hyperledger/fabric/msp/keystore
        // - cat <private-key-file>
        //      - ex: cat f019e83fa2ee33791b9a06680200b08093ebf4a084ea77e8b3536089a913e7c7_sk
        String prKS = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQghZYzLK5wJhTXLhZaOh4HM0xD4sqPPpkHtyfxmJ+Z3a+hRANCAAT1TSZRuekvjQ3mYg6fxBys2Ko68VlVnRyLfjWHQpzLeryxT6cSR+G59TzU3U56II4syhmo8LrNBMkyQvEKvcsx";
        byte[] prKSBytes = Base64.getDecoder().decode(prKS);
        // getting signed PEM:
        // - cd /etc/hyperledger/fabric/msp/signcerts/
        // - cat peer0.org1.example.com-cert.pem
        String signedPem = "-----BEGIN CERTIFICATE-----\n" +
                "MIICKTCCAc+gAwIBAgIRAOAwQjPjPtntYDlDM5hX9FowCgYIKoZIzj0EAwIwczEL\n" +
                "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" +
                "cmFuY2lzY28xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\n" +
                "Lm9yZzEuZXhhbXBsZS5jb20wHhcNMTkwMzMwMjAwODAwWhcNMjkwMzI3MjAwODAw\n" +
                "WjBqMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\n" +
                "U2FuIEZyYW5jaXNjbzENMAsGA1UECxMEcGVlcjEfMB0GA1UEAxMWcGVlcjAub3Jn\n" +
                "MS5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABPVNJlG56S+N\n" +
                "DeZiDp/EHKzYqjrxWVWdHIt+NYdCnMt6vLFPpxJH4bn1PNTdTnogjizKGajwus0E\n" +
                "yTJC8Qq9yzGjTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1Ud\n" +
                "IwQkMCKAIOFQncAxyLF0PjLU8P3SSj2bl22Y2jwYaXd9JU13n+hjMAoGCCqGSM49\n" +
                "BAMCA0gAMEUCIQCgIkkTH6VKDXxGEBjhLvWwZAMJjCc3zQyrOKDDE0sgJQIgdf8z\n" +
                "0htTJ9Zi/f0bf+q3/ibXXq+gaEgic8pO7l4Gmp4=\n" +
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

    static Channel newChannel(HFClient instance) throws InvalidArgumentException, TransactionException
    {
        Channel channel = instance.newChannel("mychannel");
        EventHub eventHub = instance.newEventHub("eventhub01", "grpc://localhost:7053");
        Orderer orderer = instance.newOrderer("orderer.example.com", "grpc://localhost:7050");
        Peer peer = instance.newPeer("peer0.org1", "grpc://localhost:7051");
        channel.addPeer(peer);
        channel.addEventHub(eventHub);
        channel.addOrderer(orderer);
        channel.initialize();

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
