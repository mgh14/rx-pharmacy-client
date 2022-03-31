package com.mgh14.rx.chaincode;

import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
public class ChaincodeApplication implements Chaincode
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ChaincodeApplication.class);

    // Init is called during chaincode instantiation to initialize any
    // data. Note that chaincode upgrade also calls this function to reset
    // or to migrate data, so be careful to avoid a scenario where you
    // inadvertently clobber your ledger's data!
    public Response init(final ChaincodeStub chaincodeStub)
    {
        LOGGER.info("Initializing chaincode...");
        // Get the args from the transaction proposal
        List<String> args = chaincodeStub.getStringArgs();
        LOGGER.info("Ic: payload: '{}'", args);
//        if (args.size() != 2)
//        {
//            throw new IllegalArgumentException("Incorrect arguments. Expecting a key and a value");
//            //return new Response(Response.Status.INTERNAL_SERVER_ERROR, "Incorrect arguments. Expecting a key and a value", null);
//        }
//
//        // Set up any variables or assets here by calling stub.PutState()
//
//        // We store the key and the value on the ledger
//        chaincodeStub.putState(args.get(0), args.get(1).getBytes());
//        //if err != nil {
//        //return shim.Error(fmt.Sprintf("Failed to create asset: %s", args[0]))
//        //}
        return new Response(Response.Status.SUCCESS, "Successful!", null);
    }

    public static final String[] RECOGNIZED_FUNCTIONS = {"storeRx", "storeRxFill", "queryRxId", "queryRxFillId"};

    private static void verifyPayload(String payload)
    {
        LOGGER.info("Verifying crypto signature for payload '{}'...", payload);
        // check crypto signature here
    }

    public Response storeRx(final ChaincodeStub chaincodeStub) {
        LOGGER.info("Storing Rx...");
        List<String> args = chaincodeStub.getStringArgs();
        String payload = args.get(0);
        LOGGER.info("SRx: Payload: {}", args);
        verifyPayload(payload);

        String transactionId = chaincodeStub.getTxId();
        LOGGER.info("SRx: storing under transactionId '{}'", transactionId);
        chaincodeStub.putState(transactionId, payload.getBytes());
        LOGGER.info("SRx: successfully stored transaction '{}'", transactionId);
        return new Response(200, String.format("Stored Rx at key '%s'", transactionId),
                transactionId.getBytes());
    }

    public Response storeRxFill(final ChaincodeStub chaincodeStub) {
        LOGGER.info("Storing Rx...");
        List<String> args = chaincodeStub.getStringArgs();
        LOGGER.info("SRx: Payload: {}", args);
        return null;
    }

    public Response queryRxId(final ChaincodeStub chaincodeStub)
    {
        LOGGER.info("Querying by Rx ID...");
        List<String> args = chaincodeStub.getStringArgs();
        String queryString = args.get(0);
        LOGGER.info("QRx: payload: {}", args);
        return query(chaincodeStub, queryString);
    }

    public Response queryRxFillId(final ChaincodeStub chaincodeStub) {
        LOGGER.info("Querying by Rx Fill ID...");
        List<String> args = chaincodeStub.getStringArgs();
        String queryString = args.get(0);
        LOGGER.info("QRxF: payload: {}", args);
        return query(chaincodeStub, queryString);
    }

    private static Response query(ChaincodeStub chaincodeStub, String key) {
        LOGGER.info("Querying by key '{}'...", key);
        byte[] result = chaincodeStub.getState(key);
        if (result != null)
        {
            LOGGER.info("Result for key '{}' is not null: '{}'", key, result);
            return new Response(200, null, result);
        } else
        {
            LOGGER.info("Result for key '{}' is null", result);
            return new Response(404, String.format("Could not find transaction by ID '%s'!", key),
                    new byte[0]);
        }
    }

    public Response invoke(final ChaincodeStub chaincodeStub)
    {
        String function = chaincodeStub.getFunction();
        LOGGER.info("Invoking chaincode function '{}'...", function);
        switch(function)
        {
            case "storeRx":
                return storeRx(chaincodeStub);
            case "storeRxFill":
                return storeRxFill(chaincodeStub);
            case "queryRxId":
                return queryRxId(chaincodeStub);
            case "queryRxFillId":
                return queryRxFillId(chaincodeStub);
            default:
                LOGGER.info("Unrecognized function '{}'!", function);
                return new Response(400, String.format("Unrecognized function '%s'!", function), new byte[0]);
        }
    }

    // Invoke is called per transaction on the chaincode. Each transaction is
    // either a 'get' or a 'set' on the asset created by Init function. The Set
    // method may create a new asset by specifying a new key-value pair.
    public Response invoke2(final ChaincodeStub chaincodeStub)
    {
        // Extract the function and args from the transaction proposal
        String fn = chaincodeStub.getFunction();
        List<String> args = chaincodeStub.getParameters();

        String result = "";
        try
        {
            if ("set".equals(fn))
            {
                //result = set(chaincodeStub, args);
            } else
            {
                //result = get(chaincodeStub, args);
            }
        }
        catch (Exception e)
        {
            /*if err != nil
            {
                return shim.Error(err.Error())
            }*/
            throw new RuntimeException("Failed to perform operation!");
        }

        // Return the result as success payload
        return new Response(Response.Status.SUCCESS, "Successful!", result.getBytes());
    }
}
