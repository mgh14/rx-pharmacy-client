package com.mgh14.rx.chaincode;

import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 *
 */
public class ChaincodeApplication2 implements Chaincode
{
    @Override
    public Response init(final ChaincodeStub chaincodeStub)
    {
        System.out.println("Hello world matt :)");
        return new Response(Response.Status.SUCCESS, "Successful init!", null);
    }

    @Override
    public Response invoke(final ChaincodeStub chaincodeStub)
    {
        System.out.println("Invoke world matt :)");
        return new Response(Response.Status.SUCCESS, "Successful invoke!", null);
    }
}
