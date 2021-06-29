package br.com.orange

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class GrpcClientFactory {

    @Singleton
    fun pixClient(@GrpcChannel("pix") channel: ManagedChannel):
            PixServiceGrpc.PixServiceBlockingStub? {
        return PixServiceGrpc.newBlockingStub(channel)
    }

}